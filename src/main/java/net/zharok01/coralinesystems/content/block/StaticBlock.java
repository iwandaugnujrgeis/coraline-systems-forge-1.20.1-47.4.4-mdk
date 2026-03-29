package net.zharok01.coralinesystems.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities; // Make sure you have this registry!
import org.jetbrains.annotations.Nullable;

public class StaticBlock extends BaseEntityBlock {

    public StaticBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CoralineBlockEntities.STATIC_BLOCK_ENTITY.get().create(pos, state);
    }

    // We set this to INVISIBLE because our Custom Renderer will draw the block instead.
    // If we left this as MODEL, the game would try to draw a missing texture box too!
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    // When the block is placed, tell the BlockEntity to look at the block below it.
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof StaticBlockEntity staticBe) {
                // Copy the state of the block directly below it
                BlockState stateBelow = level.getBlockState(pos.below());
                staticBe.setCopiedState(stateBelow);
            }
        }
    }
}