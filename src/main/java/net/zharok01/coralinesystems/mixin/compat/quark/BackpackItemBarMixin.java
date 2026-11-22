package net.zharok01.coralinesystems.mixin.compat.quark;

import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.util.compat.quark.BackpackDurability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class BackpackItemBarMixin {
    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void coraline$backpackBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> callback) {
        if (BackpackDurability.isBackpack(stack)) {
            callback.setReturnValue(Math.round(13.0F - stack.getDamageValue() * 13.0F / stack.getMaxDamage()));
        }
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void coraline$backpackBarColor(ItemStack stack, CallbackInfoReturnable<Integer> callback) {
        if (BackpackDurability.isBackpack(stack)) {
            float health = Math.max(0.0F, (stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage());
            callback.setReturnValue(Mth.hsvToRgb(health / 3.0F, 1.0F, 1.0F));
        }
    }
}