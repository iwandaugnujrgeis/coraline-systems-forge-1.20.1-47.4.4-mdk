package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Inject(
            method = "canSustainPlant",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void coraline$allowTorchflowerOnNetherrack(BlockState state, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(Blocks.NETHERRACK) && plantable.getPlant(level, pos.relative(facing)).is(Blocks.TORCHFLOWER)) {
            cir.setReturnValue(true);
        }
    }
}