package net.zharok01.coralinesystems.mixin.entity.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Giant;
import net.zharok01.coralinesystems.entity.GiantJammingAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class GiantJammingAnimationMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void coraline$applyGiantJamming(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                            float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Giant && ((GiantJammingAccess) entity).coraline$isJamming()) {
            this.head.xRot += Mth.sin(ageInTicks * 0.8F) * 0.15F;
            this.head.yRot += Mth.cos(ageInTicks * 0.7F) * 0.2F;

            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount;

            this.rightArm.zRot = (Mth.cos(limbSwing * 0.2312F) + 1.0F) * limbSwingAmount;
            this.leftArm.zRot = (Mth.cos(limbSwing * 0.2812F) - 1.0F) * limbSwingAmount;
        }
    }
}
