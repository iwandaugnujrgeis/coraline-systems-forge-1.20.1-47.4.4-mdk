package net.zharok01.coralinesystems.mixin.entity.model;

import com.ninni.spawn.client.model.HamsterModel;
import com.ninni.spawn.server.entity.Hamster;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HamsterModel.class)
public abstract class HamsterModelMixin<E extends Hamster> {

    @Shadow @Final private ModelPart root;
    @Shadow @Final private ModelPart all;
    @Shadow @Final private ModelPart body;
    @Shadow @Final private ModelPart rightLeg;
    @Shadow @Final private ModelPart rightArm;
    @Shadow @Final private ModelPart rightCheek;
    @Shadow @Final private ModelPart leftLeg;
    @Shadow @Final private ModelPart leftArm;
    @Shadow @Final private ModelPart leftCheek;
    @Shadow @Final private ModelPart rightEar;
    @Shadow @Final private ModelPart leftEar;

    @Inject(
            method = "setupAnim(Lcom/ninni/spawn/server/entity/Hamster;FFFFF)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void simplifySetupAnim(E hamster, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        ci.cancel();

        this.root.getAllParts().forEach(ModelPart::resetPose);

        float speed = 1.0F;
        float pi = (float) Math.PI;
        float puffTicks = hamster.getPuffTicks();
        boolean standing = hamster.isStanding() || hamster.hasEatenTnt();
        boolean inWheel = hamster.isInWheel();

        this.root.y = !standing && !hamster.isBaby() ? -4.0F : -3.0F;

        float cheekScale = 2.0F * puffTicks;
        this.leftCheek.xScale = cheekScale;
        this.leftCheek.zScale = cheekScale;
        this.leftCheek.yScale = 1.5F * puffTicks;

        this.rightCheek.xScale = cheekScale;
        this.rightCheek.zScale = cheekScale;
        this.rightCheek.yScale = 1.5F * puffTicks;

        if (inWheel) {
            this.root.y = 24.0F;

            this.rightLeg.xRot = Mth.cos(animationProgress * 1.0F) * 0.45F;
            this.leftLeg.xRot = Mth.cos(animationProgress * 1.0F + pi) * 0.45F;
            this.rightArm.xRot = Mth.cos(animationProgress * 1.0F + pi) * 0.45F;
            this.leftArm.xRot = Mth.cos(animationProgress * 1.0F) * 0.45F;

            this.leftEar.yRot = 0.0F;
            this.rightEar.yRot = 0.3F;
        } else {
            this.body.y = 0.0F;
            this.body.yRot = 0.0F;

            this.rightLeg.xRot = Mth.cos(limbAngle * 0.6F * speed) * 1.4F * limbDistance;
            this.leftLeg.xRot = Mth.cos(limbAngle * 0.6F * speed + pi) * 1.4F * limbDistance;
            this.rightArm.xRot = Mth.cos(limbAngle * 0.6F * speed + pi) * 1.4F * limbDistance + (standing ? -0.3927F : 0.0F);
            this.leftArm.xRot = Mth.cos(limbAngle * 0.6F * speed) * 1.4F * limbDistance + (standing ? -0.3927F : 0.0F);

            this.rightLeg.y = standing ? 2.0F : 3.0F;
            this.leftLeg.y = standing ? 2.0F : 3.0F;
            this.rightArm.y = standing ? -1.0F : 3.0F;
            this.leftArm.y = standing ? -1.0F : 3.0F;
        }
    }
}