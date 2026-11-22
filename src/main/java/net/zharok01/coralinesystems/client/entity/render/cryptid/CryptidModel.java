package net.zharok01.coralinesystems.client.entity.render.cryptid;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.zharok01.coralinesystems.entity.CryptidEntity;
import org.jetbrains.annotations.NotNull;

public class CryptidModel extends HumanoidModel<CryptidEntity> {

    private static final float THROW_ARM_ROTATION = -(float) Math.PI * 0.9F;

    public CryptidModel(ModelPart root) {
        super(root, RenderType::entityTranslucent);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror()
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 15.0F, 4.0F, CubeDeformation.NONE)
                        .mirror(false),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 15.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).mirror()
                        .addBox(-1.9F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, CubeDeformation.NONE)
                        .mirror(false),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.1F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull CryptidEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float throwProgress = entity.getThrowAnimProgress();
        if (throwProgress > 0.0F) {
            float eased = Mth.sin(throwProgress * (float) Math.PI);
            float armRot = THROW_ARM_ROTATION * eased;

            this.leftArm.xRot  = armRot;
            this.rightArm.xRot = armRot;

            this.leftArm.zRot  = 0.0F;
            this.rightArm.zRot = 0.0F;
        }
    }
}