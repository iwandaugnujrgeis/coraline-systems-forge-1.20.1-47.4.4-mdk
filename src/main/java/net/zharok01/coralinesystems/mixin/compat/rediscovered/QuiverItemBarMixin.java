package net.zharok01.coralinesystems.mixin.compat.rediscovered;

import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.quiver.QuiverContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class QuiverItemBarMixin {
    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void coraline$quiverBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> callback) {
        if (QuiverContents.isQuiver(stack)) {
            callback.setReturnValue(Math.round(13.0F - stack.getDamageValue() * 13.0F / stack.getMaxDamage()));
        }
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void coraline$quiverBarColor(ItemStack stack, CallbackInfoReturnable<Integer> callback) {
        if (QuiverContents.isQuiver(stack)) {
            float health = Math.max(0.0F, (stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage());
            callback.setReturnValue(Mth.hsvToRgb(health / 3.0F, 1.0F, 1.0F));
        }
    }
}