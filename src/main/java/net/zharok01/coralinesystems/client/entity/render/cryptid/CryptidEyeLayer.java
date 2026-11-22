package net.zharok01.coralinesystems.client.entity.render.cryptid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.CryptidEntity;
import org.jetbrains.annotations.NotNull;

public class CryptidEyeLayer extends RenderLayer<CryptidEntity, CryptidModel> {

    @SuppressWarnings("removal")
    private static final ResourceLocation EYE_GREEN = new ResourceLocation(
            CoralineSystems.MOD_ID, "textures/entity/cryptid/cryptid_eye_green.png");

    @SuppressWarnings("removal")
    private static final ResourceLocation EYE_PURPLE = new ResourceLocation(
            CoralineSystems.MOD_ID, "textures/entity/cryptid/cryptid_eye_purple.png");

    public CryptidEyeLayer(RenderLayerParent<CryptidEntity, CryptidModel> parent) {
        super(parent);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int packedLight, @NotNull CryptidEntity entity,
                       float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        ResourceLocation eyeTexture = entity.hasGreenEye() ? EYE_GREEN : EYE_PURPLE;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.eyes(eyeTexture));

        this.getParentModel().head.render(
                poseStack,
                vertexConsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                1.0F, 1.0F, 1.0F, 1.0F
        );
    }
}