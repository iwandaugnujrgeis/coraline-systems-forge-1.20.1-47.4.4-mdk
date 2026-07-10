package net.zharok01.coralinesystems.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.zharok01.coralinesystems.util.CoralineSneakAccessor;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rotates the crosshair for sneaking and eating/drinking.
 * Targets Gui#renderCrosshair directly (not Gui#render) to maintain
 * compatibility with Forge's VanillaGuiOverlay system.
 */
@Mixin(Gui.class)
public abstract class CrosshairRenderMixin {

    @Shadow @Final protected Minecraft minecraft;

    // --- Sneak ---
    @Unique private static final float coralinesystems$SNEAK_MAX_DEGREES = 180.0F;

    // --- Eat / drink spin ---
    @Unique private static final float coralinesystems$EAT_SPIN_DEGREES_PER_TICK = 35.0F;
    @Unique private static final float coralinesystems$EAT_SPIN_RETURN_RATE = 0.35F;
    @Unique private static final float coralinesystems$EAT_SPIN_SNAP_EPSILON = 0.05F;

    @Unique private float coralinesystems$eatSpinAngle = 0.0F;

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void coralinesystems$pushCrosshairTransform(GuiGraphics guiGraphics, CallbackInfo ci) {
        Player player = this.minecraft.player;
        if (player == null) return;

        // If we cancel here, we return early and never push the pose, keeping
        // the matrix stack safe.
        if (player.isScoping()) {
            ci.cancel();
            return;
        }

        float partialTick = this.minecraft.getFrameTime();
        float deltaTime = this.minecraft.getDeltaFrameTime();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        // 0.5F offset accounts for Vanilla's odd-numbered (15x15) sprite integer truncation
        float centerX = guiGraphics.guiWidth() / 2.0F - 0.5F;
        float centerY = guiGraphics.guiHeight() / 2.0F - 0.5F;

        float angleDegrees = 0.0F;

        // --- Sneak ---
        if (player instanceof CoralineSneakAccessor sneakAccessor) {
            float sneakAmount = sneakAccessor.coralinesystems$getSneakAmount(partialTick);
            angleDegrees += -sneakAmount * coralinesystems$SNEAK_MAX_DEGREES;
        }

        // --- Eat/Drink spin ---
        boolean isEatingOrDrinking = false;
        if (player.isUsingItem()) {
            ItemStack useStack = player.getUseItem();
            UseAnim useAnim = useStack.getUseAnimation();

            if (useAnim == UseAnim.EAT || useAnim == UseAnim.DRINK) {
                isEatingOrDrinking = true;
            }
        }

        // Continuous one-direction spin while eating/drinking; springs back to 0 when stopped
        if (isEatingOrDrinking) {
            this.coralinesystems$eatSpinAngle += coralinesystems$EAT_SPIN_DEGREES_PER_TICK * deltaTime;
            this.coralinesystems$eatSpinAngle %= 360.0F;
        } else if (this.coralinesystems$eatSpinAngle != 0.0F) {
            float remaining = Mth.wrapDegrees(-this.coralinesystems$eatSpinAngle);
            float step = remaining * Mth.clamp(coralinesystems$EAT_SPIN_RETURN_RATE * deltaTime, 0.0F, 1.0F);
            this.coralinesystems$eatSpinAngle += step;
            if (Math.abs(Mth.wrapDegrees(this.coralinesystems$eatSpinAngle)) < coralinesystems$EAT_SPIN_SNAP_EPSILON) {
                this.coralinesystems$eatSpinAngle = 0.0F;
            }
        }

        angleDegrees += this.coralinesystems$eatSpinAngle;

        // Apply transformations safely without scale distortions
        pose.translate(centerX, centerY, 0.0F);
        pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(angleDegrees)));
        pose.translate(-centerX, -centerY, 0.0F);
    }

    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    private void coralinesystems$popCrosshairTransform(GuiGraphics guiGraphics, CallbackInfo ci) {
        // Pops strictly if the push occurred (skipped by Mixin if HEAD cancels)
        if (this.minecraft.player != null) {
            guiGraphics.pose().popPose();
        }
    }
}