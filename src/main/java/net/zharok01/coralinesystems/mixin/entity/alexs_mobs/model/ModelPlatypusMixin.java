package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelPlatypus;
import com.github.alexthe666.alexsmobs.entity.EntityPlatypus;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModelPlatypus.class, remap = false)
public class ModelPlatypusMixin {

    @Unique private static final float LAND_WALK_SWING_MULTIPLIER = 1.8F;

    @Final @Shadow private AdvancedModelBox body;
    @Final @Shadow private AdvancedModelBox head;
    @Final @Shadow private AdvancedModelBox beak;
    @Final @Shadow private AdvancedModelBox arm_left;
    @Final @Shadow private AdvancedModelBox arm_right;
    @Final @Shadow private AdvancedModelBox leg_left;
    @Final @Shadow private AdvancedModelBox leg_right;
    @Final @Shadow private AdvancedModelBox tail;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityPlatypus;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityPlatypus;FFFFF)V"
            },
            at = @At("RETURN")
    )
    private void coraline$anchorPlatypusLimbSockets(EntityPlatypus entity, float limbSwing,
                                                    float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                                                    CallbackInfo ci) {
        this.body.rotationPointY = this.body.defaultPositionY;

        if (entity.prevInWaterProgress == 0.0F && entity.inWaterProgress == 0.0F) {

            this.arm_left.rotationPointY = this.arm_left.defaultPositionY;
            this.arm_right.rotationPointY = this.arm_right.defaultPositionY;
            this.leg_left.rotationPointY = this.leg_left.defaultPositionY;
            this.leg_right.rotationPointY = this.leg_right.defaultPositionY;

            this.arm_left.rotateAngleX = this.arm_left.defaultRotationX;
            this.arm_left.rotateAngleZ = this.arm_left.defaultRotationZ;

            this.arm_right.rotateAngleX = this.arm_right.defaultRotationX;
            this.arm_right.rotateAngleZ = this.arm_right.defaultRotationZ;

            this.leg_left.rotateAngleX = this.leg_left.defaultRotationX;
            this.leg_left.rotateAngleZ = this.leg_left.defaultRotationZ;

            this.leg_right.rotateAngleX = this.leg_right.defaultRotationX;
            this.leg_right.rotateAngleZ = this.leg_right.defaultRotationZ;

            this.tail.rotateAngleX = this.tail.defaultRotationX;
            this.tail.rotateAngleZ = this.tail.defaultRotationZ;
            this.tail.rotateAngleY = this.tail.defaultRotationY + (Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount);
        }
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityPlatypus;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityPlatypus;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelPlatypus;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V"
            )
    )
    private void coraline$modifyPlatypusSwing(ModelPlatypus instance, AdvancedModelBox box, float speed, float degree,
                                              boolean invert, float offset, float weight, float f, float f1) {
        if (this.beak.getParent() == this.head && (box == this.body || box == this.head)) {
            return;
        }

        if (box == this.arm_left || box == this.arm_right || box == this.leg_left || box == this.leg_right) {
            degree *= LAND_WALK_SWING_MULTIPLIER;
        }

        instance.swing(box, speed, degree, invert, offset, weight, f, f1);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityPlatypus;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityPlatypus;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelPlatypus;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V"
            )
    )
    private void coraline$cancelLandBobbing(ModelPlatypus instance, AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
    }
}