package net.zharok01.coralinesystems.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CartStorageRenderer extends MinecartRenderer<MinecartChest> {

    @SuppressWarnings("removal")
    private static final ResourceLocation CART_STORAGE_TEXTURE =
            new ResourceLocation("coraline_systems", "textures/entity/cart_storage_entity.png");

    public CartStorageRenderer(EntityRendererProvider.Context context) {
        super(context, CartStorageModel.LAYER_LOCATION);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MinecartChest entity) {
        return CART_STORAGE_TEXTURE;
    }

    @Override
    protected void renderMinecartContents(@NotNull MinecartChest entity, float partialTicks, @NotNull BlockState state, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
    }
}