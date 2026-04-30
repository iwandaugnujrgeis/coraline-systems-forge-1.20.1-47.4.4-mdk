package net.zharok01.coralinesystems.client.monster;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.entity.custom.MonsterEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MonsterRenderer extends MobRenderer<MonsterEntity, PlayerModel<MonsterEntity>> {

    private static final ResourceLocation TEXTURE  = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/monster/monster.png");
    private static final ResourceLocation INVISIBLE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/monster/invisible.png");

    public MonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(new MonsterEyeLayer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(MonsterEntity entity) {
        return entity.isSpotted() ? TEXTURE : INVISIBLE;
    }

    @Override
    public void render(MonsterEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        this.shadowRadius = entity.isSpotted() ? 0.5F : 0.0F;

        poseStack.pushPose();

        if (entity.isFading()) {
            // ── Fade animation ────────────────────────────────────────────────
            //
            // fadeProgress: 1.0 = fade just started (entity fully visible),
            //               0.0 = fade complete (entity about to be discarded).
            //
            // We lerp using partialTicks so the animation stays smooth between
            // server ticks — without this it would stutter at 20fps intervals.
            float fadeTicks = entity.getFadeTicksRemaining() - partialTicks;
            float fadeProgress = Math.max(0f, fadeTicks / (float) MonsterEntity.FADE_DURATION_TICKS);
            float dissolve = 1.0F - fadeProgress; // 0.0 → 1.0 over the duration

            // 1. Shrink toward zero
            float scale = fadeProgress;
            poseStack.scale(scale, scale, scale);

            // 2. Escalating jitter — starts subtle, becomes violent at the end
            float jitterIntensity = dissolve * 0.35F;
            float speed = (entity.tickCount + partialTicks) * 4.0F;
            float xOffset = (float)(Math.sin(speed * 3.7) * jitterIntensity);
            float zOffset = (float)(Math.cos(speed * 5.1) * jitterIntensity);
            poseStack.translate(xOffset, 0, zOffset);

            // 3. Alpha — fade the entity translucent as it shrinks.
            //    We apply the shader color before the super.render call and
            //    restore it immediately after so nothing else is affected.
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, fadeProgress);
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        } else if (entity.isGlitching()) {
            // ── Standard glitch jitter (unchanged) ───────────────────────────
            float speed = (entity.tickCount + partialTicks) * 2.5F;
            float intensity = 0.15F;
            float xOffset = (float)(Math.sin(speed * 3.5) * intensity);
            float zOffset = (float)(Math.cos(speed * 4.1) * intensity);
            poseStack.translate(xOffset, 0, zOffset);
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        } else {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }

        poseStack.popPose();
    }

    @Nullable
    @Override
    protected RenderType getRenderType(MonsterEntity entity, boolean bodyVisible,
                                       boolean translucent, boolean glowing) {
        if (!entity.isSpotted() || entity.isFading()) {
            // Use the translucent cull pass for both invisible state and fade-out,
            // so the alpha set by RenderSystem.setShaderColor is respected.
            return RenderType.itemEntityTranslucentCull(this.getTextureLocation(entity));
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }
}