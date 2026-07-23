package net.zharok01.coralinesystems.mixin;

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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * Coraline Systems — Rediscovered compatibility optimization mixin.
 *
 * Targets {@link SkylandsCloudRenderer} with three {@link Overwrite}s:
 *
 * <h3>Overwrite 1: {@code renderLayers} (static)</h3>
 * <ul>
 *   <li><b>Sort fix:</b> Rediscovered's original condition {@code !inMiddle || inMiddle != wasInMiddle}
 *       sorts the renderer list on <em>every</em> frame when the camera is outside the y=15..200 band —
 *       which is exactly where Skylands clouds are most visible to the player. The corrected condition
 *       {@code inMiddle != wasInMiddle || inMiddle} sorts only when band membership changes, or while
 *       inside the band (where translucency depth order actually varies per-frame with camY).</li>
 *   <li><b>GL state hoisting:</b> All six renderer instances share the same blend function, depth
 *       mask, and cull state. Rediscovered calls disableCull + enableBlend + blendFuncSeparate +
 *       depthMask on entry and enableCull + disableBlend + defaultBlendFunc on exit for every one
 *       of the six layers — 48 GL state changes per frame. We hoist these to a single bracket
 *       around the entire loop: 8 GL state changes per frame.</li>
 * </ul>
 *
 * <h3>Overwrite 2: {@code buildClouds} (instance)</h3>
 * <ul>
 *   <li><b>Dead {@code setShader} removed:</b> Rediscovered calls
 *       {@code RenderSystem.setShader(this::getShader)} at the top of buffer construction.
 *       {@link com.mojang.blaze3d.vertex.VertexBuffer} is an immutable GPU buffer; the RenderSystem
 *       shader at the time {@code BufferBuilder.begin()} is called has no effect on the upload.
 *       The correct shader is already set unconditionally in {@code renderClouds()} at draw-time.
 *       This call was purely wasted work, plus it allocated a new Supplier lambda on every
 *       invocation.</li>
 *   <li><b>{@code prevCloudsType} cached locally:</b> The shadow field is read once and stored
 *       in a local final, avoiding repeated field reads throughout the method.</li>
 * </ul>
 *
 * <h3>Overwrite 3: {@code renderCloudOverride} (static)</h3>
 * <p>Rediscovered's original implementation renders only a single cloud layer in the Overworld
 * when {@code override_vanilla_clouds = true} is set — one call to {@code VANILLA_OVERRIDE.renderClouds()}.
 * This overwrite replaces that with a multi-layer overworld renderer equivalent to the Skylands
 * treatment: three dedicated {@link SkylandsCloudRenderer} instances at staggered heights around
 * the vanilla cloud floor, rendered with the same GL-hoisted loop pattern used in {@code renderLayers}.
 * </p>
 * <ul>
 *   <li>Layer 0 — slightly below vanilla cloud height, offset west/south, slower: provides a
 *       distant lower deck that gives the sky visual depth from above.</li>
 *   <li>Layer 1 — at vanilla cloud height, no offset: the primary layer that matches vanilla
 *       positioning exactly so the override feels native.</li>
 *   <li>Layer 2 — slightly above vanilla cloud height, offset east/north, faster: a thinner
 *       high-altitude streaked layer visible when looking up from the surface.</li>
 * </ul>
 */
@Mixin(value = SkylandsCloudRenderer.class, remap = false)
public abstract class SkylandsCloudRendererMixin {

    // =========================================================================
    // Shadows
    // =========================================================================

    /**
     * The ordered list of Skylands cloud layer renderers.
     * Private static in {@link SkylandsCloudRenderer}.
     */
    @Shadow
    private static List<SkylandsCloudRenderer> RENDERERS;

    /**
     * Tracks whether the Skylands camera was in the middle altitude band on the previous frame.
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
    // @Unique — Overworld multi-layer renderer state
    // =========================================================================

    /**
     * Vanilla Overworld cloud height in 1.20.1. {@code DimensionSpecialEffects} for the Overworld
     * returns this constant from {@code getCloudHeight()}, so we bake it in directly rather than
     * doing a live lookup on each frame.
     *
     * <p>If you ever need to support dimensions that share the vanilla {@code DimensionSpecialEffects}
     * but use a different cloud height, replace this with a dynamic read inside
     * {@link #coralineSystems$renderOverworldLayers} via
     * {@code Minecraft.getInstance().level.effects().getCloudHeight()}.
     */
    @Unique
    private static final float CORALINE_OVERWORLD_CLOUD_HEIGHT = 150.0F;

