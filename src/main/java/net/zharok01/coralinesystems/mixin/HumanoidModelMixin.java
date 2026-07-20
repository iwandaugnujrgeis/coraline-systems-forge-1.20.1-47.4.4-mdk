package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.client.CoralineClientAnimationTypes;
import net.zharok01.coralinesystems.zipline.ZiplineHandler;
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
    @Shadow public HumanoidModel.ArmPose rightArmPose;
    @Shadow public HumanoidModel.ArmPose leftArmPose;

    // --- ZIPLINE LOGIC (Existing) ---
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;copyFrom(Lnet/minecraft/client/model/geom/ModelPart;)V"))
    void poseZiplineArm(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player) {
            // NOTE: Assuming ZiplineHandler exists in your workspace per your previous code!
            if (!ZiplineHandler.ZIPLINING_PLAYERS.containsKey(player.getUUID())) {
                return;
            }
            HumanoidArm mainArm = player.getMainArm();
            theCoralineSystems$positionArm(theCoralineSystems$getArmModel(mainArm));
        }
    }

    @Unique
    ModelPart theCoralineSystems$getArmModel(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? rightArm : leftArm;
    }

    @Unique
    void theCoralineSystems$positionArm(ModelPart arm) {
        arm.xRot = (float) (-Math.PI * 0.95f);
        arm.yRot = 0.0F;
        arm.zRot = 0.0F;
        arm.y = 2.0F;
        arm.z = 0.0F;
    }

    // --- SWORD BLOCKING LOGIC ---
    @Inject(method = "poseRightArm", at = @At(value = "HEAD"), cancellable = true)
    private void coralinesystems$poseRightBlock(T livingEntity, CallbackInfo info) {
        if (this.rightArmPose == CoralineClientAnimationTypes.SWORD_BLOCK_POSE) {
            CoralineClientAnimationTypes.renderSwordBlockArm(livingEntity.getMainArm(), this.rightArm);
            info.cancel();
        }
    }

    @Inject(method = "poseLeftArm", at = @At(value = "HEAD"), cancellable = true)
    private void coralinesystems$poseLeftBlock(T livingEntity, CallbackInfo info) {
        if (this.leftArmPose == CoralineClientAnimationTypes.SWORD_BLOCK_POSE) {
            CoralineClientAnimationTypes.renderSwordBlockArm(livingEntity.getMainArm(), this.leftArm);
            info.cancel();
        }
    }

    @Inject(method = "setupAttackAnimation", at = @At(value = "HEAD"), cancellable = true)
    private void coralinesystems$cancelAttackAnimWhenBlocking(T livingEntity, float ageInTicks, CallbackInfo info) {
        if (livingEntity.getMainArm() == HumanoidArm.RIGHT && this.rightArmPose == CoralineClientAnimationTypes.SWORD_BLOCK_POSE) {
            info.cancel();
        } else if (livingEntity.getMainArm() == HumanoidArm.LEFT && this.leftArmPose == CoralineClientAnimationTypes.SWORD_BLOCK_POSE) {
            info.cancel();
        }
    }
}