package net.zharok01.coralinesystems.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
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

@Mixin(Gui.class)
public abstract class CrosshairRenderMixin {

    @Shadow @Final protected Minecraft minecraft;

    @Unique private static final float coralinesystems$SNEAK_MAX_DEGREES = 70.0F;
    @Unique private static final float coralinesystems$AIM_SCALE_BONUS = 0.5F;
    @Unique private static final float coralinesystems$AIM_COUNTER_SCALE = -0.6F;

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void coralinesystems$pushCrosshairTransform(GuiGraphics guiGraphics, CallbackInfo ci) {
        Player player = this.minecraft.player;
        if (player == null) return;

        // 1. We moved your InGameHudMixin logic here!
        // If we cancel here, we return early and NEVER push the pose, keeping the matrix stack safe.
        if (player.isScoping()) {
            ci.cancel();
            return;
        }

        // 2. We grab partialTick directly from the client. No need for convoluted injections!
        float partialTick = this.minecraft.getFrameTime();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        int centerX = guiGraphics.guiWidth() / 2;
        int centerY = guiGraphics.guiHeight() / 2;

        float angleDegrees = 0.0F;
        float scale = 1.0F;
        float counterScale = 1.0F;

        // --- Sneak ---
        if (player instanceof CoralineSneakAccessor sneakAccessor) {
            float sneakAmount = sneakAccessor.coralinesystems$getSneakAmount(partialTick);
            angleDegrees += -sneakAmount * coralinesystems$SNEAK_MAX_DEGREES;
        }

        // --- Eat/Drink + Aim ---
        if (player.isUsingItem()) {
            ItemStack useStack = player.getUseItem();
            UseAnim useAnim = useStack.getUseAnimation();
            float useProgress = coralinesystems$usingItemProgress(player, useStack, partialTick);

            if (useAnim == UseAnim.EAT || useAnim == UseAnim.DRINK) {
                angleDegrees += coralinesystems$eatDrinkAngle(useProgress);
            } else if (useAnim == UseAnim.BOW || useAnim == UseAnim.SPEAR) {
                float eased = coralinesystems$sineEase(useProgress);
                scale *= 1.0F + eased * coralinesystems$AIM_SCALE_BONUS;
                counterScale *= 1.0F + eased * coralinesystems$AIM_COUNTER_SCALE;
            }
        }

        pose.translate(centerX, centerY, 0.0F);
        pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(angleDegrees)));
        pose.scale(scale, scale * counterScale, 1.0F);
        pose.translate(-centerX, -centerY, 0.0F);
    }

    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    private void coralinesystems$popCrosshairTransform(GuiGraphics guiGraphics, CallbackInfo ci) {
        // If we cancelled at HEAD due to scoping, the Mixin framework bypasses RETURN injections.
        // Therefore, this safely only pops if the push actually happened!
        if (this.minecraft.player != null) {
            guiGraphics.pose().popPose();
        }
    }

    @Unique
    private static float coralinesystems$usingItemProgress(LivingEntity entity, ItemStack useStack, float partialTick) {
        int duration = useStack.getUseDuration();
        if (duration <= 0) {
            return 0.0F;
        }
        float ticksUsed = entity.getTicksUsingItem() + partialTick;
        return net.minecraft.util.Mth.clamp(ticksUsed / (float) duration, 0.0F, 1.0F);
    }

    @Unique
    private static float coralinesystems$sineEase(float x) {
        return (float) Math.sin(net.minecraft.util.Mth.clamp(x, 0.0F, 1.0F) * (Math.PI / 2.0));
    }

    @Unique
    private static float coralinesystems$eatDrinkAngle(float progress) {
        if (progress < 0.2F) {
            float local = Math.min(1.0F, progress * 10.0F);
            return coralinesystems$sineEase(local) * 135.0F;
        } else if (progress < 0.9F) {
            float local = (progress - 0.2F) / 0.7F;
            return coralinesystems$sineEase(local) * -30.0F + 45.0F;
        } else {
            float local = Math.min(1.0F, (progress - 0.9F) * 10.0F);
            return coralinesystems$sineEase(local) * -135.0F + 45.0F;
        }
    }
}