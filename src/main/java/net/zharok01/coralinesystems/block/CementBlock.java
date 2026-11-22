package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;

public class CementBlock extends FallingBlock {

    public CementBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    private BlockState concrete() {
        return CoralineBlocks.CONCRETE.get().defaultBlockState();
    }

    private static boolean shouldSolidify(BlockGetter level, BlockPos pos, BlockState state) {
        return shouldSolidify(level, pos, state, level.getFluidState(pos));
    }

    private static boolean shouldSolidify(BlockGetter level, BlockPos pos, BlockState state,
                                          net.minecraft.world.level.material.FluidState fluidState) {
        return state.canBeHydrated(level, pos, fluidState, pos) || touchesWater(level, pos, state);
    }

    private static boolean touchesWater(BlockGetter level, BlockPos pos, BlockState state) {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        for (Direction direction : Direction.values()) {
            BlockState neighborState = level.getBlockState(mutablePos);
            if (direction != Direction.DOWN
                    || state.canBeHydrated(level, pos, neighborState.getFluidState(), mutablePos)) {
                mutablePos.setWithOffset(pos, direction);
                neighborState = level.getBlockState(mutablePos);
                if (state.canBeHydrated(level, pos, neighborState.getFluidState(), mutablePos)
                        && !neighborState.isFaceSturdy(level, pos, direction.getOpposite())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void playConversionEffect(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            level.playSound(null, pos, CoralineSounds.CEMENT_HARDENING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

            if (level instanceof ServerLevel serverLevel) {
                BlockState state = level.getBlockState(pos);
                BlockParticleOption dustParticle = new BlockParticleOption(ParticleTypes.FALLING_DUST, state);

                serverLevel.sendParticles(dustParticle, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 12, 0.25D, 0.25D, 0.25D, 0.05D);
            }
        }
    }

    @Override
    public void onLand(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState fallingState,
                       BlockState replaceableState, @NotNull FallingBlockEntity fallingBlock) {
        if (shouldSolidify(level, pos, fallingState, replaceableState.getFluidState())) {
            level.setBlock(pos, concrete(), 3);
            playConversionEffect(level, pos);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter blockGetter = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState existingState = blockGetter.getBlockState(clickedPos);
        if (shouldSolidify(blockGetter, clickedPos, existingState)) {
            if (blockGetter instanceof Level level) {
                playConversionEffect(level, clickedPos);
            }
            return concrete();
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
                                           @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (touchesWater(level, pos, state)) {
            if (level instanceof Level worldLevel) {
                playConversionEffect(worldLevel, pos);
            }
            return concrete();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public int getDustColor(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 0xB0B0B0;
    }
}