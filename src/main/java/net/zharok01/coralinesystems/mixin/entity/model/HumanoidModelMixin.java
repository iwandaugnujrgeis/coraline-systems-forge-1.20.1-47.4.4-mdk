package net.zharok01.coralinesystems.mixin.entity.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.event.DualWieldEvents;
import net.zharok01.coralinesystems.event.zipline.ZiplineHandler;
import net.zharok01.coralinesystems.registry.CoralineAnimationTypes;
import net.zharok01.coralinesystems.registry.CoralineClientAnimationTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart head;
    @Shadow public HumanoidModel.ArmPose rightArmPose;
    @Shadow public HumanoidModel.ArmPose leftArmPose;
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;copyFrom(Lnet/minecraft/client/model/geom/ModelPart;)V"))

    void poseZiplineArm(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player) {
            if (!ZiplineHandler.CLIENT_ZIPLINERS.containsKey(player.getUUID())) {
                return;
            }
            HumanoidArm mainArm = player.getMainArm();
            coraline$positionArm(coraline$getArmModel(mainArm));
        }
    }
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAttackAnimation(Lnet/minecraft/world/entity/LivingEntity;F)V"))

    private void coraline$poseInactiveSwordBlockArm(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player) || !coraline$isSwordBlocking(player)
                || !DualWieldEvents.coraline$isDualWieldingSwords(player)) {
            return;
        }
        HumanoidArm inactiveArm = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? player.getMainArm().getOpposite() : player.getMainArm();
        CoralineClientAnimationTypes.renderSwordBlockArm(inactiveArm,
                inactiveArm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm);
    }

    @Unique
    ModelPart coraline$getArmModel(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? rightArm : leftArm;
    }

    @Unique
    void coraline$positionArm(ModelPart arm) {
        arm.xRot = (float) (-Math.PI * 0.95f);
        arm.yRot = 0.0F;
        arm.zRot = 0.0F;
        arm.y = 2.0F;
        arm.z = 0.0F;
    }

    @Unique
    private static boolean coraline$isSwordBlocking(LivingEntity entity) {
        return entity.isUsingItem() && entity.getUseItem().getUseAnimation() == CoralineAnimationTypes.SWORD_BLOCK;
    }

    @Inject(method = "poseRightArm", at = @At(value = "HEAD"), cancellable = true)
    private void coraline$poseRightBlock(T livingEntity, CallbackInfo info) {
        if (this.rightArmPose == CoralineClientAnimationTypes.SWORD_BLOCK_POSE) {
            CoralineClientAnimationTypes.renderSwordBlockArm(HumanoidArm.RIGHT, this.rightArm);
            info.cancel();
        }
    }

    @Inject(method = "poseLeftArm", at = @At(value = "HEAD"), cancellable = true)
    private void coraline$poseLeftBlock(T livingEntity, CallbackInfo info) {
        if (this.leftArmPose == CoralineClientAnimationTypes.SWORD_BLOCK_POSE) {
            CoralineClientAnimationTypes.renderSwordBlockArm(HumanoidArm.LEFT, this.leftArm);
            info.cancel();
        }
    }

    @Inject(method = "setupAttackAnimation", at = @At(value = "HEAD"), cancellable = true)
    private void coraline$animateAttack(T livingEntity, float ageInTicks, CallbackInfo info) {
        if (coraline$isSwordBlocking(livingEntity)) {
            info.cancel();
            return;
        }
        float attackTime = ((EntityModel<?>) (Object) this).attackTime;
        if (attackTime <= 0.0F || !(livingEntity instanceof Player player)
                || !DualWieldEvents.coraline$isDualWielding(player)) {
            return;
        }
        float swing = 1.0F - attackTime;
        swing *= swing;
        swing *= swing;
        swing = 1.0F - swing;
        float forwardSwing = Mth.sin(swing * (float) Math.PI) * 1.2F
                + Mth.sin(attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
        float inwardYaw = Mth.sin(Mth.sqrt(attackTime) * ((float) Math.PI * 2.0F)) * 0.6F;
        float inwardRoll = Mth.sin(attackTime * (float) Math.PI) * 0.4F;
        this.rightArm.xRot -= forwardSwing;
        this.rightArm.yRot += inwardYaw;
        this.rightArm.zRot -= inwardRoll;
        this.leftArm.xRot -= forwardSwing;
        this.leftArm.yRot -= inwardYaw;
        this.leftArm.zRot += inwardRoll;
        info.cancel();
    }
}
