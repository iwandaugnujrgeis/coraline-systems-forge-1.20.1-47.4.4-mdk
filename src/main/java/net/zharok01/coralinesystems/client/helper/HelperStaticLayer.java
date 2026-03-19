package net.zharok01.coralinesystems.client.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;
import org.jetbrains.annotations.NotNull;

public class HelperStaticLayer extends RenderLayer<HelperEntity, PlayerModel<HelperEntity>> {
    private static final ResourceLocation STATIC_TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/helper/static.png");

    public HelperStaticLayer(RenderLayerParent<HelperEntity, PlayerModel<HelperEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer, int packedLight, HelperEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // SYNCED CHECK: We no longer use a random float here.
        // We ask the entity if the Server has triggered the glitch state.
        if (!entity.isInvisible() && entity.isGlitching()) {

            matrixStack.pushPose();

            // OPTIONAL: Add a tiny "jitter" effect to the model while glitching
            // This makes the static feel physically unstable
            float shake = (entity.getRandom().nextFloat() - 0.5F) * 0.02F;
            matrixStack.translate(shake, shake, shake);

            // Use entityTranslucent so we can see the noise colors (black/grey/white)
            // and apply a 0.7F alpha so the skin is slightly visible underneath.
            VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(STATIC_TEXTURE));

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