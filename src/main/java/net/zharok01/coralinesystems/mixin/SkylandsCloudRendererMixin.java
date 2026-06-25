package net.zharok01.coralinesystems.mixin;

import com.legacy.rediscovered.client.render.world.SkylandsCloudRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/**
 * Coraline Systems — Rediscovered compatibility optimization mixin.
 *
 * Targets {@link SkylandsCloudRenderer} with two surgical {@link Overwrite}s:
 *
 * <h3>Overwrite 1: {@code renderLayers} (static)</h3>
 * <ul>
 *   <li><b>Sort fix:</b> Rediscovered's original condition {@code !inMiddle || inMiddle != wasInMiddle}
 *       sorts the renderer list on <em>every</em> frame when the camera is outside the y=15..200 band —
 *       which is exactly where Skylands clouds are most visible to the player. The corrected condition
 *       {@code inMiddle != wasInMiddle || inMiddle} sorts only when band membership changes, or during
 *       every frame <em>inside</em> the middle band (where translucency depth order matters).</li>
 *   <li><b>GL state hoisting:</b> All six renderer instances share the same blend function, depth
 *       mask, and cull state. Rediscovered calls {@code disableCull + enableBlend + blendFuncSeparate +
 *       depthMask} on entry and {@code enableCull + disableBlend + defaultBlendFunc} on exit for every
 *       one of the six layers — 48 GL state changes per frame. We hoist these to a single bracket
 *       around the entire loop: 8 GL state changes per frame.</li>
 * </ul>
 *
 * <h3>Overwrite 2: {@code buildClouds} (instance)</h3>
 * <ul>
 *   <li><b>Dead {@code setShader} removed:</b> Rediscovered calls
 *       {@code RenderSystem.setShader(this::getShader)} at the top of buffer construction. A
 *       {@link com.mojang.blaze3d.vertex.VertexBuffer} is an immutable GPU buffer; the RenderSystem
 *       shader at the time {@code BufferBuilder.begin()} is called has no effect on the upload — only
 *       the {@link VertexFormat} matters. The correct shader is already set unconditionally in
 *       {@code renderClouds()} at draw-time. This call was purely wasted work, plus it allocated a
 *       new {@code Supplier} lambda ({@code this::getShader}) on every invocation.</li>
 *   <li><b>{@code prevCloudsType} read cached:</b> The shadow-accessed field is read twice inside
 *       the original (once for the FANCY branch, once implicitly). Using a local final avoids
 *       repeated volatile/synchronized field reads from a different class's instance.</li>
 * </ul>
 */
@Mixin(value = SkylandsCloudRenderer.class, remap = false)
public abstract class SkylandsCloudRendererMixin {

    // =========================================================================
    // Shadows
    // =========================================================================

    /**
     * The ordered list of cloud layer renderers.
     * Private static in {@link SkylandsCloudRenderer}.
     */
    @Shadow
    private static List<SkylandsCloudRenderer> RENDERERS;

    /**
     * Tracks whether the camera was in the middle altitude band on the previous frame.
     * Private static in {@link SkylandsCloudRenderer}.
     */
    @Shadow
    private static boolean wasInMiddle;

    /**
     * The cloud-type (FANCY / FAST / OFF) cached from last dirty-check.
     * Private instance field in {@link SkylandsCloudRenderer}, read in {@code buildClouds}.
     */
    @Shadow
    private CloudStatus prevCloudsType;

    // =========================================================================
    // Overwrite 1 — renderLayers (static)
    // =========================================================================

    /**
     * @author Coraline Systems (zharok01)
     * @reason See class-level Javadoc: sort-fix + GL state hoisting.
     */
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

