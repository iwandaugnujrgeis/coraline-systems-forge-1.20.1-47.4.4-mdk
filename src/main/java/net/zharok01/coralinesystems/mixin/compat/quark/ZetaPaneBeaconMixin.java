package net.zharok01.coralinesystems.mixin.compat.quark;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.block.ZetaInheritedPaneBlock;

import javax.annotation.Nullable;

@Mixin(value = ZetaInheritedPaneBlock.class, remap = false)
public abstract class ZetaPaneBeaconMixin extends Block {

    public ZetaPaneBeaconMixin(Properties properties) {
        super(properties);
    }

    @Final @Shadow public IZetaBlock parent;

    @Override @Nullable
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader level, BlockPos pos, BlockPos beaconPos) {
        BlockState parentState = this.parent.getBlock().defaultBlockState();
        return this.parent.getBlock().getBeaconColorMultiplier(parentState, level, pos, beaconPos);
    }
}