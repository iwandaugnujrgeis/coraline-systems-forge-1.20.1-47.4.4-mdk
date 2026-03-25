package net.zharok01.coralinesystems.client.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.entity.custom.MonsterEntity;

public class MonsterEyeLayer extends RenderLayer<MonsterEntity, PlayerModel<MonsterEntity>> {
    // REMEMBER: This texture must have a PURE BLACK background, not transparent!
    private static final ResourceLocation EYES_TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/monster/monster_eyes.png");

    public MonsterEyeLayer(RenderLayerParent<MonsterEntity, PlayerModel<MonsterEntity>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, MonsterEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // TIGER LOGIC: Manually grab the additive 'eyes' buffer
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.eyes(EYES_TEXTURE));

        // TIGER LOGIC: Force the parent model to draw into this buffer at full brightness
        this.getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
    }
}