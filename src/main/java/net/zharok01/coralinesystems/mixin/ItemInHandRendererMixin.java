package net.zharok01.coralinesystems.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.util.AnimationTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equippedProgress);

    @Inject(method = "renderArmWithItem",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
    private void coralinesystems$renderSwordBlock(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo info) {
        if (stack.getUseAnimation() == AnimationTypes.SWORD_BLOCK) {
            boolean isMainHand = (hand == InteractionHand.MAIN_HAND);
            HumanoidArm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();

            this.applyItemArmTransform(poseStack, arm, equippedProgress);

            int horizontal = arm == HumanoidArm.RIGHT ? 1 : -1;
            poseStack.translate(horizontal * -0.14142136F, 0.08F, 0.14142136F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
            poseStack.mulPose(Axis.YP.rotationDegrees(horizontal * 13.365F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(horizontal * 78.05F));
        }
    }
}