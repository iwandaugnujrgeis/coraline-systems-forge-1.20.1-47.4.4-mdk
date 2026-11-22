package net.zharok01.coralinesystems.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.zharok01.coralinesystems.util.interfaces.SneakAccessor;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class CrosshairRenderMixin {

    @Shadow @Final protected Minecraft minecraft;

    @Unique private static final float SNEAK_MAX_DEGREES = 180.0F;
    @Unique private static final float EAT_SPIN_DEGREES_PER_TICK = 35.0F;
    @Unique private static final float EAT_SPIN_RETURN_RATE = 0.35F;
    @Unique private static final float EAT_SPIN_SNAP_EPSILON = 0.05F;

    @Unique private float coraline$eatSpinAngle = 0.0F;
    @Unique private boolean coraline$poseWasPushed = false;
    @Unique private final Quaternionf coraline$rotationScratch = new Quaternionf();

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void coraline$pushCrosshairTransform(GuiGraphics guiGraphics, CallbackInfo ci) {
        Player player = this.minecraft.player;
        if (player == null) {
            coraline$poseWasPushed = false;
            return;
        }

        if (player.isScoping()) {
            coraline$poseWasPushed = false;
            ci.cancel();
            return;
        }

        float partialTick = this.minecraft.getFrameTime();
        float deltaTime = this.minecraft.getDeltaFrameTime();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        coraline$poseWasPushed = true;

        int renderX = (guiGraphics.guiWidth() - 15) / 2;
        int renderY = (guiGraphics.guiHeight() - 15) / 2;

        float centerX = renderX + 7.5F;
        float centerY = renderY + 7.5F;

        float angleDegrees = 0.0F;

        if (player instanceof SneakAccessor sneakAccessor) {
            angleDegrees -= sneakAccessor.coraline$getSneakAmount(partialTick) * SNEAK_MAX_DEGREES;
        }

        boolean isEatingOrDrinking = false;
        if (player.isUsingItem()) {
            UseAnim useAnim = player.getUseItem().getUseAnimation();
            if (useAnim == UseAnim.EAT || useAnim == UseAnim.DRINK) {
                isEatingOrDrinking = true;
            }
        }

        if (isEatingOrDrinking) {
            coraline$eatSpinAngle = (coraline$eatSpinAngle + EAT_SPIN_DEGREES_PER_TICK * deltaTime) % 360.0F;
        } else if (coraline$eatSpinAngle != 0.0F) {
            float remaining = Mth.wrapDegrees(-coraline$eatSpinAngle);
            coraline$eatSpinAngle += remaining * Mth.clamp(EAT_SPIN_RETURN_RATE * deltaTime, 0.0F, 1.0F);
            if (Math.abs(Mth.wrapDegrees(coraline$eatSpinAngle)) < EAT_SPIN_SNAP_EPSILON) {
                coraline$eatSpinAngle = 0.0F;
            }
        }

        angleDegrees += coraline$eatSpinAngle;

        pose.translate(centerX, centerY, 0.0F);
        pose.mulPose(coraline$rotationScratch.rotationZ((float) Math.toRadians(angleDegrees)));
        pose.translate(-centerX, -centerY, 0.0F);
    }

    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    private void coraline$popCrosshairTransform(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (coraline$poseWasPushed) {
            guiGraphics.pose().popPose();
            coraline$poseWasPushed = false;
        }
    }
}