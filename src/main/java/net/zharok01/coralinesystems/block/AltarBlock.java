package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class AltarBlock extends Block {

    public AltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            TesseractBlock.notifyNearbyTesseracts(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}