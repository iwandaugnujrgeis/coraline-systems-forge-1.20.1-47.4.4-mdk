package net.zharok01.coralinesystems.client.entity.render.monster;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

public class AlphaBufferSource implements MultiBufferSource {

    private final MultiBufferSource delegate;
    private final float alpha;

    public AlphaBufferSource(MultiBufferSource delegate, float alpha) {
        this.delegate = delegate;
        this.alpha = alpha;
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
        return new AlphaConsumer(delegate.getBuffer(renderType), alpha);
    }

    private record AlphaConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {

        @Override
        public @NotNull VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public @NotNull VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, (int) (alpha * this.alpha));
            return this;
        }

        @Override
        public @NotNull VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor(red, green, blue, alpha);
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}