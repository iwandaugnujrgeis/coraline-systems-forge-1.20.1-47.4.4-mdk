package net.zharok01.coralinesystems.mixin;

import com.legacy.structure_gel.api.block.GelPortalBlock;
import com.legacy.structure_gel.core.capability.entity.GelEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.client.TranscendingPortalOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin
{
    @Final @Shadow private Minecraft minecraft;

    // ── 1. Snapshot portal type at HEAD of handleRespawn ─────────────────────
    //
    // We must read GelEntity.getPortalClient() and player.spinningEffectIntensity
    // HERE, before handleRespawn rebuilds the player entity — after that point
    // both values are gone.

    @Inject(
            method = "handleRespawn",
            at = @At("HEAD")
    )
    private void coraline$snapshotPortalOnRespawn(
            ClientboundRespawnPacket packet, CallbackInfo ci)
    {
        // Only act on a genuine dimension-change respawn (not a plain death respawn)
        // handleRespawn already has that dimension-key guard internally, but we can
        // do an early-out here too: if the player is null there is nothing to detect.
        if (this.minecraft.player == null) return;

        GelPortalBlock gelPortal = GelEntity.getPortalClient();

        if (gelPortal != null)
        {
            // Gel portal (Skylands or any other Structure Gel portal)
            TranscendingPortalOverlay.beginTransition(gelPortal);
        }
        else if (this.minecraft.player.spinningEffectIntensity > 0)
        {
            // Vanilla Nether portal — no GelPortalBlock exists, pass null as sentinel
            TranscendingPortalOverlay.beginTransition(null);
        }
        // If neither condition is true this is a death/End respawn — do nothing.
    }

    // ── 2. Suppress ReceivingLevelScreen during a portal transition ───────────
    //
    // handleRespawn calls minecraft.setScreen(new ReceivingLevelScreen()) once
    // when the dimension actually changes.  We redirect that single call: if our
    // overlay is active we swallow the screen push; otherwise we let it through
    // unchanged so first-join and non-portal respawns still work normally.

    @Redirect(
            method = "handleRespawn",
            at = @At(
                    value  = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
                    ordinal = 0
            )
    )
    private void coraline$suppressReceivingLevelScreen(Minecraft mc, Screen screen)
    {
        if (TranscendingPortalOverlay.isActive() && screen instanceof ReceivingLevelScreen)
        {
            // Deliberately do nothing — the overlay is handling the visual cover.
            return;
        }
        mc.setScreen(screen);
    }

    // ── 3. Signal "loading packets received" to the overlay ──────────────────
    //
    // The vanilla ReceivingLevelScreen uses handleSetSpawn to know that the
    // server has sent enough data to start watching for compiled chunks.
    // We forward the same signal to the overlay's chunk-readiness logic so
    // the overlay never fades out before packets have arrived.

    @Inject(
            method = "handleSetSpawn",
            at = @At("TAIL")
    )
    private void coraline$notifyOverlayPacketsReceived(
            ClientboundSetDefaultSpawnPositionPacket packet, CallbackInfo ci)
    {
        TranscendingPortalOverlay.notifyLoadingPacketsReceived();
    }
}