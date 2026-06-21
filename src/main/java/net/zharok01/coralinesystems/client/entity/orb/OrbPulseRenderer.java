package net.zharok01.coralinesystems.client.entity.orb;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.OrbPulseEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class OrbPulseRenderer extends EntityRenderer<OrbPulseEntity> {

    // The path to your flat 2D sprite texture
    private static final ResourceLocation TEXTURE = CoralineSystems.of("textures/entity/orb/orb_pulse.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);

    public OrbPulseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull OrbPulseEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 1. Billboarding: Make the flat sprite always face the player's camera
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // Optional: Scale the sprite down if 16x16 feels too large in-game
        poseStack.scale(0.5F, 0.5F, 0.5F);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);

        // 2. Draw the 4 corners of the 2D quad
        vertex(vertexConsumer, poseMatrix, normalMatrix, packedLight, 0.0F, 0, 0, 1);
        vertex(vertexConsumer, poseMatrix, normalMatrix, packedLight, 1.0F, 0, 1, 1);
        vertex(vertexConsumer, poseMatrix, normalMatrix, packedLight, 1.0F, 1, 1, 0);
        vertex(vertexConsumer, poseMatrix, normalMatrix, packedLight, 0.0F, 1, 0, 0);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, int light, float x, float y, int u, int v) {
        consumer.vertex(pose, x - 0.5F, y - 0.25F, 0.0F)
                .color(255, 255, 255, 255)
                .uv((float) u, (float) v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull OrbPulseEntity entity) {
        return TEXTURE;
    }

    /**
     * Forces the projectile to always render at maximum brightness (15),
     * ignoring the actual lighting of the block it is currently passing through.
     */
    @Override
    protected int getBlockLightLevel(@NotNull OrbPulseEntity entity, @NotNull BlockPos pos) {
        return 15;
    }
}