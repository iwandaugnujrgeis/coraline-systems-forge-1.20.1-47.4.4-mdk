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

            // 1. RANDOM HEAD ROTATION (The "Twitch")
            // We use ageInTicks to create a fast, jittery movement
            float headJitterX = Mth.sin(ageInTicks * 0.8F) * 0.15F;
            float headJitterY = Mth.cos(ageInTicks * 0.7F) * 0.2F;

            // Occasionally "snap" the head to a random angle
            if (helper.getRandom().nextFloat() < 0.05F) {
                this.head.zRot = (helper.getRandom().nextFloat() - 0.5F) * 0.4F;
            }

            this.head.xRot += headJitterX;
            this.head.yRot += headJitterY;

            // 2. CRAZY ARM SWING
            // We're increasing the frequency from 0.6662 to 1.2 for "double time"
            // And keeping the 2.0 amplitude for that wide Classic look
            float crazySwing = limbSwing * 0.6F;

            this.rightArm.xRot = Mth.cos(crazySwing + (float)Math.PI) * 2.5F * limbSwingAmount;
            this.leftArm.xRot = Mth.cos(crazySwing) * 2.5F * limbSwingAmount;

            // 3. THE "FLAIL" EFFECT (Z-Axis Chaos)
            // This makes the arms move slightly outward and inward randomly while swinging
            this.rightArm.zRot = Mth.sin(ageInTicks * 0.4F) * 0.1F + 0.1F;
            this.leftArm.zRot = -(Mth.sin(ageInTicks * 0.4F) * 0.1F + 0.1F);
        }
    }
}