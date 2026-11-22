package net.zharok01.coralinesystems.mixin.entity.model;

import net.minecraft.client.animation.definitions.FrogAnimation;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.frog.Frog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FrogModel.class)
public abstract class FrogModelMixin<T extends Frog> extends HierarchicalModel<T> {

    @Unique private ModelPart coraline$leftArm;
    @Unique private ModelPart coraline$rightArm;
    @Unique private ModelPart coraline$leftLeg;
    @Unique private ModelPart coraline$rightLeg;
    @Unique private ModelPart coraline$croakingBody;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void coraline$cacheParts(ModelPart root, CallbackInfo ci) {
        ModelPart rootPart = root.getChild("root");
        ModelPart body = rootPart.getChild("body");

        this.coraline$leftArm = body.getChild("left_arm");
        this.coraline$rightArm = body.getChild("right_arm");
        this.coraline$croakingBody = body.getChild("croaking_body");

        this.coraline$leftLeg = rootPart.getChild("left_leg");
        this.coraline$rightLeg = rootPart.getChild("right_leg");
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/animal/frog/Frog;FFFFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$overrideSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ci.cancel();

        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity.isInWaterOrBubble()) {
            this.coraline$leftArm.xRot = Mth.cos(limbSwing * 0.8F) * 0.6F * limbSwingAmount;
            this.coraline$rightArm.xRot = Mth.cos(limbSwing * 0.8F + (float) Math.PI) * 0.6F * limbSwingAmount;
            this.coraline$leftLeg.xRot = Mth.cos(limbSwing * 0.8F + (float) Math.PI) * 0.8F * limbSwingAmount;
            this.coraline$rightLeg.xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
        } else {
            this.coraline$leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
            this.coraline$rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.8F * limbSwingAmount;
            this.coraline$leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.0F * limbSwingAmount;
            this.coraline$rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
        }

        this.animate(entity.jumpAnimationState, FrogAnimation.FROG_JUMP, ageInTicks);
        this.animate(entity.croakAnimationState, FrogAnimation.FROG_CROAK, ageInTicks);
        this.animate(entity.tongueAnimationState, FrogAnimation.FROG_TONGUE, ageInTicks);
        this.animate(entity.swimIdleAnimationState, FrogAnimation.FROG_IDLE_WATER, ageInTicks);

        this.coraline$croakingBody.visible = entity.croakAnimationState.isStarted();
    }
}