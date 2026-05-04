package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.content.zipline.ZiplineHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightArm;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;copyFrom(Lnet/minecraft/client/model/geom/ModelPart;)V"))
    void poseZiplineArm(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {

        if (entity instanceof Player player) {
            // Check if they are in our custom Zipline state!
            if (!ZiplineHandler.ZIPLINING_PLAYERS.containsKey(player.getUUID())) {
                return;
            }

            // Figure out which arm to raise (Defaults to Main Arm for now based on our logic)
            HumanoidArm mainArm = player.getMainArm();
            positionArm(getArmModel(mainArm));
        }
    }

    @Unique
    ModelPart getArmModel(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? rightArm : leftArm;
    }

    @Unique
    void positionArm(ModelPart arm) {
        // -Math.PI is straight up. Multiplying by 0.95 tilts it just slightly forward.
        arm.xRot = (float) (-Math.PI * 0.95f);

        // Zeroing these out completely stops the arm from crossing into the head!
        arm.yRot = 0.0F;
        arm.zRot = 0.0F;

        // Reset the translations to the standard vanilla shoulder socket positions.
        // We leave arm.x alone because the base model already sets the correct left/right width.
        arm.y = 2.0F;
        arm.z = 0.0F;
    }
}