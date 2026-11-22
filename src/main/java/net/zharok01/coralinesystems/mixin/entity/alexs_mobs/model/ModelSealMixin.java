package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelSeal;
import com.github.alexthe666.alexsmobs.entity.EntitySeal;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import net.zharok01.coralinesystems.util.access.SealPickupAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModelSeal.class, remap = false)
public class ModelSealMixin {

    @Unique private static final float PICKUP_ARM_YAW = (float) Math.toRadians(-115.0D);
    @Unique private static final float PICKUP_HEAD_PITCH = (float) Math.toRadians(10.0D);
    @Unique private static final float PICKUP_ARM_SHIFT_X = 3.5F;
    @Unique private static final float PICKUP_ARM_SHIFT_Y = -1.5F;
    @Unique private static final float PICKUP_ARM_SHIFT_Z = -5.0F;

    @Final @Shadow public AdvancedModelBox rightArm;
    @Final @Shadow public AdvancedModelBox head;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySeal;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySeal;FFFFF)V"
            },
            at = @At("RETURN")
    )
    private void coraline$raiseFlipperToMouth(EntitySeal entity, float limbSwing, float limbSwingAmount,
                                              float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        float partialTick = ageInTicks - entity.tickCount;
        float progress = ((SealPickupAccess) entity).coraline$getPickupProgress(partialTick);
        if (progress <= 0.0F) return;

        ModelSeal model = (ModelSeal) (Object) this;
        model.progressRotationPrev(this.rightArm, progress, 0.0F, PICKUP_ARM_YAW, 0.0F, SealPickupAccess.POSE_TICKS);
        model.progressPositionPrev(this.rightArm, progress, PICKUP_ARM_SHIFT_X, PICKUP_ARM_SHIFT_Y, PICKUP_ARM_SHIFT_Z, SealPickupAccess.POSE_TICKS);
        model.progressRotationPrev(this.head, progress, PICKUP_HEAD_PITCH, 0.0F, 0.0F, SealPickupAccess.POSE_TICKS);
    }
}