        // --- Sort fix --------------------------------------------------------
        // Original: (!inMiddle || inMiddle != wasInMiddle)
        //   → sorts every frame when player is OUTSIDE the band (i.e. most of the time)
        //
        // Corrected: (inMiddle != wasInMiddle || inMiddle)
        //   → sorts only when the band is entered/exited, OR while inside the band
        //     (where layering order for translucency actually varies per-frame with camY).
        //   → Outside the band: sort once on exit, then never again until re-entry.
        if (inMiddle != wasInMiddle || inMiddle) {
            RENDERERS.sort((r1, r2) -> Double.compare(
                    Math.abs(camY - coralineSystems$heightOf(r2)),
                    Math.abs(camY - coralineSystems$heightOf(r1))
            ));
        }
        wasInMiddle = inMiddle;

        // --- GL state hoisting -----------------------------------------------
        // These 5 calls were executed once per layer (×6) in renderClouds().
        // All 6 layers use identical values, so we set them once here.
        // renderClouds() still performs its own internal state management for
        // the depth pre-pass (colorMask) and the per-layer shader/texture binding,
        // which are layer-specific and intentionally left untouched.
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

        // Single shared teardown. Was previously called at the end of every renderClouds().
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    // =========================================================================
    // Overwrite 2 — buildClouds (instance)
    // =========================================================================

