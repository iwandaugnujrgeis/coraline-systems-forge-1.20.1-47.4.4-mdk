package net.zharok01.coralinesystems.mixin.compat.rediscovered;

import com.legacy.rediscovered.RediscoveredConfig;
import com.legacy.rediscovered.client.render.world.SkylandsCloudRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(value = SkylandsCloudRenderer.class, remap = false)
public abstract class SkylandsCloudRendererMixin {

    @Final @Shadow private static List<SkylandsCloudRenderer> RENDERERS;

    @Shadow private static boolean wasInMiddle;
    @Shadow private CloudStatus prevCloudsType;

    @Unique private static final float OVERWORLD_CLOUD_HEIGHT = 150.0F;

    @Unique private static final List<SkylandsCloudRenderer> OVERWORLD_RENDERERS = new ArrayList<>(List.of(
            new SkylandsCloudRenderer(-25.0F, OVERWORLD_CLOUD_HEIGHT + 65.0F, 20.0F, 0.025F),
            new SkylandsCloudRenderer(0.0F, OVERWORLD_CLOUD_HEIGHT, 0.0F, 0.030F),
            new SkylandsCloudRenderer(15.0F, OVERWORLD_CLOUD_HEIGHT + 130.0F, -15.0F, 0.035F)
    ));

    @Unique private static boolean coraline$overworldWasInMiddle = false;


