package net.zharok01.coralinesystems.mixin.entity.model;

import net.minecraft.client.animation.definitions.CamelAnimation;
import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.camel.Camel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CamelModel.class)
public abstract class CamelModelMixin<T extends Camel> extends HierarchicalModel<T> {

    @Unique private ModelPart coraline$leftHindLeg;
    @Unique private ModelPart coraline$rightHindLeg;
    @Unique private ModelPart coraline$leftFrontLeg;
    @Unique private ModelPart coraline$rightFrontLeg;

    @Shadow
    private void applyHeadRotation(T entity, float netHeadYaw, float headPitch, float ageInTicks) {}

    @Shadow
    private void toggleInvisibleParts(T entity) {}
    
    @Inject(method = "<init>", at = @At("TAIL"))
    private void cacheLimbParts(ModelPart root, CallbackInfo ci) {
        this.coraline$leftHindLeg = root.getChild("left_hind_leg");
        this.coraline$rightHindLeg = root.getChild("right_hind_leg");
        this.coraline$leftFrontLeg = root.getChild("left_front_leg");
        this.coraline$rightFrontLeg = root.getChild("right_front_leg");
    }
    
    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/animal/camel/Camel;FFFFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void overrideSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ci.cancel();

        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.applyHeadRotation(entity, netHeadYaw, headPitch, ageInTicks);
        this.toggleInvisibleParts(entity);

        this.coraline$leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
        this.coraline$rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.2F * limbSwingAmount;
        this.coraline$leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.2F * limbSwingAmount;
        this.coraline$rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;

        this.animate(entity.sitAnimationState, CamelAnimation.CAMEL_SIT, ageInTicks, 1.0F);
        this.animate(entity.sitPoseAnimationState, CamelAnimation.CAMEL_SIT_POSE, ageInTicks, 1.0F);
        this.animate(entity.sitUpAnimationState, CamelAnimation.CAMEL_STANDUP, ageInTicks, 1.0F);
        this.animate(entity.dashAnimationState, CamelAnimation.CAMEL_DASH, ageInTicks, 1.0F);
    }
}