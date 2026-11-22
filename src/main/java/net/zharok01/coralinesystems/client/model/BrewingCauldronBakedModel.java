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

public class BrewingCauldronBakedModel implements IDynamicBakedModel {

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

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(
            @NotNull BlockState state,
            @NotNull RandomSource random,
            @NotNull ModelData data) {
        return RENDER_TYPES;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction direction,
            @NotNull RandomSource random,
            @NotNull ModelData modelData,
            @Nullable RenderType renderType) {

        List<BakedQuad> quads = new ArrayList<>();

        if (renderType == null) {
            quads.addAll(shell.getQuads(state, direction, random, modelData, null));
            quads.addAll(fluid.getQuads(state, direction, random, modelData, null));
            return quads;
        }

        if (renderType == RenderType.cutout()) {
            quads.addAll(shell.getQuads(state, direction, random, modelData, renderType));
        }

        if (renderType == RenderType.translucent()) {
            quads.addAll(fluid.getQuads(state, direction, random, modelData, renderType));
        }

        return quads;
    }

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