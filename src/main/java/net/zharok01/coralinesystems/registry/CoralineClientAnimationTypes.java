package net.zharok01.coralinesystems.registry;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CoralineClientAnimationTypes {

    public static final HumanoidModel.ArmPose SWORD_BLOCK_POSE = HumanoidModel.ArmPose.create("CORALINE_SWORD_BLOCK", false, (model, entity, arm) -> {});

    public static void renderSwordBlockArm(HumanoidArm armType, ModelPart armPart) {
        armPart.xRot = armPart.xRot * 0.5F - 0.9424778F;
        armPart.yRot = (armType == HumanoidArm.RIGHT ? 1 : -1) * -0.5235988F;
    }
}