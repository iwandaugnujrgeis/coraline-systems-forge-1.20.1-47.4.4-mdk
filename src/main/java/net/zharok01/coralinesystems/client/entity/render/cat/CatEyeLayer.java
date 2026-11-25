package net.zharok01.coralinesystems.client.entity.render.cat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.LightLayer;
import net.zharok01.coralinesystems.CoralineSystems;

public class CatEyeLayer extends RenderLayer<Cat, CatModel<Cat>> {

    @SuppressWarnings("removal")
    private static final ResourceLocation DEFAULT_EYES_TEXTURE = new ResourceLocation(
            CoralineSystems.MOD_ID, "textures/entity/cat/cat_eyes.png");

    @SuppressWarnings("removal")
    private static final Map<String, ResourceLocation> EYE_TEXTURES = Map.ofEntries(
            Map.entry("textures/entity/cat/all_black.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/all_black_eyes.png")),
            Map.entry("textures/entity/cat/black.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/black_eyes.png")),
            Map.entry("textures/entity/cat/british_shorthair.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/british_shorthair_eyes.png")),
            Map.entry("textures/entity/cat/calico.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/calico_eyes.png")),
            Map.entry("textures/entity/cat/jellie.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/jellie_eyes.png")),
            Map.entry("textures/entity/cat/persian.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/persian_eyes.png")),
            Map.entry("textures/entity/cat/ragdoll.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/ragdoll_eyes.png")),
            Map.entry("textures/entity/cat/red.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/red_eyes.png")),
            Map.entry("textures/entity/cat/siamese.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/siamese_eyes.png")),
            Map.entry("textures/entity/cat/tabby.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/tabby_eyes.png")),
            Map.entry("textures/entity/cat/white.png", new ResourceLocation(CoralineSystems.MOD_ID,
                    "textures/entity/cat/white_eyes.png"))
    );

    public CatEyeLayer(RenderLayerParent<Cat, CatModel<Cat>> parent) {
        super(parent);
    }

    @Override
    @SuppressWarnings("null")
    public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer,
                       int packedLight, @Nonnull Cat cat,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        // ASSUMPTION: "In the dark" uses Alex's Mobs' local-light threshold and day/night handling.
        long dayTime = cat.level().getDayTime() % 24000L;
        boolean night = dayTime >= 13000L && dayTime <= 22000L;
        BlockPos lightPosition = BlockPos.containing(cat.getX(), cat.getEyeY(), cat.getZ());
        int blockLight = cat.level().getBrightness(LightLayer.BLOCK, lightPosition);
        int brightness = night
                ? blockLight
                : Math.max(cat.level().getBrightness(LightLayer.SKY, lightPosition), blockLight);

        if (brightness < 7) {
            ResourceLocation catTexture = cat.getVariant().texture();
            ResourceLocation eyeTexture = EYE_TEXTURES.getOrDefault(
                    catTexture.getPath(), DEFAULT_EYES_TEXTURE);
            RenderType eyesRenderType = RenderType.eyes(eyeTexture);
            VertexConsumer vertexConsumer = buffer.getBuffer(eyesRenderType);
            this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight,
                    LivingEntityRenderer.getOverlayCoords(cat, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
