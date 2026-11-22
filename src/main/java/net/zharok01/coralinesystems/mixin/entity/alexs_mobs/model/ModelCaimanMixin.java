package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelCaiman;
import com.github.alexthe666.alexsmobs.entity.EntityCaiman;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModelCaiman.class, remap = false)
public class ModelCaimanMixin {

    @Unique private static final float WALK_SWING_MULTIPLIER = 2.0F;

    @Final @Shadow private AdvancedModelBox body;
    @Final @Shadow private AdvancedModelBox leftArm;
    @Final @Shadow private AdvancedModelBox leftHand;
    @Final @Shadow private AdvancedModelBox rightArm;
    @Final @Shadow private AdvancedModelBox rightHand;
    @Final @Shadow private AdvancedModelBox leftLeg;
    @Final @Shadow private AdvancedModelBox leftFoot;
    @Final @Shadow private AdvancedModelBox rightLeg;
    @Final @Shadow private AdvancedModelBox rightFoot;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V"
            },
            at = @At("RETURN")
    )
    private void coraline$anchorLimbSockets(EntityCaiman entity, float limbSwing,
                                            float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                                            CallbackInfo ci) {
        this.body.rotationPointY = this.body.defaultPositionY;
        this.leftArm.rotationPointX = this.leftArm.defaultPositionX;
        this.leftArm.rotationPointY = this.leftArm.defaultPositionY;
        this.leftArm.rotationPointZ = this.leftArm.defaultPositionZ;
        this.rightArm.rotationPointX = this.rightArm.defaultPositionX;
        this.rightArm.rotationPointY = this.rightArm.defaultPositionY;
        this.rightArm.rotationPointZ = this.rightArm.defaultPositionZ;
        this.leftLeg.rotationPointX = this.leftLeg.defaultPositionX;
        this.leftLeg.rotationPointY = this.leftLeg.defaultPositionY;
        this.leftLeg.rotationPointZ = this.leftLeg.defaultPositionZ;
        this.rightLeg.rotationPointX = this.rightLeg.defaultPositionX;
        this.rightLeg.rotationPointY = this.rightLeg.defaultPositionY;
        this.rightLeg.rotationPointZ = this.rightLeg.defaultPositionZ;
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelCaiman;walk(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V"
            )
    )
    private void coraline$amplifyWalkSwing(ModelCaiman instance, AdvancedModelBox box, float speed, float degree,
                                           boolean invert, float offset, float weight,
                                           float walk, float walkAmount) {
        if (speed == 1.0F && (box == this.leftArm || box == this.rightArm ||
                box == this.leftLeg || box == this.rightLeg ||
                box == this.leftHand || box == this.rightHand ||
                box == this.leftFoot || box == this.rightFoot)) {
            degree *= WALK_SWING_MULTIPLIER;
        }
        instance.walk(box, speed, degree, invert, offset, weight, walk, walkAmount);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelCaiman;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V",
                    ordinal = 0
            )
    )
    private void coraline$cancelIdleHeadBob(ModelCaiman instance, AdvancedModelBox box,
                                            float speed, float degree, boolean bounce, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelCaiman;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 5
            )
    )
    private void coraline$cancelIdleTail1Swing(ModelCaiman instance, AdvancedModelBox box,
                                               float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelCaiman;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 6
            )
    )
    private void coraline$cancelIdleTail2Swing(ModelCaiman instance, AdvancedModelBox box,
                                               float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelCaiman;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 7
            )
    )
    private void coraline$cancelIdleTail3Swing(ModelCaiman instance, AdvancedModelBox box,
                                               float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelCaiman;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 0
            )
    )
    private void coraline$cancelWalkBodyFlap(ModelCaiman instance, AdvancedModelBox box,
                                             float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelCaiman;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 1
            )
    )
    private void coraline$cancelWalkHeadFlap(ModelCaiman instance, AdvancedModelBox box,
                                             float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelCaiman;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 6
            )
    )
    private void coraline$cancelWalkTailFlap(ModelCaiman instance, AdvancedModelBox box,
                                             float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityCaiman;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelCaiman;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V",
                    ordinal = 2
            )
    )
    private void coraline$cancelWalkHeadBob(ModelCaiman instance, AdvancedModelBox box,
                                            float speed, float degree, boolean bounce, float f, float f1) {
    }
}