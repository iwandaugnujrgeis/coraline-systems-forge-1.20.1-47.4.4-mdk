package net.zharok01.coralinesystems.client.entity.render.monster;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class MonsterRenderTypes extends RenderType {

    private MonsterRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        throw new UnsupportedOperationException();
    }

    private static final Function<ResourceLocation, RenderType> GHOST_DEPTH = Util.memoize(texture -> create(
            "coraline_ghost_depth",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,
            false,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(DEPTH_WRITE)
                    .createCompositeState(true)));

    private static final Function<ResourceLocation, RenderType> GHOST_COLOR = Util.memoize(texture -> create(
            "coraline_ghost_color",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,
            true,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .createCompositeState(true)));

    public static RenderType ghostDepth(ResourceLocation texture) {
        return GHOST_DEPTH.apply(texture);
    }

    public static RenderType ghostColor(ResourceLocation texture) {
        return GHOST_COLOR.apply(texture);
    }
}