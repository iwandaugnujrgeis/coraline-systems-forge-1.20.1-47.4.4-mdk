package net.zharok01.coralinesystems.client.monster;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.entity.custom.MonsterEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MonsterRenderer extends MobRenderer<MonsterEntity, PlayerModel<MonsterEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/monster/monster.png");
    // This MUST be a fully transparent 2x2 PNG
    private static final ResourceLocation INVISIBLE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/monster/invisible.png");

    public MonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        // We can safely add the layer now
        this.addLayer(new MonsterEyeLayer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(MonsterEntity entity) {
        return entity.isSpotted() ? TEXTURE : INVISIBLE;
    }

    @Override
    public void render(MonsterEntity entity, float entityYaw, float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight) {

        if (!entity.isSpotted()) {
            this.shadowRadius = 0.0F;
        } else {
            this.shadowRadius = 0.5F;
        }

        poseStack.pushPose();

        // Extreme visual jitter ONLY during the 5-second spotted phase
        if (entity.isGlitching()) {
            float speed = (entity.tickCount + partialTicks) * 2.5F; // Very fast
            float intensity = 0.15F; // Aggressive shaking distance

            float xOffset = (float)(Math.sin(speed * 3.5) * intensity);
            float zOffset = (float)(Math.cos(speed * 4.1) * intensity);

            poseStack.translate(xOffset, 0, zOffset);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Nullable
    @Override
    protected RenderType getRenderType(MonsterEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        // TIGER LOGIC: Swap the RenderType when stalking
        if (!entity.isSpotted()) {
            return RenderType.itemEntityTranslucentCull(this.getTextureLocation(entity));
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }
}