package net.zharok01.coralinesystems.client.entity.orb;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
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

        // An 11x11x11 cube centered at the entity's midpoint.
        // Pivot at (0, 8, 0) places the center of the 1-block-tall hitbox
        // at the geometric center of the cube.
        // The box origin is offset by -5.5 on each axis to center it on the pivot.
        partdefinition.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.5F, -5.5F, -5.5F, 11.0F, 11.0F, 11.0F),
                PartPose.ZERO);

        // Texture size 64x32: gives each face of the 11x11x11 cube
        // a clean 11x11 region with room to spare. When you want unique
        // per-face textures later, just adjust texOffs per face like a player head.
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Spin on all three axes at different speeds for an unsettling energy feel
        this.core.yRot = ageInTicks * 0.05F;
        this.core.xRot = ageInTicks * 0.03F;
        this.core.zRot = ageInTicks * 0.02F;
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }
}