package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TesseractBlock extends BaseEntityBlock {

    public TesseractBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TesseractBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, CoralineBlockEntities.TESSERACT.get(), TesseractBlockEntity::tick);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof TesseractBlockEntity tesseract) {
                tesseract.unleashDragon();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static void notifyNearbyTesseracts(Level level, BlockPos brokenPos) {
        for (int i = 1; i <= TesseractBlockEntity.MAX_ALTAR_DISTANCE; i++) {
            BlockPos checkPos = brokenPos.above(i);
            BlockState state = level.getBlockState(checkPos);

            if (state.isAir()) continue;

            if (state.is(CoralineBlocks.TESSERACT.get())
                    && level.getBlockEntity(checkPos) instanceof TesseractBlockEntity tesseract) {
                tesseract.markStructureDirty();
            }
            break;
        }
    }
}