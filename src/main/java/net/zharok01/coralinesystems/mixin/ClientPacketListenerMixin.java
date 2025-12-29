package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(
            method = "handleGameEvent",
            at = @At("HEAD"),
            cancellable = true
    )
    private void silenceNoRespawnBlockMessage(ClientboundGameEventPacket packet, CallbackInfo ci) {
        if (packet.getEvent() == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE) {
            ci.cancel();
        }
    }
}