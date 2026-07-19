package net.zharok01.coralinesystems.client.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite baked model for the Brewing Cauldron.
 *
 * The root cause of the "fluid not rendering" bug was two-fold:
 *  1. CoralineBlocks.registerRenderLayers() called
 *       ItemBlockRenderTypes.setRenderLayer(BREWING_CAULDRON, RenderType.cutout())
 *     which locked the block into a single render pass. The chunk renderer
 *     therefore never issued a translucent-pass getQuads() call, so the
 *     fluid quads were silently discarded.
 *  2. IDynamicBakedModel alone does not opt a block into multiple passes.
 *     We must override getRenderTypes() (from IForgeBakedModel, inherited
 *     via BakedModel) so the model self-declares which chunk buffer layers
 *     it participates in, superseding any ItemBlockRenderTypes registration.
 *
 * Fix: override getRenderTypes() to return both cutout (shell) and
 * translucent (fluid surface), then remove the ItemBlockRenderTypes call
 * from CoralineBlocks.registerRenderLayers() entirely.
 *
 * Render pass split:
 *  - Cutout pass      → shell quads only (normal shading, AO on).
 *  - Translucent pass → fluid surface quads only (AO already off in the
 *                       JSON via "shade": false; tinted by CoralineBlockColors
 *                       via Forge's tint pipeline on tintindex 0).
 */
public class BrewingCauldronBakedModel implements IDynamicBakedModel {

    /**
     * Declared once — both chunk buffer layers this model participates in.
     * cutout()     → the cauldron shell (metal walls, legs, rim).
     * translucent() → the fluid surface quad (tinted, alpha-blended).
     */
    private static final ChunkRenderTypeSet RENDER_TYPES =
            ChunkRenderTypeSet.of(RenderType.cutout(), RenderType.translucent());

    private final BakedModel shell;
    private final BakedModel fluid;
    private final TextureAtlasSprite particle;

    public BrewingCauldronBakedModel(
            BakedModel shell,
            BakedModel fluid,
            TextureAtlasSprite particle) {
        this.shell = shell;
        this.fluid = fluid;
        this.particle = particle;
    }

    // ── Render type declaration ──────────────────────────────────────────────

    /**
     * Overriding this is what actually makes multi-pass rendering work.
     * The chunk renderer calls this once per block position and issues one
     * getQuads() call per layer in the returned set. Without this override,
     * the default implementation delegates to ItemBlockRenderTypes, which
     * only knows about cutout() — the translucent pass call never arrives.
     */
    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(
            @NotNull BlockState state,
            @NotNull RandomSource random,
            @NotNull ModelData data) {
        return RENDER_TYPES;
    }

    // ── IDynamicBakedModel ───────────────────────────────────────────────────

    @Override
    public @NotNull List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction direction,
            @NotNull RandomSource random,
            @NotNull ModelData modelData,
            @Nullable RenderType renderType) {

        List<BakedQuad> quads = new ArrayList<>();

        // null renderType → item-rendering path, emit everything.
        if (renderType == null) {
            quads.addAll(shell.getQuads(state, direction, random, modelData, null));
            quads.addAll(fluid.getQuads(state, direction, random, modelData, null));
            return quads;
        }

        // Shell → cutout pass only.
        if (renderType == RenderType.cutout()) {
            quads.addAll(shell.getQuads(state, direction, random, modelData, renderType));
        }

        // Fluid surface → translucent pass only.
        if (renderType == RenderType.translucent()) {
            quads.addAll(fluid.getQuads(state, direction, random, modelData, renderType));
        }

        return quads;
    }

    // ── BakedModel boilerplate ───────────────────────────────────────────────

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return particle;
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return shell.getTransforms();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}