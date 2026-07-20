package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.client.CoralineTranscendingPortalOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@OnlyIn(Dist.CLIENT)

@Mixin(Minecraft.class)
public abstract class MinecraftMixin
{
    /**
     * Suppresses the "Joining world…" ProgressScreen during a portal transition.
     *
     * setLevel() calls updateScreenAndTick(new ProgressScreen(...)) which in turn
     * calls setScreen(progressScreen) — this would flash the dirt background and
     * "Joining world…" text over our portal overlay.
     *
     * We redirect that single setScreen call inside updateScreenAndTick: if our
     * overlay is currently active AND the incoming screen is a ProgressScreen, we
     * swallow it.  Every other caller of updateScreenAndTick (clearLevel on
     * disconnect, etc.) passes through unchanged because isActive() will be false.
     *
     * Note: the other work inside updateScreenAndTick — soundManager.stop(),
     * cameraEntity = null, pendingConnection = null — still executes normally
     * because we are only redirecting the setScreen call, not cancelling the
     * entire method.
     */
    @Redirect(
            method = "updateScreenAndTick",
            at = @At(
                    value  = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"
            )
    )
    private void coraline$suppressJoiningWorldScreen(Minecraft mc, Screen screen)
    {
        if (CoralineTranscendingPortalOverlay.isActive() && screen instanceof ProgressScreen)
        {
            // Deliberately do nothing — the overlay covers the screen visually
            // and the side-effects we need (soundManager.stop etc.) have already
            // run earlier in updateScreenAndTick before this call.
            return;
        }
        mc.setScreen(screen);
    }
}