    @Overwrite
    public static void renderLayers(
            ClientLevel level,
            int ticks,
            float partialTick,
            PoseStack poseStack,
            double camX,
            double camY,
            double camZ,
            Matrix4f projectionMatrix
    ) {
        final boolean inMiddle = camY < 200.0 && camY > 15.0;

        if (inMiddle != wasInMiddle || inMiddle) {
            RENDERERS.sort((r1, r2) -> Double.compare(
                    Math.abs(camY - r2.getHeight()),
                    Math.abs(camY - r1.getHeight())
            ));
        }
        wasInMiddle = inMiddle;

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.depthMask(true);

        for (SkylandsCloudRenderer renderer : RENDERERS) {
            renderer.renderClouds(level, ticks, poseStack, projectionMatrix, partialTick, camX, camY, camZ);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Overwrite
    public BufferBuilder.RenderedBuffer buildClouds(
            BufferBuilder builder,
            double x,
            double y,
            double z,
            Vec3 cloudColor
    ) {
        final CloudStatus cloudsType = this.prevCloudsType;

        final float uvScale = 0.00390625F;
        final float epsilon = 9.765625E-4F;

        final float originU = (float) Mth.floor(x) * uvScale;
        final float originV = (float) Mth.floor(z) * uvScale;

        final float r = (float) cloudColor.x;
        final float g = (float) cloudColor.y;
        final float b = (float) cloudColor.z;

        final float sideR = r * 0.9F;
        final float sideG = g * 0.9F;
        final float sideB = b * 0.9F;
        final float botR = r * 0.9F;
        final float botG = g * 0.9F;
        final float botB = b * 0.9F;
        final float nsR = r * 0.8F;
        final float nsG = g * 0.8F;
        final float nsB = b * 0.8F;

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        final float cloudFloorY = (float) Math.floor(y / 4.0) * 4.0F;

        if (cloudsType == CloudStatus.FANCY) {
            for (int k = -7; k <= 8; ++k) {
                for (int l = -7; l <= 8; ++l) {
                    final float cx = (float) (k * 8);
                    final float cz = (float) (l * 8);

                    if (cloudFloorY > -5.0F) {
                        builder.vertex(cx,       cloudFloorY, cz + 8F).uv(cx        * uvScale + originU, (cz + 8F) * uvScale + originV).color(botR, botG, botB, 0.8F).normal(0F, -1F, 0F).endVertex();
                        builder.vertex(cx + 8F,  cloudFloorY, cz + 8F).uv((cx + 8F) * uvScale + originU, (cz + 8F) * uvScale + originV).color(botR, botG, botB, 0.8F).normal(0F, -1F, 0F).endVertex();
                        builder.vertex(cx + 8F,  cloudFloorY, cz     ).uv((cx + 8F) * uvScale + originU, cz        * uvScale + originV).color(botR, botG, botB, 0.8F).normal(0F, -1F, 0F).endVertex();
                        builder.vertex(cx,        cloudFloorY, cz     ).uv(cx        * uvScale + originU, cz        * uvScale + originV).color(botR, botG, botB, 0.8F).normal(0F, -1F, 0F).endVertex();
                    }

                    if (cloudFloorY <= 5.0F) {
                        final float topY = cloudFloorY + 4.0F - epsilon;
                        builder.vertex(cx,       topY, cz + 8F).uv(cx        * uvScale + originU, (cz + 8F) * uvScale + originV).color(r, g, b, 0.8F).normal(0F, 1F, 0F).endVertex();
                        builder.vertex(cx + 8F,  topY, cz + 8F).uv((cx + 8F) * uvScale + originU, (cz + 8F) * uvScale + originV).color(r, g, b, 0.8F).normal(0F, 1F, 0F).endVertex();
                        builder.vertex(cx + 8F,  topY, cz     ).uv((cx + 8F) * uvScale + originU, cz        * uvScale + originV).color(r, g, b, 0.8F).normal(0F, 1F, 0F).endVertex();
                        builder.vertex(cx,        topY, cz     ).uv(cx        * uvScale + originU, cz        * uvScale + originV).color(r, g, b, 0.8F).normal(0F, 1F, 0F).endVertex();
                    }

                    if (k > -1) {
                        for (int i1 = 0; i1 < 8; ++i1) {
                            final float wx  = cx + i1;
                            final float wuv = (wx + 0.5F) * uvScale + originU;
                            builder.vertex(wx, cloudFloorY,        cz + 8F).uv(wuv, (cz + 8F) * uvScale + originV).color(sideR, sideG, sideB, 0.8F).normal(-1F, 0F, 0F).endVertex();
                            builder.vertex(wx, cloudFloorY + 4.0F, cz + 8F).uv(wuv, (cz + 8F) * uvScale + originV).color(sideR, sideG, sideB, 0.8F).normal(-1F, 0F, 0F).endVertex();
                            builder.vertex(wx, cloudFloorY + 4.0F, cz     ).uv(wuv, cz        * uvScale + originV).color(sideR, sideG, sideB, 0.8F).normal(-1F, 0F, 0F).endVertex();
                            builder.vertex(wx, cloudFloorY,        cz     ).uv(wuv, cz        * uvScale + originV).color(sideR, sideG, sideB, 0.8F).normal(-1F, 0F, 0F).endVertex();
                        }
                    }

                    if (k <= 1) {
                        for (int j2 = 0; j2 < 8; ++j2) {
                            final float ex  = cx + j2 + 1.0F - epsilon;
                            final float euv = (cx + j2 + 0.5F) * uvScale + originU;
                            builder.vertex(ex, cloudFloorY,        cz + 8F).uv(euv, (cz + 8F) * uvScale + originV).color(sideR, sideG, sideB, 0.8F).normal(1F, 0F, 0F).endVertex();
                            builder.vertex(ex, cloudFloorY + 4.0F, cz + 8F).uv(euv, (cz + 8F) * uvScale + originV).color(sideR, sideG, sideB, 0.8F).normal(1F, 0F, 0F).endVertex();
                            builder.vertex(ex, cloudFloorY + 4.0F, cz     ).uv(euv, cz        * uvScale + originV).color(sideR, sideG, sideB, 0.8F).normal(1F, 0F, 0F).endVertex();
                            builder.vertex(ex, cloudFloorY,        cz     ).uv(euv, cz        * uvScale + originV).color(sideR, sideG, sideB, 0.8F).normal(1F, 0F, 0F).endVertex();
                        }
                    }

                    if (l > -1) {
                        for (int k2 = 0; k2 < 8; ++k2) {
                            final float nz  = cz + k2;
                            final float nuv = (nz + 0.5F) * uvScale + originV;
                            builder.vertex(cx,       cloudFloorY + 4.0F, nz).uv(cx        * uvScale + originU, nuv).color(nsR, nsG, nsB, 0.8F).normal(0F, 0F, -1F).endVertex();
                            builder.vertex(cx + 8F,  cloudFloorY + 4.0F, nz).uv((cx + 8F) * uvScale + originU, nuv).color(nsR, nsG, nsB, 0.8F).normal(0F, 0F, -1F).endVertex();
                            builder.vertex(cx + 8F,  cloudFloorY,        nz).uv((cx + 8F) * uvScale + originU, nuv).color(nsR, nsG, nsB, 0.8F).normal(0F, 0F, -1F).endVertex();
                            builder.vertex(cx,        cloudFloorY,        nz).uv(cx        * uvScale + originU, nuv).color(nsR, nsG, nsB, 0.8F).normal(0F, 0F, -1F).endVertex();
                        }
                    }

                    if (l <= 1) {
                        for (int l2 = 0; l2 < 8; ++l2) {
                            final float sz  = cz + l2 + 1.0F - epsilon;
                            final float suv = (cz + l2 + 0.5F) * uvScale + originV;
                            builder.vertex(cx,       cloudFloorY + 4.0F, sz).uv(cx        * uvScale + originU, suv).color(nsR, nsG, nsB, 0.8F).normal(0F, 0F, 1F).endVertex();
                            builder.vertex(cx + 8F,  cloudFloorY + 4.0F, sz).uv((cx + 8F) * uvScale + originU, suv).color(nsR, nsG, nsB, 0.8F).normal(0F, 0F, 1F).endVertex();
                            builder.vertex(cx + 8F,  cloudFloorY,        sz).uv((cx + 8F) * uvScale + originU, suv).color(nsR, nsG, nsB, 0.8F).normal(0F, 0F, 1F).endVertex();
                            builder.vertex(cx,        cloudFloorY,        sz).uv(cx        * uvScale + originU, suv).color(nsR, nsG, nsB, 0.8F).normal(0F, 0F, 1F).endVertex();
                        }
                    }
                }
            }
        } else {
            for (int l1 = -32; l1 < 32; l1 += 32) {
                for (int i2 = -32; i2 < 32; i2 += 32) {
                    builder.vertex(l1,       cloudFloorY, i2 + 32).uv((float)  l1       * uvScale + originU, (float)(i2 + 32) * uvScale + originV).color(r, g, b, 0.8F).normal(0F, -1F, 0F).endVertex();
                    builder.vertex(l1 + 32,  cloudFloorY, i2 + 32).uv((float)(l1 + 32)  * uvScale + originU, (float)(i2 + 32) * uvScale + originV).color(r, g, b, 0.8F).normal(0F, -1F, 0F).endVertex();
                    builder.vertex(l1 + 32,  cloudFloorY, i2     ).uv((float)(l1 + 32)  * uvScale + originU, (float)  i2      * uvScale + originV).color(r, g, b, 0.8F).normal(0F, -1F, 0F).endVertex();
                    builder.vertex(l1, cloudFloorY, i2     ).uv((float)  l1       * uvScale + originU, (float)  i2      * uvScale + originV).color(r, g, b, 0.8F).normal(0F, -1F, 0F).endVertex();
                }
            }
        }

        return builder.end();
    }

    @Overwrite
    public static boolean renderCloudOverride(
            PoseStack poseStack,
            Matrix4f projectionMatrix,
            float partialTick,
            double camX,
            double camY,
            double camZ
    ) {
        if (RediscoveredConfig.CLIENT.overrideVanillaClouds()) {
            Minecraft mc = Minecraft.getInstance();
            coraline$renderOverworldLayers(
                    mc.level,
                    mc.levelRenderer.getTicks(),
                    partialTick,
                    poseStack,
                    camX, camY, camZ,
                    projectionMatrix
            );
            return true;
        }
        return false;
    }

    @Unique
    private static void coraline$renderOverworldLayers(
            ClientLevel level,
            int ticks,
            float partialTick,
            PoseStack poseStack,
            double camX,
            double camY,
            double camZ,
            Matrix4f projectionMatrix
    ) {
        final boolean inMiddle = camY > (OVERWORLD_CLOUD_HEIGHT - 30.0)
                && camY < (OVERWORLD_CLOUD_HEIGHT + 30.0);

        if (inMiddle != coraline$overworldWasInMiddle || inMiddle) {
            OVERWORLD_RENDERERS.sort((r1, r2) -> Double.compare(
                    Math.abs(camY - r2.getHeight()),
                    Math.abs(camY - r1.getHeight())
            ));
        }
        coraline$overworldWasInMiddle = inMiddle;

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.depthMask(true);

        for (SkylandsCloudRenderer renderer : OVERWORLD_RENDERERS) {
            renderer.renderClouds(level, ticks, poseStack, projectionMatrix, partialTick, camX, camY, camZ);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

}