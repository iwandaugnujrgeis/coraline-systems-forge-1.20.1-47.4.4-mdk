package net.zharok01.coralinesystems.mixin.compat.sweety_garden;

import net.mcreator.sweetygarden.init.SweetyGardenModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BushBlock.class)
public abstract class WoodSorrelBlockMixin {

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void coraline$allowWoodSorrelOnOverworldBaseStone(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == SweetyGardenModBlocks.WOOD_SORREL.get() && state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            cir.setReturnValue(true);
        }
    }
}
