package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelGrizzlyBear;
import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModelGrizzlyBear.class, remap = false)
public class ModelGrizzlyBearMixin {

    @Unique private static final float WALK_SWING_MULTIPLIER = 1.2F;

    @Final @Shadow public AdvancedModelBox body;
    @Final @Shadow public AdvancedModelBox midbody;
    @Final @Shadow public AdvancedModelBox head;
    @Final @Shadow public AdvancedModelBox left_leg;
    @Final @Shadow public AdvancedModelBox right_leg;
    @Final @Shadow public AdvancedModelBox left_arm;
    @Final @Shadow public AdvancedModelBox right_arm;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;FFFFF)V"
            },
            at = @At("RETURN")
    )
    private void coraline$anchorGrizzlyLimbSockets(EntityGrizzlyBear entity, float limbSwing,
                                                   float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                                                   CallbackInfo ci) {

        if (entity.prevStandProgress == 0.0F && entity.standProgress == 0.0F &&
                entity.prevSitProgress == 0.0F && entity.sitProgress == 0.0F) {

            this.body.rotationPointY = this.body.defaultPositionY;
            this.body.rotateAngleX = this.body.defaultRotationX;
            this.body.rotateAngleY = this.body.defaultRotationY;
            this.body.rotateAngleZ = this.body.defaultRotationZ;
            this.midbody.rotateAngleX = this.midbody.defaultRotationX;
            this.midbody.rotateAngleY = this.midbody.defaultRotationY;
            this.midbody.rotateAngleZ = this.midbody.defaultRotationZ;
            this.left_leg.rotationPointY = this.left_leg.defaultPositionY;
            this.right_leg.rotationPointY = this.right_leg.defaultPositionY;
            this.left_arm.rotationPointY = this.left_arm.defaultPositionY;
            this.right_arm.rotationPointY = this.right_arm.defaultPositionY;
        }
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelGrizzlyBear;walk(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V"
            )
    )
    private void coraline$amplifyGrizzlyWalkSwing(ModelGrizzlyBear instance, AdvancedModelBox box, float speed, float degree,
                                                  boolean invert, float offset, float weight,
                                                  float walk, float walkAmount) {

        if (box == this.body || box == this.midbody || box == this.head) {
            return;
        }

        if (box == this.left_leg || box == this.right_leg || box == this.left_arm || box == this.right_arm) {
            degree *= WALK_SWING_MULTIPLIER;
        }

        instance.walk(box, speed, degree, invert, offset, weight, walk, walkAmount);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelGrizzlyBear;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V"
            )
    )
    private void coraline$cancelGrizzlyWalkFlap(ModelGrizzlyBear instance, AdvancedModelBox box, float speed, float degree,
                                                boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelGrizzlyBear;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V"
            )
    )
    private void coraline$cancelGrizzlyBobbing(ModelGrizzlyBear instance, AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
    }
}