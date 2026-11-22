package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Redirect(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean coraline$allowGlideStartWhileLevitating(ItemStack itemStack, LivingEntity entity) {
        return itemStack.canElytraFly(entity) || entity.hasEffect(MobEffects.LEVITATION);
    }
}