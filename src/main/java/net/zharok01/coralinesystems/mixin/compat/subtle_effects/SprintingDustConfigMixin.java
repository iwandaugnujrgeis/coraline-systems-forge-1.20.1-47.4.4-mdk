package net.zharok01.coralinesystems.mixin.compat.subtle_effects;

import einstein.subtle_effects.configs.ModEntityConfigs;
import einstein.subtle_effects.init.ModConfigs;
import einstein.subtle_effects.networking.clientbound.ClientBoundEntitySpawnSprintingDustCloudsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "einstein.subtle_effects.networking.clientbound.ClientPacketHandlers", remap = false)
public abstract class SprintingDustConfigMixin {

    @Inject(
            method = "handle(Leinstein/subtle_effects/networking/clientbound/ClientBoundEntitySpawnSprintingDustCloudsPacket;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void coraline$respectPlayerRunningDustConfig(ClientBoundEntitySpawnSprintingDustCloudsPacket packet, CallbackInfo ci) {
        ModEntityConfigs entityConfigs = ModConfigs.ENTITIES;
        if (!entityConfigs.dustClouds.playerRunning) {
            ci.cancel();
        }
    }
}
