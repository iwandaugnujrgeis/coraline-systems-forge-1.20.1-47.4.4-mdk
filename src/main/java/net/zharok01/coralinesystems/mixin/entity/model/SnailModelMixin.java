package net.zharok01.coralinesystems.mixin.entity.model;

import com.ninni.spawn.client.model.SnailModel;
import com.ninni.spawn.client.model.animation.SnailAnimations;
import com.ninni.spawn.server.entity.Snail;
import net.minecraft.client.model.AgeableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnailModel.class)
public abstract class SnailModelMixin extends AgeableHierarchicalModel<Snail> {

    @Shadow @Final private ModelPart leftEyeStalk;
    @Shadow @Final private ModelPart rightEyeStalk;
    @Shadow @Final private ModelPart shell;

    protected SnailModelMixin(float youngScaleFactor, float bodyYOffset) {
        super(youngScaleFactor, bodyYOffset);
    }

    @Inject(
            method = "setupAnim(Lcom/ninni/spawn/server/entity/Snail;FFFFF)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void simplifySetupAnim(Snail entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, CallbackInfo ci) {
        ci.cancel();

        this.root().getAllParts().forEach(ModelPart::resetPose);

        float pi = (float) Math.PI;

        this.shell.visible = entity.getShellGrowthTicks() == 0;

        this.leftEyeStalk.xRot = headPitch * pi / 180.0F;
        this.leftEyeStalk.yRot = headYaw * pi / 180.0F;
        this.rightEyeStalk.xRot = headPitch * pi / 180.0F;
        this.rightEyeStalk.yRot = headYaw * pi / 180.0F;

        this.animate(entity.hideAnimationState, SnailAnimations.HIDE, ageInTicks);
        this.animate(entity.shakeAnimationState, SnailAnimations.SHAKE, ageInTicks);
        this.animate(entity.stopHidingAnimationState, SnailAnimations.STOP_HIDING, ageInTicks);
        this.animate(entity.pinchedLeftAnimationState, SnailAnimations.PINCHED_LEFT, ageInTicks);
        this.animate(entity.pinchedRightAnimationState, SnailAnimations.PINCHED_RIGHT, ageInTicks);

        if (this.young) {
            this.applyStatic(SnailAnimations.BABY_PROPORTIONS);
        }

        if (entity.getPose() == Pose.CROUCHING && !entity.hideAnimationState.isStarted() && !entity.stopHidingAnimationState.isStarted()) {
            this.applyStatic(SnailAnimations.HIDING);
        }
    }
}