package net.zharok01.coralinesystems.client.entity.render.star;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.StarEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class StarRenderer extends EntityRenderer<StarEntity> {

    private static final ResourceLocation STAR_TEXTURE =
            CoralineSystems.of("textures/entity/star/star.png");

    private static final ResourceLocation BEAM_TEXTURE =
            new ResourceLocation("rediscovered", "textures/entity/dragon_pylon/beam.png");

    private static final float PLANE_HALF_SIZE = 0.55F;
    private static final float SPIN_DEGREES_PER_TICK = 1.8F;

    private static final float OVERLAY_R = 0.72F;
    private static final float OVERLAY_G = 0.90F;
    private static final float OVERLAY_B = 1.00F;
    private static final float OVERLAY_ALPHA = 0.40F;

    public StarRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull StarEntity entity, float entityYaw, float partialTicks,
                       PoseStack pose, MultiBufferSource bufferSource, int packedLight) {

        float age = entity.tickCount + partialTicks;
        float bob = Mth.cos(age * 0.07F) * 0.12F;
        pose.translate(0.0F, 0.6F + bob, 0.0F);

        float spinDeg = age * SPIN_DEGREES_PER_TICK;

        VertexConsumer texVc = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(STAR_TEXTURE));

        coraline$renderPlane(pose, texVc, spinDeg, 0.0F,
                0.0F, 1.0F, 0.0F, 1.0F,
                1.0F, 1.0F, 1.0F, 1.0F);
        coraline$renderPlane(pose, texVc, spinDeg, 90.0F,
                0.0F, 1.0F, 0.0F, 1.0F,
                1.0F, 1.0F, 1.0F, 1.0F);

        float scrollSpeed = 0.02F;
        float vOffset = (age * scrollSpeed) % 1.0F;
        float vHeight = 16.0F / 256.0F;

        VertexConsumer overlayVc = bufferSource.getBuffer(StarGlintRenderType.getGlint(BEAM_TEXTURE));

        coraline$renderPlane(pose, overlayVc, spinDeg, 0.0F,
                0.0F, 1.0F, vOffset, vOffset + vHeight,
                OVERLAY_R, OVERLAY_G, OVERLAY_B, OVERLAY_ALPHA);
        coraline$renderPlane(pose, overlayVc, spinDeg, 90.0F,
                0.0F, 1.0F, vOffset, vOffset + vHeight,
                OVERLAY_R, OVERLAY_G, OVERLAY_B, OVERLAY_ALPHA);

        super.render(entity, entityYaw, partialTicks, pose, bufferSource, packedLight);
    }

    private static void coraline$renderPlane(PoseStack pose, VertexConsumer vc,
                                             float spinDeg, float planeOffsetDeg,
                                             float u0, float u1, float v0, float v1,
                                             float r, float g, float b, float alpha) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(spinDeg + planeOffsetDeg));

        PoseStack.Pose entry = pose.last();
        Matrix4f mat = entry.pose();
        Matrix3f norm = entry.normal();

        float h = PLANE_HALF_SIZE;
        int ri = (int) (r * 255);
        int gi = (int) (g * 255);
        int bi = (int) (b * 255);
        int ai = (int) (alpha * 255);

        coraline$v(vc, mat, norm, -h,  h, 0.0F, u0, v0, ri, gi, bi, ai);
        coraline$v(vc, mat, norm,  h,  h, 0.0F, u1, v0, ri, gi, bi, ai);
        coraline$v(vc, mat, norm,  h, -h, 0.0F, u1, v1, ri, gi, bi, ai);
        coraline$v(vc, mat, norm, -h, -h, 0.0F, u0, v1, ri, gi, bi, ai);

        pose.popPose();
    }

    private static void coraline$v(VertexConsumer vc, Matrix4f mat, Matrix3f norm,
                                   float x, float y, float z, float u, float v,
                                   int r, int g, int b, int a) {
        vc.vertex(mat, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(norm, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull StarEntity entity) {
        return STAR_TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(@NotNull StarEntity entity, @NotNull BlockPos pos) {
        return 15;
    }

    private abstract static class StarGlintRenderType extends RenderType {
        private StarGlintRenderType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
            super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
        }

        public static RenderType getGlint(ResourceLocation texture) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .createCompositeState(false);

            return create("coraline_star_glint",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256, false, false, state);
        }
    }
}