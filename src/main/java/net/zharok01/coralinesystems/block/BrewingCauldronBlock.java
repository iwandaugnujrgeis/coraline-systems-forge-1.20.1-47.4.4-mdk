package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.zharok01.coralinesystems.client.color.CoralineBlockColors;
import net.zharok01.coralinesystems.network.CauldronSplashPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.util.BrewingCauldronInteractions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public static final int MIN_SOLID_LEVEL = 1;
    public static final int MAX_SOLID_LEVEL = 5;

    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;

    private static final int WINE_MAX_LIGHT = 6;
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
     * block-local units (0-1).  Matches LayeredCauldronBlock's formula
     * exactly so that isEntityInsideContent() correctly detects overlap
     * and so splash particles spawn at the right height.
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
     * Called every tick that an entity overlaps with this block.
     * Mirrors LayeredCauldronBlock.entityInside:
     * - extinguishes burning entities (server-side only)
     * - plays a water-splash sound at the fluid surface (server-side)
     * - sends a tinted splash-particle packet to nearby clients
     */
    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level level,
                             @NotNull BlockPos pos, @NotNull Entity entity) {
        if (level.isClientSide) return;
        if (!isEntityInsideContent(state, pos, entity)) return;

        // Extinguish fire — same condition as LayeredCauldronBlock, but we
        // deliberately do NOT lower the fill level (the brew is not water).
        if (entity.isOnFire()) {
            entity.clearFire();
        }

        // 1. Fetch the BlockEntity to access our anti-spam cooldown logic
        if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
            return;
        }

        // 2. Determine if the entity is actually causing a disturbance.
        // We check if they are actively moving horizontally/vertically, ignoring
        // the tiny floating jitters (< 0.001) that occur when standing still.
        double movementSq = entity.getDeltaMovement().horizontalDistanceSqr() + Math.pow(entity.getDeltaMovement().y, 2);
        boolean isMoving = movementSq > 0.001;
        boolean justEntered = entity.getDeltaMovement().y < -0.01;

        if (!justEntered && !isMoving) return;

        // 3. Apply the strict 8-tick debounce cooldown from the BlockEntity.
        // This completely prevents the "machine gun" packet spam.
        if (!be.tryConsumeSplashCooldown(level.getGameTime())) return;

        // Play the water-splash sound at the fluid surface position.
        // Reduced the volume to 0.25f so it isn't deafening when bouncing inside.
        level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, entity.getSoundSource(),
                0.25f, 0.8f + level.getRandom().nextFloat() * 0.4f);

        // Compute the tint color using the same logic as CoralineBlockColors,
        // but called directly here on the server.
        int color = getFluidColor(level, pos, state);

        // Send tinted splash particles to clients near this chunk.
        if (level instanceof ServerLevel serverLevel) {
            if (entity instanceof ServerPlayer serverPlayer) {
                CoralinePacketHandler.sendCauldronSplash(pos, color, serverPlayer);
            } else {
                // Non-player entity (mob, item, etc.) — broadcast to all trackers.
                CauldronSplashPacket packet = new CauldronSplashPacket(pos, color);
                CoralinePacketHandler.INSTANCE.send(
                        net.minecraftforge.network.PacketDistributor.TRACKING_CHUNK.with(
                                () -> serverLevel.getChunkAt(pos)),
                        packet);
            }
        }
    }

    /**
     * Derives the packed RGB fluid color from the block entity at pos,
     * reusing the same branch logic as CoralineBlockColors.CAULDRON_CONTENT.
     * Returns a mid-purple fallback if the block entity is missing.
     */
    private static int getFluidColor(Level level, BlockPos pos, BlockState state) {
        // Delegate directly to our existing BlockColor handler, which already
        // knows all the culture/brew-state/strength branches.
        // BlockColor.getColor takes (state, level, pos, tintIndex); tintIndex
        // 0 is the fluid surface element, which is what we want.
        int color = CoralineBlockColors.CAULDRON_CONTENT.getColor(state, level, pos, 0);
        // getColor returns -1 (NO_TINT) if the block entity is absent or the
        // branch is indeterminate — fall back to a neutral mid-purple.
        return color == -1 ? 0xd070d0 : color;
    }

    // ── Tick logic (unchanged) ──────────────────────────────────────────────

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
        if (light >= KOMBUCHA_MIN_CORRECT_LIGHT) {
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
            fireFinishedCue(level, pos);
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
            fireFinishedCue(level, pos);
        }
    }

    private static void fireFinishedCue(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, CoralineSounds.CAULDRON_BREW_SUCCESS.get(), SoundSource.BLOCKS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                8, 0.25, 0.2, 0.25, 0.0);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
            return;
        }
        if (be.getCulture() == CultureType.NONE || be.getBrewState() != BrewState.BREWING) {
            return;
        }

        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP,
                    pos.getX() + 0.2 + random.nextDouble() * 0.6,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                    0.0, 0.05, 0.0);
        }
        if (random.nextInt(20) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    CoralineSounds.CAULDRON_BUBBLING.get(), SoundSource.BLOCKS,
                    0.3F, 0.6F + random.nextFloat() * 0.2F, false);
        }
    }
}
