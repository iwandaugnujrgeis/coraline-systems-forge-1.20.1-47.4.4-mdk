package net.zharok01.coralinesystems.client.entity.render.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.HelperEntity;
import org.jetbrains.annotations.NotNull;

public class HelperGlitchLayer extends RenderLayer<HelperEntity, PlayerModel<HelperEntity>> {

    @SuppressWarnings("removal")
    private static final ResourceLocation STATIC_TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/helper/helper_static.png");
    private static final int FRAME_COUNT = 32;

    public HelperGlitchLayer(RenderLayerParent<HelperEntity, PlayerModel<HelperEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer, int packedLight, HelperEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isInvisible() && entity.isGlitching()) {

            matrixStack.pushPose();

            float shake = (entity.getRandom().nextFloat() - 0.5F) * 0.02F;
            matrixStack.translate(shake, shake, shake);

            int frame = Math.floorMod((int) (ageInTicks / 2.0F), FRAME_COUNT);
            VertexConsumer vertexconsumer = new VertexConsumerWrapper(
                    buffer.getBuffer(RenderType.entityTranslucent(STATIC_TEXTURE))) {
                @Override
                public VertexConsumer uv(float u, float v) {
                    return super.uv(u, (v + frame) / FRAME_COUNT);
                }
            };

            this.getParentModel().renderToBuffer(
                    matrixStack,
                    vertexconsumer,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 0.7F
            );

            matrixStack.popPose();
        }
    }
}