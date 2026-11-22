package net.zharok01.coralinesystems.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.event.DualWieldEvents;
import net.zharok01.coralinesystems.registry.CoralineAnimationTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float offHandHeight;

    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private float coraline$swingBothHands(float swingProgress, AbstractClientPlayer player, float partialTicks) {
        return DualWieldEvents.coraline$isDualWielding(player) ? player.getAttackAnim(partialTicks) : swingProgress;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void coraline$pullOffhandWithMainHand(CallbackInfo info) {
        if (DualWieldEvents.coraline$isDualWielding(this.minecraft.player)) {
            this.offHandHeight = Math.min(this.offHandHeight, this.mainHandHeight);
        }
    }

    @Inject(method = "itemUsed", at = @At("TAIL"))
    private void coraline$pullBothHandsOnUse(InteractionHand hand, CallbackInfo info) {
        if (DualWieldEvents.coraline$isDualWielding(this.minecraft.player)) {
            this.mainHandHeight = 0.0F;
            this.offHandHeight = 0.0F;
        }
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void coraline$renderInactiveSwordBlock(AbstractClientPlayer player, float partialTicks, float pitch,
                                                   InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack,
                                                   MultiBufferSource buffer, int combinedLight, CallbackInfo info) {
        if (player.isScoping() || !player.isUsingItem() || hand == player.getUsedItemHand()
                || !DualWieldEvents.coraline$isDualWieldingSwords(player)
                || player.getUseItem().getUseAnimation() != CoralineAnimationTypes.SWORD_BLOCK) {
            return;
        }
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND
                ? player.getMainArm() : player.getMainArm().getOpposite();
        poseStack.pushPose();
        this.coraline$applySwordBlockTransform(poseStack, arm, equippedProgress);
        ((ItemInHandRenderer) (Object) this).renderItem(player, stack,
                arm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                arm == HumanoidArm.LEFT, poseStack, buffer, combinedLight);
        poseStack.popPose();
        info.cancel();
    }
    @Inject(method = "renderArmWithItem",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))

    private void coraline$renderSwordBlock(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo info) {
        if (stack.getUseAnimation() == CoralineAnimationTypes.SWORD_BLOCK) {
            HumanoidArm arm = hand == InteractionHand.MAIN_HAND
                    ? player.getMainArm() : player.getMainArm().getOpposite();
            this.coraline$applySwordBlockTransform(poseStack, arm, equippedProgress);
        }
    }

    @Unique
    private void coraline$applySwordBlockTransform(PoseStack poseStack, HumanoidArm arm, float equippedProgress) {
        int horizontal = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(horizontal * 0.56F, -0.52F + equippedProgress * -0.6F, -0.72F);
        poseStack.translate(horizontal * -0.14142136F, 0.08F, 0.14142136F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
        poseStack.mulPose(Axis.YP.rotationDegrees(horizontal * 13.365F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(horizontal * 78.05F));
    }
}
