package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.zharok01.coralinesystems.client.color.CoralineBlockColors;
import net.zharok01.coralinesystems.network.CauldronSplashPacket;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.util.BrewingCauldronInteractions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public static final int MIN_SOLID_LEVEL = 1;
    public static final int MAX_SOLID_LEVEL = 5;

    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;

    /**
     * Wine spoils when light reaches this level or above.  Mirrors the maximum
     * light level at which mushrooms can grow in Vanilla (light ≤ 12), so Wine
     * spoils at light ≥ 13, the same threshold that prevents mushroom growth.
     */
    private static final int WINE_SPOIL_LIGHT = 13;

    /** Minimum light level at which Kombucha brews at full speed. */
    private static final int KOMBUCHA_MIN_CORRECT_LIGHT = 7;

    private static final long WINE_TARGET_PROGRESS = 24_000L;
    private static final long KOMBUCHA_TARGET_PROGRESS = WINE_TARGET_PROGRESS / 2;
    private static final double EXPECTED_RANDOM_TICKS_PER_MC_DAY = 24_000.0 * 3.0 / 4096.0;
    private static final long WINE_PROGRESS_PER_TICK =
            Math.round(WINE_TARGET_PROGRESS / EXPECTED_RANDOM_TICKS_PER_MC_DAY);
    private static final long KOMBUCHA_PROGRESS_PER_TICK =
            Math.round(KOMBUCHA_TARGET_PROGRESS / EXPECTED_RANDOM_TICKS_PER_MC_DAY);
    private static final long[] KOMBUCHA_LIGHT_PUNISHMENT = {5000L, 5000L, 4000L, 3000L, 2000L, 1000L};

    public BrewingCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, BrewingCauldronInteractions.BREWING);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 3));
    }

    // ── AbstractCauldronBlock requirements ──────────────────────────────────

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == 3;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    /**
     * Returns the Y-offset of the fluid surface inside the cauldron, in
     * block-local units (0–1). Matches {@code LayeredCauldronBlock}'s formula
     * exactly so that {@code isEntityInsideContent()} and splash particles
     * both use the correct height.
     *
     * level=1 → 9/16 ≈ 0.5625
     * level=2 → 12/16 = 0.75
     * level=3 → 15/16 = 0.9375
     */
    @Override
    protected double getContentHeight(BlockState state) {
        return (6.0 + state.getValue(LEVEL) * 3.0) / 16.0;
    }

    // ── EntityBlock wiring ──────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BrewingCauldronBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Entity-inside handling ──────────────────────────────────────────────

    /**
     * Mirrors {@code LayeredCauldronBlock.entityInside}:
     * <ul>
     *   <li>Extinguishes burning entities and consumes one level of liquid
     *       (reverting to an empty Vanilla cauldron at level 0).</li>
     *   <li>On entry, plays a water-splash sound and sends a packet to nearby
     *       clients to spawn tinted splash particles at the fluid surface.</li>
     * </ul>
     *
     * "Entry" is debounced through {@link BrewingCauldronBlockEntity#tryConsumeSplashCooldown}
     * rather than the fragile {@code getDeltaMovement().y} check used in the
     * previous version.  This fires the splash FX at most once per
     * {@code SPLASH_COOLDOWN_TICKS} ticks, which stops the continuous spam
     * while an entity stands still inside the cauldron.
     */
    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level level,
                             @NotNull BlockPos pos, @NotNull Entity entity) {
        if (level.isClientSide) return;
        if (!isEntityInsideContent(state, pos, entity)) return;

        // ── Fire extinguish — mirrors LayeredCauldronBlock ──────────────────
        if (entity.isOnFire() && entity.mayInteract(level, pos)) {
            entity.clearFire();
            lowerFillLevel(state, level, pos);
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            // Skip splash FX after extinguishing — the fire-out sound is
            // enough; the splash packet would be redundant noise.
            return;
        }

        // ── Splash FX — debounced so it fires only on actual entry ───────────
        if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) return;
        if (!be.tryConsumeSplashCooldown(level.getGameTime())) return;

        // Play the splash sound at the cauldron position.
        level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, entity.getSoundSource(),
                0.5f, 0.8f + level.getRandom().nextFloat() * 0.4f);

        // Send tinted splash particles to nearby clients.
        int color = getFluidColor(level, pos, state);
        if (level instanceof ServerLevel serverLevel) {
            if (entity instanceof ServerPlayer serverPlayer) {
                CoralinePacketHandler.sendCauldronSplash(pos, color, serverPlayer);
            } else {
                CauldronSplashPacket packet = new CauldronSplashPacket(pos, color);
                CoralinePacketHandler.INSTANCE.send(
                        net.minecraftforge.network.PacketDistributor.TRACKING_CHUNK.with(
                                () -> serverLevel.getChunkAt(pos)),
                        packet);
            }
        }
    }

    /**
     * Decrements the cauldron's LEVEL by one, reverting to an empty Vanilla
     * cauldron block when the level reaches zero.  Mirrors
     * {@code LayeredCauldronBlock.lowerFillLevel}.
     */
    private static void lowerFillLevel(BlockState state, Level level, BlockPos pos) {
        int newLevel = state.getValue(LEVEL) - 1;
        BlockState newState = newLevel == 0
                ? Blocks.CAULDRON.defaultBlockState()
                : state.setValue(LEVEL, newLevel);
        level.setBlockAndUpdate(pos, newState);
    }

    /**
     * Derives the packed RGB fluid color from the block entity at {@code pos},
     * delegating to {@link CoralineBlockColors#CAULDRON_CONTENT} so the color
     * is always consistent with what the renderer displays.
     * Returns a neutral mid-purple fallback when the block entity is absent.
     */
    static int getFluidColor(Level level, BlockPos pos, BlockState state) {
        int color = CoralineBlockColors.CAULDRON_CONTENT.getColor(state, level, pos, 1);
        return color == -1 ? 0xd070d0 : color;
    }

    // ── Tick logic ──────────────────────────────────────────────────────────

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
            return;
        }
        if (be.getCulture() == CultureType.NONE || be.getBrewState() != BrewState.BREWING) {
            return;
        }

        int light = level.getRawBrightness(pos, 0);

        if (be.getCulture() == CultureType.WINE) {
            tickWine(be, level, pos, light);
        } else if (be.getCulture() == CultureType.KOMBUCHA) {
            tickKombucha(be, level, pos, light);
        }
    }

    private void tickWine(BrewingCauldronBlockEntity be, ServerLevel level, BlockPos pos, int light) {
        // Wine spoils at light ≥ 13, matching the light threshold above which
        // mushrooms cannot grow in Vanilla.
        if (light >= WINE_SPOIL_LIGHT) {
            be.setBrewState(BrewState.SPOILED);
            level.playSound(null, pos, CoralineSounds.CAULDRON_BREW_SPOILED.get(), SoundSource.BLOCKS, 1.0F, 0.7F);
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    10, 0.3, 0.2, 0.3, 0.02);
            return;
        }

        be.addBrewProgress(WINE_PROGRESS_PER_TICK);
        if (be.getBrewProgress() >= WINE_TARGET_PROGRESS) {
            be.setBrewState(BrewState.FINISHED);
            fireFinishedCue(be, level, pos);
        }
    }

    private void tickKombucha(BrewingCauldronBlockEntity be, ServerLevel level, BlockPos pos, int light) {
        if (light <= 0) {
            return;
        }

        long gain;
        if (light >= KOMBUCHA_MIN_CORRECT_LIGHT) {
            gain = KOMBUCHA_PROGRESS_PER_TICK;
        } else {
            long punishment = KOMBUCHA_LIGHT_PUNISHMENT[light - 1];
            gain = Math.max(0L, KOMBUCHA_PROGRESS_PER_TICK - punishment);
        }

        if (gain <= 0L) {
            return;
        }

        be.addBrewProgress(gain);
        if (be.getBrewProgress() >= KOMBUCHA_TARGET_PROGRESS) {
            be.setBrewState(BrewState.FINISHED);
            fireFinishedCue(be, level, pos);
        }
    }

    /**
     * Plays the brew-completion sound, spawns a HAPPY_VILLAGER burst (the
     * "success" vanilla cue), and sends a tinted splash-particle packet to
     * all clients tracking the chunk.  The tinted burst visually distinguishes
     * a newly-finished brew from an already-finished one that the player just
     * walked past.
     */
    private static void fireFinishedCue(BrewingCauldronBlockEntity be, ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, CoralineSounds.CAULDRON_BREW_SUCCESS.get(), SoundSource.BLOCKS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                8, 0.25, 0.2, 0.25, 0.0);

        // Derive the current fluid color directly from the BE (which has just
        // been set to FINISHED) so the burst matches the drink's final color.
        BlockState state = level.getBlockState(pos);
        int color = getFluidColor(level, pos, state);
        CoralinePacketHandler.broadcastCauldronSplash(pos, color, level);
    }

    // ── Client-side ambient tick ─────────────────────────────────────────────

    /**
     * Spawns tinted rising bubble particles and plays the bubbling ambient
     * sound when the cauldron is actively fermenting.
     *
     * <p>Bubble color is read directly from the block entity here on the
     * client (same as the rendering tint), so no server packet is needed.
     * The color lookup mirrors {@link #getFluidColor} but runs client-side
     * where the BE is already loaded.</p>
     *
     * <p>Particle density scales with strength (1-5) — stronger brews produce
     * more vigorous bubbling, giving a visible signal of the ingredient level
     * without any UI.</p>
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
            return;
        }
        if (be.getCulture() == CultureType.NONE || be.getBrewState() != BrewState.BREWING) {
            return;
        }

        // ── Tinted rising bubbles ────────────────────────────────────────────
        // Derive the particle color from the same source as the block tint.
        int rgb = CoralineBlockColors.CAULDRON_CONTENT.getColor(state, level, pos, 1);
        if (rgb != -1) {
            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;

            // Surface Y — same formula as getContentHeight.
            double surfaceY = pos.getY() + getContentHeight(state);

            // One bubble per animateTick call; occasionally two for higher-
            // strength brews (strength 4-5 → ~40 % chance of a second bubble).
            int bubbleCount = 1 + (be.getSolidStrength() >= 4 && random.nextInt(5) < 2 ? 1 : 0);
            for (int i = 0; i < bubbleCount; i++) {
                double bx = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                double bz = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                // Spawn slightly below the surface so the bubble visibly rises.
                level.addParticle(CoralineParticles.CAULDRON_BUBBLE.get(),
                        bx, surfaceY - 0.05, bz,
                        // Pack r/g/b into velocity slots — Provider reads them as color.
                        r, g, b);
            }
        }

        // ── Ambient bubbling sound ───────────────────────────────────────────
        if (random.nextInt(20) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    CoralineSounds.CAULDRON_BUBBLING.get(), SoundSource.BLOCKS,
                    0.3F, 0.6F + random.nextFloat() * 0.2F, false);
        }
    }
}