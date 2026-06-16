package net.zharok01.coralinesystems.mixin;

import com.legacy.structure_gel.api.block.GelPortalBlock;
import com.legacy.structure_gel.core.capability.entity.GelEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.zharok01.coralinesystems.client.portal.PortalTransitionContext;
import net.zharok01.coralinesystems.client.screen.TranscendingScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin
{
    @Final @Shadow private Minecraft minecraft;

    @Inject(
            method = "handleGameEvent",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$silenceNoRespawnBlock(
            ClientboundGameEventPacket packet, CallbackInfo ci)
    {
        if (packet.getEvent() == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE)
            ci.cancel();
    }

    /**
     * Snapshot portal and Nether portal state at HEAD of handleRespawn,
     * before the player entity is rebuilt and those values are lost.
     */
    @Inject(
            method = "handleRespawn",
            at = @At("HEAD")
    )
    private void coraline$snapshotPortalOnRespawn(
            ClientboundRespawnPacket packet, CallbackInfo ci)
    {
        GelPortalBlock gelPortal = GelEntity.getPortalClient();

        if (gelPortal != null)
        {
            PortalTransitionContext.set(gelPortal);
        }
        else if (this.minecraft.player != null
                && this.minecraft.player.spinningEffectIntensity > 0)
        {
            // Nether portal detected — signal with a sentinel state
            PortalTransitionContext.setNetherPortal();
        }
        else
        {
            PortalTransitionContext.clear();
        }
    }

    /**
     * Redirect the single setScreen(new ReceivingLevelScreen()) call.
     */
    @Redirect(
            method = "handleRespawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
                    ordinal = 0
            )
    )
    private void coraline$interceptRespawnScreen(
            Minecraft mc, Screen screen)
    {
        if (PortalTransitionContext.hasTransition())
            mc.setScreen(new TranscendingScreen()); // Context is finally consumed here!
        else
            mc.setScreen(screen);
    }

    @Inject(
            method = "handleSetSpawn",
            at = @At("TAIL")
    )
    private void coraline$forwardLoadingPackets(
            ClientboundSetDefaultSpawnPositionPacket packet, CallbackInfo ci)
    {
        if (this.minecraft.screen instanceof TranscendingScreen ts)
            ts.loadingPacketsReceived();
    }
}