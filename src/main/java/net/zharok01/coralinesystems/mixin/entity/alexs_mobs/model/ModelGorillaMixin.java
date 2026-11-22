package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelGorilla;
import com.github.alexthe666.alexsmobs.entity.EntityGorilla;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import net.zharok01.coralinesystems.util.access.GorillaPickupAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModelGorilla.class, remap = false)
public class ModelGorillaMixin {

    @Unique private static final float WALK_SWING_MULTIPLIER = 1.5F;

    @Unique private static final float PICKUP_ARM_PITCH = (float) Math.toRadians(-125.0D);
    @Unique private static final float PICKUP_ARM_YAW = (float) Math.toRadians(-25.0D);
    @Unique private static final float PICKUP_HEAD_PITCH = (float) Math.toRadians(12.0D);

    @Final @Shadow public AdvancedModelBox body;
    @Final @Shadow public AdvancedModelBox head;
    @Final @Shadow public AdvancedModelBox leftArm;
    @Final @Shadow public AdvancedModelBox rightArm;
    @Final @Shadow public AdvancedModelBox leftLeg;
    @Final @Shadow public AdvancedModelBox rightLeg;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityGorilla;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityGorilla;FFFFF)V"
            },
            at = @At("RETURN")
    )
    private void coraline$anchorGorillaLimbSockets(EntityGorilla entity, float limbSwing,
                                                   float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                                                   CallbackInfo ci) {
        if (entity.prevStandProgress == 0.0F && entity.standProgress == 0.0F &&
                entity.prevSitProgress == 0.0F && entity.sitProgress == 0.0F) {

            this.body.rotationPointY = this.body.defaultPositionY;
            this.leftLeg.rotationPointY = this.leftLeg.defaultPositionY;
            this.rightLeg.rotationPointY = this.rightLeg.defaultPositionY;
            this.leftArm.rotationPointY = this.leftArm.defaultPositionY;
            this.rightArm.rotationPointY = this.rightArm.defaultPositionY;
        }
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityGorilla;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityGorilla;FFFFF)V"
            },
            at = @At("RETURN")
    )
    private void coraline$raiseArmToMouth(EntityGorilla entity, float limbSwing,
                                          float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                                          CallbackInfo ci) {
        float partialTick = ageInTicks - entity.tickCount;
        float progress = ((GorillaPickupAccess) entity).coraline$getPickupProgress(partialTick);
        if (progress <= 0.0F) return;

        ModelGorilla model = (ModelGorilla) (Object) this;
        model.progressRotationPrev(this.rightArm, progress, PICKUP_ARM_PITCH, PICKUP_ARM_YAW, 0.0F, GorillaPickupAccess.POSE_TICKS);
        model.progressRotationPrev(this.head, progress, PICKUP_HEAD_PITCH, 0.0F, 0.0F, GorillaPickupAccess.POSE_TICKS);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityGorilla;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityGorilla;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelGorilla;walk(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V"
            )
    )
    private void coraline$amplifyGorillaWalkSwing(ModelGorilla instance, AdvancedModelBox box, float speed, float degree,
                                                  boolean invert, float offset, float weight,
                                                  float walk, float walkAmount) {
        if (box == this.leftLeg || box == this.rightLeg || box == this.leftArm || box == this.rightArm) {
            instance.walk(box, speed, degree * WALK_SWING_MULTIPLIER, invert, offset, weight, walk, walkAmount);
        }
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityGorilla;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityGorilla;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelGorilla;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V"
            )
    )
    private void coraline$cancelGorillaWalkFlap(ModelGorilla instance, AdvancedModelBox box, float speed, float degree,
                                                boolean invert, float offset, float weight, float f, float f1) {
    }
}