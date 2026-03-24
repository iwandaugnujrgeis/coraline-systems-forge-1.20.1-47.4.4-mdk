package net.zharok01.coralinesystems.client.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.entity.custom.MonsterEntity;
import org.jetbrains.annotations.NotNull;

public class MonsterRenderer extends MobRenderer<MonsterEntity, PlayerModel<MonsterEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/monster/monster.png");

    public MonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(MonsterEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MonsterEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Use the synced boolean from MonsterEntity
        boolean isSpotted = entity.isSpotted();

        if (!isSpotted) {
            // STEP 1: Hide every single part of the PlayerModel
            // This prevents the "White Silhouette" by ensuring the engine draws nothing
            toggleModelVisibility(false);

            // STEP 2: Call super.render (it will draw nothing)
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

            // STEP 3: IMMEDIATELY set visibility back to true
            // This ensures the model is ready for the next frame or other entities
            toggleModelVisibility(true);
        } else {
            // If spotted, just render the skin normally
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }

    /**
     * Helper method to toggle all PlayerModel parts at once.
     * This covers the "Outer Layers" (jacket/sleeves) which cause the white glitch.
     */
    private void toggleModelVisibility(boolean visible) {
        this.model.head.visible = visible;
        this.model.hat.visible = visible;
        this.model.body.visible = visible;
        this.model.jacket.visible = visible;
        this.model.leftArm.visible = visible;
        this.model.leftSleeve.visible = visible;
        this.model.rightArm.visible = visible;
        this.model.rightSleeve.visible = visible;
        this.model.leftLeg.visible = visible;
        this.model.leftPants.visible = visible;
        this.model.rightLeg.visible = visible;
        this.model.rightPants.visible = visible;
    }
}