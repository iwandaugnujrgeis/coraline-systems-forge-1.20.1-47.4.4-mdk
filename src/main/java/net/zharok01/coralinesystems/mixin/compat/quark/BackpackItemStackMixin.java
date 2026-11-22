package net.zharok01.coralinesystems.mixin.compat.quark;

import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.util.compat.quark.BackpackDurability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class BackpackItemStackMixin {
    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void coraline$backpackMaxDamage(CallbackInfoReturnable<Integer> callback) {
        if (BackpackDurability.isBackpack((ItemStack) (Object) this)) {
            callback.setReturnValue(BackpackDurability.MAX_DURABILITY);
        }
    }

    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true)
    private void coraline$backpackIsDamageable(CallbackInfoReturnable<Boolean> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        if (BackpackDurability.isBackpack(stack)) {
            callback.setReturnValue(!stack.isEmpty());
        }
    }

    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    private <T extends LivingEntity> void coraline$routeBackpackWear(int amount, T entity, Consumer<T> onBroken, CallbackInfo callback) {
        ItemStack stack = (ItemStack) (Object) this;
        if (entity instanceof ServerPlayer player && BackpackDurability.isBackpack(stack)) {
            BackpackDurability.damage(player, stack, amount);
            callback.cancel();
        }
    }
}