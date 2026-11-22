package net.zharok01.coralinesystems.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// TODO: Is redundant since Bed removal in 5.0/4.20.11?
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Redirect(
            method = "setRespawnPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void silenceSpawnMessage(ServerPlayer instance, Component component) {
    }
}