package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {

    @Inject(method = "getLightBlock", at = @At("HEAD"), cancellable = true)
    private void coraline$turnLeavesOpaque(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == Blocks.DARK_OAK_LEAVES
                || (Object) this == Blocks.SPRUCE_LEAVES) {
            cir.setReturnValue(15);
        }
    }
}