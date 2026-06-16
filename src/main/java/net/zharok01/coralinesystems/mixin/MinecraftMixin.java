package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.Entity;
import net.zharok01.coralinesystems.client.portal.PortalTransitionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin
{
    @Shadow private SoundManager soundManager;
    @Shadow @Nullable public Entity cameraEntity;
    @Shadow @Nullable private Connection pendingConnection;

    /**
     * Intercept the `updateScreenAndTick` call. If it's trying to push the
     * "Joining world..." ProgressScreen and we have a portal transition pending,
     * we simply cancel the call. This safely suppresses the text without
     * prematurely consuming the PortalTransitionContext.
     */
    @Inject(
            method = "updateScreenAndTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$suppressJoiningWorldScreen(Screen screen, CallbackInfo ci)
    {
        if (screen instanceof ProgressScreen && PortalTransitionContext.hasTransition())
        {
            // We must manually perform the critical cleanup steps that
            // updateScreenAndTick normally handles before skipping it.
            if (this.soundManager != null)
            {
                this.soundManager.stop();
            }

            this.cameraEntity = null;
            this.pendingConnection = null;

            // Cancel the method. This prevents setScreen(ProgressScreen)
            // and runTick(false) from executing.
            ci.cancel();
        }
    }
}