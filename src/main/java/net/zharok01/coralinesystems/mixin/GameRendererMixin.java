package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.server.IntegratedServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Optional;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow protected abstract void takeAutoScreenshot(Path path);

    @Unique
    private boolean coralineSystems$hasUpdatedScreenshot = false;

    /**
     * Redirects the initial check for `hasWorldScreenshot`.
     * By using our custom flag, the game will keep calling `tryTakeScreenshotIfNeeded`
     * every second until we successfully capture the new icon.
     */
    @Redirect(
            method = "tryTakeScreenshotIfNeeded",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;hasWorldScreenshot:Z", opcode = Opcodes.GETFIELD)
    )
    private boolean redirectHasWorldScreenshot(GameRenderer instance) {
        return this.coralineSystems$hasUpdatedScreenshot;
    }

    /**
     * Intercepts the call to get the screenshot path.
     * We consume the path manually to invoke `takeAutoScreenshot` directly, bypassing
     * the vanilla file-existence check. Returning Optional.empty() stops the vanilla lambda.
     */
    @Redirect(
            method = "tryTakeScreenshotIfNeeded",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;getWorldScreenshotFile()Ljava/util/Optional;")
    )
    private Optional<Path> redirectGetWorldScreenshotFile(IntegratedServer instance) {
        instance.getWorldScreenshotFile().ifPresent(this::takeAutoScreenshot);
        return Optional.empty();
    }

    /**
     * Detects when the screenshot is actually successfully taken and saved.
     * This is only reached once chunks are fully rendered.
     */
    @Inject(
            method = "takeAutoScreenshot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;takeScreenshot(Lcom/mojang/blaze3d/pipeline/RenderTarget;)Lcom/mojang/blaze3d/platform/NativeImage;")
    )
    private void onScreenshotTaken(Path path, CallbackInfo ci) {
        this.coralineSystems$hasUpdatedScreenshot = true;
    }

    /**
     * Resets the flag when joining a new world or exiting to the main menu.
     */
    @Inject(
            method = "resetData",
            at = @At("HEAD")
    )
    private void onResetData(CallbackInfo ci) {
        this.coralineSystems$hasUpdatedScreenshot = false;
    }
}