package net.zharok01.coralinesystems.client.entity.render.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.MonsterEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MonsterRenderer extends MobRenderer<MonsterEntity, PlayerModel<MonsterEntity>> {

    private static final ResourceLocation[] TEXTURES = new ResourceLocation[MonsterEntity.SKIN_VARIANT_COUNT];
    private static final ResourceLocation[] UNSEEN_TEXTURES = new ResourceLocation[MonsterEntity.SKIN_VARIANT_COUNT];

    static {
        for (int i = 0; i < MonsterEntity.SKIN_VARIANT_COUNT; i++) {
            TEXTURES[i] = coraline$texture("textures/entity/monster/monster_" + (i + 1) + ".png");
            UNSEEN_TEXTURES[i] = coraline$texture("textures/entity/monster/monster_unseen_" + (i + 1) + ".png");
        }
    }

    private static final float SHAKE_MIN_DEGREES = 2.0F;
    private static final float SHAKE_EXTRA_DEGREES = 9.0F;
    private static final float SHAKE_SPEED = 2.6F;

    @Nullable private ResourceLocation coraline$passTexture = null;

    public MonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(new MonsterEyeLayer(this));
    }

    @SuppressWarnings("removal")
    private static ResourceLocation coraline$texture(String path) {
        return new ResourceLocation(CoralineSystems.MOD_ID, path);
    }

    private static ResourceLocation coraline$visibleTexture(MonsterEntity entity) {
        return TEXTURES[Math.floorMod(entity.coraline$getSkinId(), TEXTURES.length)];
    }

    private static ResourceLocation coraline$unseenTexture(MonsterEntity entity) {
        return UNSEEN_TEXTURES[Math.floorMod(entity.coraline$getSkinId(), UNSEEN_TEXTURES.length)];
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MonsterEntity entity) {
        if (this.coraline$passTexture != null) {
            return this.coraline$passTexture;
        }
        if (entity.isSpotted() || entity.coraline$isRoaming()) {
            return coraline$visibleTexture(entity);
        }
        return coraline$unseenTexture(entity);
    }

    @Override
    protected boolean shouldShowName(@NotNull MonsterEntity entity) {
        return false;
    }

    private float coraline$getFormed(MonsterEntity entity, float partialTicks) {
        float progress = entity.coraline$getTransitionProgress(partialTicks);
        return entity.coraline$isMaterializing() ? progress : 1.0F - progress;
    }

    @Override
    protected void setupRotations(@NotNull MonsterEntity entity, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);

        if (entity.coraline$isTransitioning()) {
            float formed = this.coraline$getFormed(entity, partialTicks);
            float shakeDegrees = SHAKE_MIN_DEGREES + (1.0F - formed) * SHAKE_EXTRA_DEGREES;
            float time = ageInTicks * SHAKE_SPEED;
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(time * 3.1D) * shakeDegrees));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.sin(time * 4.3D) * shakeDegrees * 0.7F));
        }
    }

    @Override
    public void render(MonsterEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {

        this.shadowRadius = entity.isSpotted() || entity.coraline$isRoaming() ? 0.5F : 0.0F;

        MultiBufferSource colorBuffer = buffer;
        boolean crossfading = entity.coraline$isTransitioning() && !entity.isFading();

        poseStack.pushPose();

        if (entity.isFading()) {
            float fadeTicks = entity.getFadeTicksRemaining() - partialTicks;
            float fadeProgress = Math.max(0f, fadeTicks / (float) MonsterEntity.FADE_DURATION_TICKS);
            float dissolve = 1.0F - fadeProgress;
            poseStack.scale(fadeProgress, fadeProgress, fadeProgress);

            float jitterIntensity = dissolve * 0.35F;
            float speed = (entity.tickCount + partialTicks) * 4.0F;
            poseStack.translate(
                    (float) (Math.sin(speed * 3.7) * jitterIntensity),
                    0,
                    (float) (Math.cos(speed * 5.1) * jitterIntensity)
            );

            colorBuffer = new AlphaBufferSource(buffer, fadeProgress);

        } else if (entity.isGlitching() && !crossfading) {
            float speed = (entity.tickCount + partialTicks) * 2.5F;
            float intensity = 0.15F;
            poseStack.translate(
                    (float) (Math.sin(speed * 3.5) * intensity),
                    0,
                    (float) (Math.cos(speed * 4.1) * intensity)
            );
        }

        if (entity.coraline$usesGhostRender()) {
            this.coraline$passTexture = crossfading ? coraline$visibleTexture(entity) : null;
            RenderType depthType = MonsterRenderTypes.ghostDepth(this.getTextureLocation(entity));
            super.render(entity, entityYaw, partialTicks, poseStack, new DepthPrepassBufferSource(colorBuffer, depthType), packedLight);
        }

        if (crossfading) {
            this.coraline$passTexture = coraline$unseenTexture(entity);
            super.render(entity, entityYaw, partialTicks, poseStack, colorBuffer, packedLight);

            this.coraline$passTexture = coraline$visibleTexture(entity);
            float formed = this.coraline$getFormed(entity, partialTicks);
            super.render(entity, entityYaw, partialTicks, poseStack, new AlphaBufferSource(colorBuffer, formed), packedLight);

            this.coraline$passTexture = null;
        } else {
            super.render(entity, entityYaw, partialTicks, poseStack, colorBuffer, packedLight);
        }

        poseStack.popPose();
    }

    @Nullable
    @Override
    protected RenderType getRenderType(MonsterEntity entity, boolean bodyVisible,
                                       boolean translucent, boolean glowing) {
        if (entity.coraline$usesGhostRender()) {
            return MonsterRenderTypes.ghostColor(this.getTextureLocation(entity));
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }
}