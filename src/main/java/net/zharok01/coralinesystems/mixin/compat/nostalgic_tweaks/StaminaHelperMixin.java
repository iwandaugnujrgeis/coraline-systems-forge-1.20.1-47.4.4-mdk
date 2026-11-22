package net.zharok01.coralinesystems.mixin.compat.nostalgic_tweaks;

import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaData;
import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaHelper;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.mixin.accessor.StaminaHelperAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(value = StaminaHelper.class, remap = false)
public class StaminaHelperMixin {

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private static void injectThreadSafeGet(Player player, CallbackInfoReturnable<StaminaData> cir) {
        HashMap<String, StaminaData> map = StaminaHelperAccessor.coraline$getPlayerData();

        synchronized (map) {
            if (!map.containsKey(player.getStringUUID())) {
                map.put(player.getStringUUID(), new StaminaData());
            }
            cir.setReturnValue(map.get(player.getStringUUID()));
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private static void injectThreadSafeRemove(Player player, CallbackInfo ci) {
        HashMap<String, StaminaData> map = StaminaHelperAccessor.coraline$getPlayerData();

        synchronized (map) {
            map.remove(player.getStringUUID());
        }
        ci.cancel();
    }

    @Inject(method = "reset", at = @At("HEAD"), cancellable = true)
    private static void injectThreadSafeReset(CallbackInfo ci) {
        HashMap<String, StaminaData> map = StaminaHelperAccessor.coraline$getPlayerData();

        synchronized (map) {
            map.clear();
        }
        ci.cancel();
    }
}