package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HelperAnimationMixin {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    private void coraline$applyCrazyJamming(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof HelperEntity helper && helper.isJamming()) {

            //Random head ("twitching") rotation:
            float headJitterX = Mth.sin(ageInTicks * 0.8F) * 0.15F;
            float headJitterY = Mth.cos(ageInTicks * 0.7F) * 0.2F;

            if (helper.getRandom().nextFloat() < 0.05F) {
                this.head.zRot = (helper.getRandom().nextFloat() - 0.5F) * 0.4F;
            }

            this.head.xRot += headJitterX;
            this.head.yRot += headJitterY;

            //Increase arm swing:
            float crazySwing = limbSwing * 0.6F;
            this.rightArm.xRot = Mth.cos(crazySwing + (float)Math.PI) * 2.5F * limbSwingAmount;
            this.leftArm.xRot = Mth.cos(crazySwing) * 2.5F * limbSwingAmount;

        } else if (entity instanceof HelperEntity) {

            //If it's a Helper, but they are NOT jamming, reset the tilt.
            //This prevents the "permanent broken neck" look after the music stops.
            this.head.zRot = 0.0F;
        }
    }
}