package net.zharok01.coralinesystems.client.entity.render.orb;

import com.legacy.rediscovered.client.RediscoveredRenderRefs;
import com.legacy.rediscovered.client.render.RediscoveredRenderType;
import com.legacy.rediscovered.client.render.model.DragonPylonRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.OrbEntity;
import net.zharok01.coralinesystems.registry.CoralineModelLayers;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class OrbRenderer<T extends OrbEntity> extends EntityRenderer<T> {

    @SuppressWarnings("removal")
    private static final ResourceLocation ORB_CORE_TEXTURE = new ResourceLocation(
            CoralineSystems.MOD_ID, "textures/entity/orb/core.png"
    );

    private static final float[][] SHIELD_LAYER_COLORS = {
            {1.0F, 0.90F, 0.96F}, // Layer 0 (Inner): Soft pastel
            {1.0F, 0.72F, 0.88F}, // Layer 1 (Mid): Fairy pink
            {1.0F, 0.52F, 0.80F} // Layer 2 (Outer): Pink halo
    };

    private final ModelPart shellPart;
    private final OrbModel<T> coreModel;

    public OrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shellPart = context.bakeLayer(RediscoveredRenderRefs.DRAGON_PYLON).getChild("cube");
        this.coreModel = new OrbModel<>(context.bakeLayer(CoralineModelLayers.ORB_LAYER));
    }

    @Override
    protected int getBlockLightLevel(@NotNull T entity, @NotNull BlockPos pos) {
        return 14;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack pose,
                       MultiBufferSource bufferSource, int packedLight) {

        float ageInTicks = entity.tickCount + partialTicks;

        float bob = Mth.cos(ageInTicks * 0.08F) * 0.15F;
        pose.translate(0.0F, 0.5F + bob, 0.0F);

        pose.pushPose();
        pose.scale(0.5F, 0.5F, 0.5F);
        this.coreModel.setupAnim(entity, 0, 0, ageInTicks, 0, 0);
        VertexConsumer coreBuffer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(ORB_CORE_TEXTURE)
        );
        this.coreModel.renderToBuffer(pose, coreBuffer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();

        int shellLayers = entity.isDeadOrDying() ? 0 : (entity.isCritical() ? 2 : 3);

        if (shellLayers > 0) {
            VertexConsumer shellBuffer = DragonPylonRenderer.ENERGY_SHIELD_TEXTURE
                    .buffer(bufferSource, RediscoveredRenderType::energy);

            pose.pushPose();
            int hash = Math.abs(entity.getUUID().hashCode());
            float rot = (ageInTicks + (hash % 360)) * 8.0F;
            float rotSpeed = 1.0F / ((hash % 2) + 1);

            pose.scale(2.0F, 2.0F, 2.0F);

            coraline$renderPylonShields(
                    this.shellPart, pose, shellBuffer,
                    LightTexture.FULL_BRIGHT,
                    rot * rotSpeed,
                    1.0F,
                    1.12F,
                    shellLayers,
                    0.50F
            );
            pose.popPose();
        }

        super.render(entity, entityYaw, partialTicks, pose, bufferSource, packedLight);
    }

    private void coraline$renderPylonShields(ModelPart cube, PoseStack poseStack, VertexConsumer buffer,
                                             int packedLight, float rotation, float scale, float scaleMul,
                                             int layers, float alpha) {
        poseStack.pushPose();
        for (int i = 0; i < layers; ++i) {
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(new Quaternionf().setAngleAxis((float) Math.PI / 3.0F, DragonPylonRenderer.SIN_45, 0.0F, DragonPylonRenderer.SIN_45));
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation * 0.4F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * -0.1F));

            float[] color = SHIELD_LAYER_COLORS[Math.min(i, SHIELD_LAYER_COLORS.length - 1)];
            cube.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], alpha);
            scale *= scaleMul;
        }
        poseStack.popPose();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}