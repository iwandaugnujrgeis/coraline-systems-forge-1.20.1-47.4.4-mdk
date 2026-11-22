package net.zharok01.coralinesystems.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.server.IntegratedServer;
import net.zharok01.coralinesystems.mixin.accessor.LightTextureAccessor;
import net.zharok01.coralinesystems.worldgen.CoralineTrueDarkness;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow protected abstract void takeAutoScreenshot(Path path);

    @Unique
    private boolean coraline$hasUpdatedScreenshot = false;

    @Redirect(
            method = "tryTakeScreenshotIfNeeded",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;hasWorldScreenshot:Z", opcode = Opcodes.GETFIELD)
    )
    private boolean redirectHasWorldScreenshot(GameRenderer instance) {
        return this.coraline$hasUpdatedScreenshot;
    }

    @Redirect(
            method = "tryTakeScreenshotIfNeeded",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;getWorldScreenshotFile()Ljava/util/Optional;")
    )
    private Optional<Path> redirectGetWorldScreenshotFile(IntegratedServer instance) {
        instance.getWorldScreenshotFile().ifPresent(this::takeAutoScreenshot);
        return Optional.empty();
    }

    @Inject(
            method = "takeAutoScreenshot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;takeScreenshot(Lcom/mojang/blaze3d/pipeline/RenderTarget;)Lcom/mojang/blaze3d/platform/NativeImage;")
    )
    private void onScreenshotTaken(Path path, CallbackInfo ci) {
        this.coraline$hasUpdatedScreenshot = true;
    }

    @Inject(method = "resetData", at = @At("HEAD"))
    private void onResetData(CallbackInfo ci) {
        this.coraline$hasUpdatedScreenshot = false;
    }

    @Shadow @Final @Nullable Minecraft minecraft;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void coraline$updateDarknessLuminance(float partialTicks, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        GameRenderer self = (GameRenderer) (Object) this;
        LightTexture lightTexture = self.lightTexture();
        LightTextureAccessor accessor = (LightTextureAccessor) lightTexture;

        if (!accessor.coraline$isLightTextureDirty()) return;

        assert this.minecraft != null;
        CoralineTrueDarkness.updateLuminance(
                partialTicks,
                this.minecraft,
                self,
                accessor.coraline$getBlockLightRedFlicker()
        );
    }








}