package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelTasmanianDevil;
import com.github.alexthe666.alexsmobs.entity.EntityTasmanianDevil;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.ModelAnimator;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModelTasmanianDevil.class, remap = false)
public class ModelTasmanianDevilMixin {

    @Unique private static final float WALK_SWING_MULTIPLIER = 1.8F;

    @Final @Shadow private AdvancedModelBox tail;
    @Final @Shadow private AdvancedModelBox head;
    @Final @Shadow private AdvancedModelBox body;
    @Final @Shadow private AdvancedModelBox armLeft;
    @Final @Shadow private AdvancedModelBox armRight;
    @Final @Shadow private AdvancedModelBox legLeft;
    @Final @Shadow private AdvancedModelBox legRight;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityTasmanianDevil;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityTasmanianDevil;FFFFF)V"
            },
            at = @At("RETURN")
    )
    private void coraline$anchorLimbSockets(EntityTasmanianDevil entity, float limbSwing,
                                            float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                                            CallbackInfo ci) {
        this.body.rotationPointY = this.body.defaultPositionY;
        this.armLeft.rotationPointX = this.armLeft.defaultPositionX;
        this.armLeft.rotationPointY = this.armLeft.defaultPositionY;
        this.armLeft.rotationPointZ = this.armLeft.defaultPositionZ;
        this.armRight.rotationPointX = this.armRight.defaultPositionX;
        this.armRight.rotationPointY = this.armRight.defaultPositionY;
        this.armRight.rotationPointZ = this.armRight.defaultPositionZ;
        this.legLeft.rotationPointX = this.legLeft.defaultPositionX;
        this.legLeft.rotationPointY = this.legLeft.defaultPositionY;
        this.legLeft.rotationPointZ = this.legLeft.defaultPositionZ;
        this.legRight.rotationPointX = this.legRight.defaultPositionX;
        this.legRight.rotationPointY = this.legRight.defaultPositionY;
        this.legRight.rotationPointZ = this.legRight.defaultPositionZ;

        this.tail.rotateAngleY = this.tail.defaultRotationY + (Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityTasmanianDevil;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityTasmanianDevil;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelTasmanianDevil;walk(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V"
            )
    )
    private void coraline$amplifyWalkSwing(ModelTasmanianDevil instance, AdvancedModelBox box, float speed, float degree,
                                           boolean invert, float offset, float weight,
                                           float walk, float walkAmount) {
        if (box == this.armLeft || box == this.armRight || box == this.legLeft || box == this.legRight) {
            instance.walk(box, speed, degree * WALK_SWING_MULTIPLIER, invert, offset, weight, walk, walkAmount);
        } else {
            instance.walk(box, speed, degree, invert, offset, weight, walk, walkAmount);
        }
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "animate(Lcom/github/alexthe666/citadel/animation/IAnimatedEntity;FFFFF)V",
                    "m_6908_(Lcom/github/alexthe666/citadel/animation/IAnimatedEntity;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/citadel/client/model/ModelAnimator;move(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFF)V"
            )
    )
    private void coraline$groundAttackHeadBob(ModelAnimator animator, AdvancedModelBox box, float x, float y, float z) {
        animator.move(this.head, 0.0F, 0.0F, 1.5F);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityTasmanianDevil;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityTasmanianDevil;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelTasmanianDevil;progressRotationPrev(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFFFF)V",
                    ordinal = 0
            )
    )
    private void coraline$cancelTailSpeedTilt(ModelTasmanianDevil instance, AdvancedModelBox box, float progress, float rotX, float rotY, float rotZ, float divisor) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityTasmanianDevil;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityTasmanianDevil;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelTasmanianDevil;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 5
            )
    )
    private void coraline$cancelIdleTailSwing(ModelTasmanianDevil instance, AdvancedModelBox box, float speed, float degree,
                                              boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityTasmanianDevil;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityTasmanianDevil;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelTasmanianDevil;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V"
            )
    )
    private void coraline$cancelBobbing(ModelTasmanianDevil instance, AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
    }
}