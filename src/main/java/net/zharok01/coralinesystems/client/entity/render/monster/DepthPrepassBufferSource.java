package net.zharok01.coralinesystems.client.entity.render.monster;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

public class DepthPrepassBufferSource implements MultiBufferSource {

    private final MultiBufferSource delegate;
    private final RenderType depthType;

    public DepthPrepassBufferSource(MultiBufferSource delegate, RenderType depthType) {
        this.delegate = delegate;
        this.depthType = depthType;
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
        if (renderType.format() == DefaultVertexFormat.NEW_ENTITY) {
            return delegate.getBuffer(depthType);
        }
        return delegate.getBuffer(renderType);
    }
}