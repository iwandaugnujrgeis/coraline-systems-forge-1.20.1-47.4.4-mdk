package net.zharok01.coralinesystems.client.entity.orb;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.zharok01.coralinesystems.entity.OrbEntity;
import org.jetbrains.annotations.NotNull;

public class OrbModel<T extends OrbEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart core;

    public OrbModel(ModelPart root) {
        this.root = root;
        this.core = root.getChild("core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Creating a full-block sized cube (16x16x16) centered perfectly in the hitbox.
        partdefinition.addOrReplaceChild("core", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Creates a subtle floating/bobbing animation
        this.core.y = 8.0F + Mth.cos(ageInTicks * 0.1F) * 2.0F;

        // Spin slowly based on entity ticks
        this.core.yRot = ageInTicks * 0.05F;
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }
}