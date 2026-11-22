package net.zharok01.coralinesystems.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.util.DespawnCleanupUtil;
import net.zharok01.coralinesystems.util.interfaces.CowMilkDuck;
import net.zharok01.coralinesystems.weather.FogWeatherManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobMixin {

    @Inject(method = "ate", at = @At("TAIL"))
    private void coraline$onAte(CallbackInfo ci) {
        if ((Object) this instanceof CowMilkDuck duck) {
            duck.coraline$setMilked(false);
        }
    }


    @Shadow
    private boolean persistenceRequired;

    @Inject(method = "setItemSlotAndDropWhenKilled", at = @At("TAIL"))
    private void coraline$onItemPickup(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        Mob instance = (Mob) (Object) this;
        DespawnCleanupUtil.onMobPickupItem(instance, slot, stack);

        if (!instance.hasCustomName()) {
            this.persistenceRequired = false;
        }
    }

    @Inject(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;discard()V"))
    private void coraline$onDespawnDiscard(CallbackInfo ci) {
        Mob instance = (Mob) (Object) this;
        DespawnCleanupUtil.dropPickedItems(instance);
    }

    @Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
    private void coraline$noSunBurnInFog(CallbackInfoReturnable<Boolean> cir) {
        if (((Mob) (Object) this).level() instanceof ServerLevel serverLevel
                && FogWeatherManager.getData(serverLevel.getServer().overworld()).fogging) {
            cir.setReturnValue(false);
        }
    }
}