package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.fml.ModList;
import net.mehvahdjukaar.amendments.common.block.CommonCauldronCode;
import net.zharok01.coralinesystems.client.BrewingCauldronClientHooks;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.util.block.BrewingCauldronInteractions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public static final int MIN_SOLID_LEVEL = 1;
    public static final int MAX_SOLID_LEVEL = 5;

    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
    public static final EnumProperty<BrewingFluid> FLUID = EnumProperty.create("fluid", BrewingFluid.class);
    public static final IntegerProperty STRENGTH = IntegerProperty.create("strength", MIN_SOLID_LEVEL, MAX_SOLID_LEVEL);

    private static final int WINE_SPOIL_LIGHT = 13;

    private static final String AMENDMENTS_MOD_ID = "amendments";

    private static final long WINE_TARGET_PROGRESS = 24_000L;
    private static final long KOMBUCHA_TARGET_PROGRESS = WINE_TARGET_PROGRESS / 2;
    private static final double EXPECTED_RANDOM_TICKS_PER_MC_DAY = 24_000.0 * 3.0 / 4096.0;
    private static final long WINE_PROGRESS_PER_TICK =
            Math.round(WINE_TARGET_PROGRESS / EXPECTED_RANDOM_TICKS_PER_MC_DAY);
    private static final long KOMBUCHA_PROGRESS_PER_TICK =
            Math.round(KOMBUCHA_TARGET_PROGRESS / EXPECTED_RANDOM_TICKS_PER_MC_DAY);

    public BrewingCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, BrewingCauldronInteractions.BREWING);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LEVEL, 3)
                .setValue(FLUID, BrewingFluid.NONE)
                .setValue(STRENGTH, MIN_SOLID_LEVEL));
    }

    public static BlockState coraline$withDisplay(BlockState state, CultureType culture, CultureType impliedCulture,
                                                  BrewState brewState, int solidStrength) {
        return state
                .setValue(FLUID, BrewingFluid.of(culture, impliedCulture, brewState))
                .setValue(STRENGTH, solidStrength);
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == 3;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, FLUID, STRENGTH);
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return (6.0 + state.getValue(LEVEL) * 3.0) / 16.0;
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return new ItemStack(Items.CAULDRON);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BrewingCauldronBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be) {
            be.coraline$refreshState();
        }
    }

    @Override
    public void fallOn(@NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull Entity entity, float fallDistance) {
        if (isEntityInsideContent(state, pos, entity)) {
            if (level instanceof ServerLevel serverLevel) {
                if (ModList.get().isLoaded(AMENDMENTS_MOD_ID)) {
                    CommonCauldronCode.onEntityFallOnContent(level, state, entity, getContentHeight(state));
                }
                CoralinePacketHandler.broadcastCauldronSplash(pos, BrewingCauldronColors.getSplashColor(state), serverLevel);
            }
            super.fallOn(level, state, pos, entity, 0.0F);
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level level,
                             @NotNull BlockPos pos, @NotNull Entity entity) {
        if (level.isClientSide) return;
        if (!isEntityInsideContent(state, pos, entity)) return;

        if (entity.isOnFire() && entity.mayInteract(level, pos)) {
            level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    entity.getSoundSource(), 0.7f, 1.6f);
            entity.clearFire();
            lowerFillLevel(state, level, pos);
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    private static void lowerFillLevel(BlockState state, Level level, BlockPos pos) {
        int newLevel = state.getValue(LEVEL) - 1;
        BlockState newState = newLevel == 0
                ? Blocks.CAULDRON.defaultBlockState()
                : state.setValue(LEVEL, newLevel);
        level.setBlockAndUpdate(pos, newState);
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void randomTick(@NotNull BlockState state, ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be)) {
            return;
        }
        if (be.getCulture() == CultureType.NONE || be.getBrewState() != BrewState.BREWING) {
            return;
        }

        if (be.getCulture() == CultureType.WINE) {
            tickWine(be, level, pos, level.getRawBrightness(pos, 0));
        } else if (be.getCulture() == CultureType.KOMBUCHA) {
            tickKombucha(be, level, pos);
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
            fireFinishedCue(level, pos);
        }
    }

    private void tickKombucha(BrewingCauldronBlockEntity be, ServerLevel level, BlockPos pos) {
        be.addBrewProgress(KOMBUCHA_PROGRESS_PER_TICK);
        if (be.getBrewProgress() >= KOMBUCHA_TARGET_PROGRESS) {
            be.setBrewState(BrewState.FINISHED);
            fireFinishedCue(level, pos);
        }
    }

    private static void fireFinishedCue(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, CoralineSounds.CAULDRON_BREW_SUCCESS.get(), SoundSource.BLOCKS, 1.0F, 1.2F);
        CoralinePacketHandler.broadcastCauldronBurst(pos, BrewingCauldronColors.getSplashColor(level.getBlockState(pos)), level);
    }

    @Override
    public void animateTick(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity be) {
            BrewingCauldronClientHooks.coraline$animate(be, state, level, pos, random, pos.getY() + getContentHeight(state));
        }
    }
}