package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ConcreteBlock extends FallingBlock {

    public ConcreteBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public int getDustColor(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 0x787878;
    }
}