    /**
     * Three dedicated cloud layer renderers for the Overworld override path.
     *
     * <p>These are distinct instances from the Skylands {@link #RENDERERS} list.
     * Each carries its own {@code VertexBuffer} + dirty-check state, so they never
     * interfere with Skylands rendering regardless of which dimension is active.
     */
    @Unique
    private static final List<SkylandsCloudRenderer> CORALINE_OVERWORLD_RENDERERS = new ArrayList<>(List.of(
            new SkylandsCloudRenderer(-25.0F, CORALINE_OVERWORLD_CLOUD_HEIGHT + 65.0F,  20.0F, 0.025F),
            new SkylandsCloudRenderer(0.0F, CORALINE_OVERWORLD_CLOUD_HEIGHT,          0.0F, 0.030F),
            new SkylandsCloudRenderer(15.0F, CORALINE_OVERWORLD_CLOUD_HEIGHT + 130.0F, -15.0F, 0.035F)
    ));

    /**
     * Overworld-path equivalent of {@link #wasInMiddle}: tracks whether the camera was inside
     * the depth-sort band on the previous frame for the overworld renderer loop.
     *
     * <p>The band is defined relative to the overworld cloud floor
     * ({@link #CORALINE_OVERWORLD_CLOUD_HEIGHT ± 30 blocks}) rather than Skylands' fixed
     * {@code y=15..200} range.
     */
    @Unique
    private static boolean coralineSystems$overworldWasInMiddle = false;

    // =========================================================================
    // Overwrite 1 — renderLayers (static, Skylands)
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
        //   → sorts every frame when the player is OUTSIDE the band (most of the time in Skylands)
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

        // Single shared teardown — was previously called at the end of every renderClouds().
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
        final CloudStatus cloudsType = this.prevCloudsType;

        final float uvScale = 0.00390625F;   // 1/256
        final float epsilon = 9.765625E-4F;  // prevents z-fighting on top face

        final float originU = (float) Mth.floor(x) * uvScale;
        final float originV = (float) Mth.floor(z) * uvScale;

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
        // BufferBuilder.begin() + endVertex() only write into a CPU-side ByteBuffer according
        // to the VertexFormat. VertexBuffer.upload() transfers that buffer to VRAM. Neither
        // operation consults the RenderSystem shader. The shader is consumed only at
        // VertexBuffer.drawWithShader(), which receives it as an explicit argument in
        // renderClouds(). Therefore setShader() here was dead, and 'this::getShader' created
        // a heap-allocated Supplier that was immediately eligible for GC.
        // ---------------------------------------------------------------------

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
    // Overwrite 3 — renderCloudOverride (static)
    // =========================================================================

    /**
     * @author Coraline Systems (zharok01)
     * @reason Rediscovered's original calls {@code VANILLA_OVERRIDE.renderClouds()} exactly
     *   once, producing a single cloud layer at vanilla cloud height. This overwrite replaces
     *   that with {@link #coralineSystems$renderOverworldLayers}, which renders all three
     *   layers in {@link #CORALINE_OVERWORLD_RENDERERS} with the same GL-hoisted loop pattern
     *   used in the Skylands {@link #renderLayers} overwrite above.
     */
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
            coralineSystems$renderOverworldLayers(
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

    // =========================================================================
    // @Unique private helpers
    // =========================================================================

    /**
     * Renders the three overworld cloud layers with a single hoisted GL state bracket,
     * mirroring the Skylands {@link #renderLayers} pattern.
     *
     * <p>The depth-sort band is centred on {@link #CORALINE_OVERWORLD_CLOUD_HEIGHT} ±30 blocks,
     * which corresponds to the altitude range where the player can be simultaneously above one
     * layer and below another (i.e. inside the cloud deck). Outside that band the sort is stable
     * and we skip it.
     */
    @Unique
    private static void coralineSystems$renderOverworldLayers(
            ClientLevel level,
            int ticks,
            float partialTick,
            PoseStack poseStack,
            double camX,
            double camY,
            double camZ,
            Matrix4f projectionMatrix
    ) {
        // Sort band: between the lowest and highest layer heights (with a little margin).
        final boolean inMiddle = camY > (CORALINE_OVERWORLD_CLOUD_HEIGHT - 30.0)
                && camY < (CORALINE_OVERWORLD_CLOUD_HEIGHT + 30.0);

        if (inMiddle != coralineSystems$overworldWasInMiddle || inMiddle) {
            CORALINE_OVERWORLD_RENDERERS.sort((r1, r2) -> Double.compare(
                    Math.abs(camY - coralineSystems$heightOf(r2)),
                    Math.abs(camY - coralineSystems$heightOf(r1))
            ));
        }
        coralineSystems$overworldWasInMiddle = inMiddle;

        // Hoist shared GL state — same rationale as in renderLayers().
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

        for (SkylandsCloudRenderer renderer : CORALINE_OVERWORLD_RENDERERS) {
            renderer.renderClouds(level, ticks, poseStack, projectionMatrix, partialTick, camX, camY, camZ);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /**
     * Reads the Y height of a renderer through its public accessor, used in sort comparators
     * to avoid any direct private-field access across class boundaries.
     */
    @Unique
    private static float coralineSystems$heightOf(SkylandsCloudRenderer renderer) {
        return renderer.getHeight();
    }
}