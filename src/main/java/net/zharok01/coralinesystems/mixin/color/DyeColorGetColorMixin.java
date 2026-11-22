package net.zharok01.coralinesystems.mixin.color;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DyeColor.class)
public abstract class DyeColorGetColorMixin {

    @Inject(
            method = "getColor",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void coraline$extendColorTagLookup(ItemStack stack, CallbackInfoReturnable<DyeColor> cir) {
        if (stack.getItem() instanceof DyeItem dyeItem) {
            cir.setReturnValue(dyeItem.getDyeColor());
            return;
        }

        for (DyeColor color : DyeColor.values()) {
            if (stack.is(color.getTag())) {
                cir.setReturnValue(color);
                return;
            }
        }

        cir.setReturnValue(null);
    }
}