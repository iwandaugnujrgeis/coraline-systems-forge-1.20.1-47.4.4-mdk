package net.zharok01.coralinesystems.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientAnimationTypes {

    // Forge natively supports creating custom ArmPoses via IExtensibleEnum.
    // We pass a dummy lambda here because we handle the actual mathematical posing inside our HumanoidModel Mixin.
    public static final HumanoidModel.ArmPose SWORD_BLOCK_POSE = HumanoidModel.ArmPose.create("CORALINE_SWORD_BLOCK", false, (model, entity, arm) -> {});

    /**
     * Standardized mathematical transformation for the blocking arm.
     */
    public static void renderSwordBlockArm(HumanoidArm armType, ModelPart armPart) {
        armPart.xRot = armPart.xRot * 0.5F - 0.9424778F;
        armPart.yRot = (armType == HumanoidArm.RIGHT ? 1 : -1) * -0.5235988F;
    }
}