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
import net.mehvahdjukaar.amendments.common.block.CommonCauldronCode;
import net.zharok01.coralinesystems.client.color.CoralineBlockColors;
import net.zharok01.coralinesystems.network.CauldronSplashPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.util.BrewingCauldronInteractions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public static final int MIN_SOLID_LEVEL = 1;
    public static final int MAX_SOLID_LEVEL = 5;

    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;

    private static final int WINE_SPOIL_LIGHT = 13;
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

    @Override
    protected double getContentHeight(BlockState state) {
        return (6.0 + state.getValue(LEVEL) * 3.0) / 16.0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BrewingCauldronBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Entity Impact (The Missing Hook) ────────────────────────────────────

    /**
     * Intercepts entities jumping or falling into the cauldron to play the initial
     * splash effects. Mirrors the exact behavior of Amendments' ModCauldronBlock.m_142072_.
     */
    @Override
    public void fallOn(@NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull Entity entity, float fallDistance) {
        if (isEntityInsideContent(state, pos, entity)) {
            if (!level.isClientSide) {
                // Layer 1: Amendments handles the impact calculation, item pickups, and base sound
                CommonCauldronCode.onEntityFallOnContent(level, state, entity, getContentHeight(state));

                // Layer 2: Our custom tinted burst
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
            // Negate fall damage because the player landed in liquid
            super.fallOn(level, state, pos, entity, 0.0F);
        } else {
            // Player landed on the solid iron rim of the cauldron; apply normal fall damage
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    // ── Entity-inside handling ──────────────────────────────────────────────

    /**
     * Handles entities actively standing inside the brewing cauldron's fluid content.
     * Splash logic has been migrated to fallOn() to match Vanilla/Amendments standards.
     */
    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level level,
                             @NotNull BlockPos pos, @NotNull Entity entity) {
        if (level.isClientSide) return;
        if (!isEntityInsideContent(state, pos, entity)) return;

        // ── Fire extinguish ─────────────────────────────────────────────────
        if (entity.isOnFire() && entity.mayInteract(level, pos)) {
            level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    entity.getSoundSource(), 0.7f, 1.6f);
            entity.clearFire();
            lowerFillLevel(state, level, pos);
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        }
        // Note: Splash FX debounce removed. Native Cauldrons do not splash repeatedly while walking!
    }

    private static void lowerFillLevel(BlockState state, Level level, BlockPos pos) {
        int newLevel = state.getValue(LEVEL) - 1;
        BlockState newState = newLevel == 0
                ? Blocks.CAULDRON.defaultBlockState()
                : state.setValue(LEVEL, newLevel);
        level.setBlockAndUpdate(pos, newState);
    }

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

    private static void fireFinishedCue(BrewingCauldronBlockEntity be, ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, CoralineSounds.CAULDRON_BREW_SUCCESS.get(), SoundSource.BLOCKS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                8, 0.25, 0.2, 0.25, 0.0);

        BlockState state = level.getBlockState(pos);
        int color = getFluidColor(level, pos, state);
        CoralinePacketHandler.broadcastCauldronSplash(pos, color, level);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
            return;
        }
        if (be.getCulture() == CultureType.NONE || be.getBrewState() != BrewState.BREWING) {
            return;
        }

        int rgb = CoralineBlockColors.CAULDRON_CONTENT.getColor(state, level, pos, 1);
        if (rgb != -1) {
            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;

            double surfaceY = pos.getY() + getContentHeight(state);

            int bubbleCount = 1 + (be.getSolidStrength() >= 4 && random.nextInt(5) < 2 ? 1 : 0);
            for (int i = 0; i < bubbleCount; i++) {
                double bx = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                double bz = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                level.addParticle(CoralineParticles.CAULDRON_BUBBLE.get(),
                        bx, surfaceY - 0.05, bz,
                        r, g, b);
            }
        }

        if (random.nextInt(20) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    CoralineSounds.CAULDRON_BUBBLING.get(), SoundSource.BLOCKS,
                    0.3F, 0.6F + random.nextFloat() * 0.2F, false);
        }
    }
}