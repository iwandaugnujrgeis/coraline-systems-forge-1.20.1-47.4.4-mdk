package net.zharok01.coralinesystems.client.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.ObsidianBoatEntity;
import net.zharok01.coralinesystems.registry.CoralineModelLayers;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class ObsidianBoatRenderer extends EntityRenderer<ObsidianBoatEntity> {
    private final ResourceLocation texture;
    private final BoatModel model;

    @SuppressWarnings("removal")
    public ObsidianBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.texture = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/obsidian_boat.png");
        this.model = new BoatModel(context.bakeLayer(CoralineModelLayers.OBSIDIAN_BOAT_LAYER));
        this.shadowRadius = 0.8F;
    }

    @Override
    public void render(ObsidianBoatEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.375D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        float hurtTime = (float)entity.getHurtTime() - partialTicks;
        float damage = entity.getDamage() - partialTicks;
        if (damage < 0.0F) {
            damage = 0.0F;
        }

        if (hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float)entity.getHurtDir()));
        }

        float bubbleAngle = entity.getBubbleAngle(partialTicks);
        if (!Mth.equal(bubbleAngle, 0.0F)) {
            poseStack.mulPose(new Quaternionf().setAngleAxis(bubbleAngle * ((float)Math.PI / 180F), 1.0F, 0.0F, 1.0F));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        this.model.setupAnim(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ObsidianBoatEntity entity) {
        return this.texture;
    }
}