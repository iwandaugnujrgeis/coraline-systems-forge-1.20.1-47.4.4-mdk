package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BushBlock.class)
public abstract class BushBlockMixin {

    @Inject(
            method = "mayPlaceOn",
            at = @At("HEAD")
    )
    private void coraline$mayPlaceTorchflowerOnNetherrack(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == Blocks.TORCHFLOWER) {
            state.is(Blocks.NETHERRACK);
        }
    }
}