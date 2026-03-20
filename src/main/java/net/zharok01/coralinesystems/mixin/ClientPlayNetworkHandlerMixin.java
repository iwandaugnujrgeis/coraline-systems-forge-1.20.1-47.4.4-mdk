
package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.world.phys.AABB;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "handleLevelEvent", at = @At("TAIL"))
    public void onJukeboxEvent(ClientboundLevelEventPacket packet, CallbackInfo ci) {
        // 1010 is the Level Event ID for "Jukebox starts playing"
        // 1011 is the Level Event ID for "Jukebox stops playing"
        if (packet.getType() == 1010 || packet.getType() == 1011) {
            BlockPos pos = packet.getPos();
            var level = Minecraft.getInstance().level;

            if (level != null) {
                // Find all Helpers within __ blocks of the Jukebox
                List<HelperEntity> helpers = level.getEntitiesOfClass(
                        HelperEntity.class,
                        new AABB(pos).inflate(64.0D)
                );

                boolean isStarting = packet.getType() == 1010;

                for (HelperEntity helper : helpers) {
                    helper.setJamming(isStarting);
                }
            }
        }
    }
}
