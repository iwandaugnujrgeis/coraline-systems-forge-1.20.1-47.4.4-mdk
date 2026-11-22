package net.zharok01.coralinesystems.client.entity.render.monster;

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
import net.zharok01.coralinesystems.entity.MonsterEntity;
import org.jetbrains.annotations.NotNull;

public class MonsterEyeLayer extends RenderLayer<MonsterEntity, PlayerModel<MonsterEntity>> {

    @SuppressWarnings("removal")
    private static final ResourceLocation EYES_TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/monster/monster_eyes.png");

    public MonsterEyeLayer(RenderLayerParent<MonsterEntity, PlayerModel<MonsterEntity>> renderer) {
        super(renderer);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, MultiBufferSource buffer, int packedLight, @NotNull MonsterEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(EYES_TEXTURE));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
    }
}