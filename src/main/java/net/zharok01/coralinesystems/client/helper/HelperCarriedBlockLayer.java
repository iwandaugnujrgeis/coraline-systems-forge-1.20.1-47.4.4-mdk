package net.zharok01.coralinesystems.client.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext; // NEW IMPORT
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;

public class HelperCarriedBlockLayer extends RenderLayer<HelperEntity, PlayerModel<HelperEntity>> {
    private final ItemRenderer itemRenderer;

    public HelperCarriedBlockLayer(RenderLayerParent<HelperEntity, PlayerModel<HelperEntity>> parent) {
        super(parent);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, HelperEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        BlockState blockstate = entity.getCarriedBlock();

        if (blockstate != null) {
            poseStack.pushPose();

            // Attach to the left arm
            this.getParentModel().leftArm.translateAndRotate(poseStack);

            // Position and rotate the block in the hand
            poseStack.translate(-0.0625D, 0.6D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.9F, 0.9F, 0.9F);

            ItemStack blockStack = new ItemStack(blockstate.getBlock());

            // CORRECTED RENDER CALL FOR 1.20
            this.itemRenderer.renderStatic(
                    blockStack,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND, // Use ItemDisplayContext
                    light,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(), // NEW: Must pass the level in 1.20
                    entity.getId()
            );

            poseStack.popPose();
        }
    }
}