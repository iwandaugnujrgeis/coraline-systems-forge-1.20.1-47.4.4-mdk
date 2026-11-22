package net.zharok01.coralinesystems.client.entity.render.cryptid;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineModelLayers;
import net.zharok01.coralinesystems.entity.CryptidEntity;
import org.jetbrains.annotations.NotNull;

public class CryptidRenderer extends MobRenderer<CryptidEntity, CryptidModel> {

    @SuppressWarnings("removal")
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            CoralineSystems.MOD_ID, "textures/entity/cryptid/cryptid.png");

    private static final float SCALE = 1.15F;

    public CryptidRenderer(EntityRendererProvider.Context context) {
        super(context, new CryptidModel(context.bakeLayer(CoralineModelLayers.CRYPTID)), 0.5F);
        this.addLayer(new CryptidEyeLayer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CryptidEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(@NotNull CryptidEntity entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public void render(@NotNull CryptidEntity entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}