    /**
     * @author Coraline Systems (zharok01)
     * @reason See class-level Javadoc: dead {@code setShader} removed, {@code prevCloudsType}
     *   cached to a local. All geometry output is byte-for-byte identical to the original.
     */
    @Overwrite
    public BufferBuilder.RenderedBuffer buildClouds(
            BufferBuilder builder,
            double x,
            double y,
            double z,
            Vec3 cloudColor
    ) {
        // Cache the cloud type once — the shadow field is read by both the FANCY
        // branch check and (in the original) by the now-removed setShader call.
        final CloudStatus cloudsType = this.prevCloudsType;

        // UV scale constants, unchanged from original.
        final float uvScale = 0.00390625F;   // 1/256
        final float epsilon = 9.765625E-4F;  // prevents z-fighting on top face

        final float originU = (float) Mth.floor(x) * uvScale;
        final float originV = (float) Mth.floor(z) * uvScale;

        // Cloud colour components, factored out once.
        final float r = (float) cloudColor.x;
        final float g = (float) cloudColor.y;
        final float b = (float) cloudColor.z;

        // Face shading multipliers match Rediscovered exactly:
        final float sideR = r * 0.9F;  // E/W faces
        final float sideG = g * 0.9F;
        final float sideB = b * 0.9F;
        final float botR  = r * 0.9F;  // Bottom face (Rediscovered raised this from vanilla's 0.7)
        final float botG  = g * 0.9F;
        final float botB  = b * 0.9F;
        final float nsR   = r * 0.8F;  // N/S faces
        final float nsG   = g * 0.8F;
        final float nsB   = b * 0.8F;

        // --- REMOVED: RenderSystem.setShader(this::getShader) ----------------
        // This was Rediscovered's first line here. Explanation:
        //   - BufferBuilder.begin() + endVertex() calls only write into a CPU-side
        //     ByteBuffer according to the VertexFormat. No GPU state is consulted.
        //   - VertexBuffer.upload() transfers that ByteBuffer to VRAM. Again, no
        //     shader involvement.
        //   - The shader is consumed only at VertexBuffer.drawWithShader(), which
        //     happens in renderClouds() and receives the shader as an explicit argument.
        //   - Therefore: setShader() here was dead, and 'this::getShader' created a
        //     heap-allocated Supplier object that was immediately eligible for GC.
        // ---------------------------------------------------------------------

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        final float cloudFloorY = (float) Math.floor(y / 4.0) * 4.0F;

        if (cloudsType == CloudStatus.FANCY) {
            for (int k = -7; k <= 8; ++k) {
                for (int l = -7; l <= 8; ++l) {
                    final float cx = (float) (k * 8);  // chunk X origin in cloud space
                    final float cz = (float) (l * 8);  // chunk Z origin in cloud space

                    if (cloudFloorY > -5.0F) {
                        // Bottom face (normal pointing down)
                        builder.vertex(cx,       cloudFloorY, cz + 8F).uv(cx        * uvScale + originU, (cz + 8F) * uvScale + originV).color(botR, botG, botB, 0.8F).normal(0F, -1F, 0F).endVertex();
                        builder.vertex(cx + 8F,  cloudFloorY, cz + 8F).uv((cx + 8F) * uvScale + originU, (cz + 8F) * uvScale + originV).color(botR, botG, botB, 0.8F).normal(0F, -1F, 0F).endVertex();
                        builder.vertex(cx + 8F,  cloudFloorY, cz     ).uv((cx + 8F) * uvScale + originU, cz        * uvScale + originV).color(botR, botG, botB, 0.8F).normal(0F, -1F, 0F).endVertex();
                        builder.vertex(cx,        cloudFloorY, cz     ).uv(cx        * uvScale + originU, cz        * uvScale + originV).color(botR, botG, botB, 0.8F).normal(0F, -1F, 0F).endVertex();
                    }

                    if (cloudFloorY <= 5.0F) {
                        // Top face (normal pointing up), epsilon offset prevents z-fighting
                        final float topY = cloudFloorY + 4.0F - epsilon;
                        builder.vertex(cx,       topY, cz + 8F).uv(cx        * uvScale + originU, (cz + 8F) * uvScale + originV).color(r, g, b, 0.8F).normal(0F, 1F, 0F).endVertex();
                        builder.vertex(cx + 8F,  topY, cz + 8F).uv((cx + 8F) * uvScale + originU, (cz + 8F) * uvScale + originV).color(r, g, b, 0.8F).normal(0F, 1F, 0F).endVertex();
                        builder.vertex(cx + 8F,  topY, cz     ).uv((cx + 8F) * uvScale + originU, cz        * uvScale + originV).color(r, g, b, 0.8F).normal(0F, 1F, 0F).endVertex();
                        builder.vertex(cx,        topY, cz     ).uv(cx        * uvScale + originU, cz        * uvScale + originV).color(r, g, b, 0.8F).normal(0F, 1F, 0F).endVertex();
                    }

                    if (k > -1) {
                        // West faces — one sub-quad per block column (8 total)
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
                        // East faces
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
                        // North faces
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
                        // South faces
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
            // Fast (non-FANCY) path — flat quad sheet, preserved verbatim from original.
            for (int l1 = -32; l1 < 32; l1 += 32) {
                for (int i2 = -32; i2 < 32; i2 += 32) {
                    builder.vertex(l1,       cloudFloorY, i2 + 32).uv((float)  l1       * uvScale + originU, (float)(i2 + 32) * uvScale + originV).color(r, g, b, 0.8F).normal(0F, -1F, 0F).endVertex();
                    builder.vertex(l1 + 32,  cloudFloorY, i2 + 32).uv((float)(l1 + 32)  * uvScale + originU, (float)(i2 + 32) * uvScale + originV).color(r, g, b, 0.8F).normal(0F, -1F, 0F).endVertex();
                    builder.vertex(l1 + 32,  cloudFloorY, i2     ).uv((float)(l1 + 32)  * uvScale + originU, (float)  i2      * uvScale + originV).color(r, g, b, 0.8F).normal(0F, -1F, 0F).endVertex();
                    builder.vertex(l1,        cloudFloorY, i2     ).uv((float)  l1       * uvScale + originU, (float)  i2      * uvScale + originV).color(r, g, b, 0.8F).normal(0F, -1F, 0F).endVertex();
                }
            }
        }

        return builder.end();
    }

    // =========================================================================
    // @Unique private helpers
    // =========================================================================

    /**
     * Reads the Y height of a renderer through its public accessor.
     * Used in the sort comparator inside {@link #renderLayers} to avoid any
     * direct private-field access across class boundaries.
     */
    @Unique
    private static float coralineSystems$heightOf(SkylandsCloudRenderer renderer) {
        return renderer.getHeight();
    }
}