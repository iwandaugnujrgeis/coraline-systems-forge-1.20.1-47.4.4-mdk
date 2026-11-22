package net.zharok01.coralinesystems.client.entity.render.orb;

import com.legacy.rediscovered.client.render.RediscoveredRenderType;
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
import net.minecraft.util.Mth;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.OrbPulseEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class OrbPulseRenderer extends EntityRenderer<OrbPulseEntity> {

    private static final ResourceLocation TEXTURE =
            CoralineSystems.of("textures/entity/orb/orb_pulse.png");

    private static final RenderType RENDER_TYPE =
            RediscoveredRenderType.energy(TEXTURE);

    private static final int HALO_R = 255, HALO_G = 133, HALO_B = 204, HALO_A = 170;
    private static final int GLOW_R = 255, GLOW_G = 184, GLOW_B = 224, GLOW_A = 225;
    private static final int CORE_R = 255, CORE_G = 240, CORE_B = 250, CORE_A = 255;

    public OrbPulseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull OrbPulseEntity entity, float entityYaw,
                       float partialTicks, @NotNull PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {

        float age = entity.tickCount + partialTicks;

        float pulse = 1.0F + Mth.sin(age * 0.25F) * 0.12F;

        VertexConsumer vc = buffer.getBuffer(RENDER_TYPE);

        coraline$drawBillboardedQuad(poseStack, vc, age,
                0.4F,
                0.72F * pulse,
                0.00F,
                HALO_R, HALO_G, HALO_B, HALO_A);

        coraline$drawBillboardedQuad(poseStack, vc, age,
                -0.65F,
                0.48F * pulse,
                0.01F,
                GLOW_R, GLOW_G, GLOW_B, GLOW_A);

        coraline$drawBillboardedQuad(poseStack, vc, age,
                1.1F,
                0.28F * pulse,
                0.02F,
                CORE_R, CORE_G, CORE_B, CORE_A);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void coraline$drawBillboardedQuad(PoseStack poseStack, VertexConsumer vc, float age, float spinSpeed, float scale, float zOffset, int r, int g, int b, int a) {
        poseStack.pushPose();

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.translate(0.0D, 0.0D, zOffset);
        poseStack.mulPose(Axis.ZP.rotation(age * spinSpeed));

        poseStack.scale(scale, scale, scale);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        Matrix3f norm = pose.normal();

        coraline$tintedVertex(vc, mat, norm, -0.5F, -0.5F, 0.0F, 1.0F, r, g, b, a);
        coraline$tintedVertex(vc, mat, norm, 0.5F, -0.5F, 1.0F, 1.0F, r, g, b, a);
        coraline$tintedVertex(vc, mat, norm, 0.5F, 0.5F, 1.0F, 0.0F, r, g, b, a);
        coraline$tintedVertex(vc, mat, norm, -0.5F, 0.5F, 0.0F, 0.0F, r, g, b, a);

        poseStack.popPose();
    }

    private static void coraline$tintedVertex(VertexConsumer vc, Matrix4f mat, Matrix3f norm, float x, float y, float u, float v, int r, int g, int b, int a) {
        vc.vertex(mat, x, y, 0.0F)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(norm, 0F, 0F, 1F)
                .endVertex();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull OrbPulseEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(@NotNull OrbPulseEntity entity, @NotNull BlockPos pos) {
        return 15;
    }
}