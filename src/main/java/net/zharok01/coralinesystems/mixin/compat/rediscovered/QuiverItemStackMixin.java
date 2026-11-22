package net.zharok01.coralinesystems.mixin.compat.rediscovered;

import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.zharok01.coralinesystems.quiver.QuiverContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class QuiverItemStackMixin {
    @Inject(method = "getRarity", at = @At("RETURN"), cancellable = true)
    private void coraline$cyanLoadedQuiver(CallbackInfoReturnable<Rarity> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        if (QuiverContents.isQuiver(stack) && QuiverContents.hasArrows(stack)) {
            callback.setReturnValue(Rarity.RARE);
        }
    }

    @Inject(method = "hasFoil", at = @At("HEAD"), cancellable = true)
    private void coraline$noQuiverGlint(CallbackInfoReturnable<Boolean> callback) {
        if (QuiverContents.isQuiver((ItemStack) (Object) this)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void coraline$quiverMaxDamage(CallbackInfoReturnable<Integer> callback) {
        if (QuiverContents.isQuiver((ItemStack) (Object) this)) {
            callback.setReturnValue(QuiverContents.MAX_DURABILITY);
        }
    }

    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true)
    private void coraline$quiverIsDamageable(CallbackInfoReturnable<Boolean> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        if (QuiverContents.isQuiver(stack)) {
            callback.setReturnValue(!stack.isEmpty());
        }
    }

    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    private <T extends LivingEntity> void coraline$routeQuiverWear(int amount, T entity, Consumer<T> onBroken, CallbackInfo callback) {
        ItemStack stack = (ItemStack) (Object) this;
        if (entity instanceof ServerPlayer player && QuiverContents.isQuiver(stack)) {
            QuiverContents.damage(player, stack, amount);
            callback.cancel();
        }
    }
}