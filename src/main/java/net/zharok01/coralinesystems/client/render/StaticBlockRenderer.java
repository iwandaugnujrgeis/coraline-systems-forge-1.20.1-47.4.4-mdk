package net.zharok01.coralinesystems.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.content.block.StaticBlockEntity;

public class StaticBlockRenderer implements BlockEntityRenderer<StaticBlockEntity> {

    public StaticBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(StaticBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState copiedState = blockEntity.getCopiedState();

        // If it hasn't copied anything yet, or copied air, don't render anything
        if (copiedState == null || copiedState.isAir()) {
            return;
        }

        poseStack.pushPose();

        // --- EFFECT 1: UV SHIFTING (The Misalignment) ---
        // We move the entire rendering of the block slightly off-center.
        // Moving it by 0.01F creates a tiny seam where the block doesn't line up with its neighbors.
        poseStack.translate(0.01F, -0.01F, 0.01F);

        // We also slightly squish the block so the texture maps weirdly
        poseStack.scale(0.99F, 1.02F, 0.99F);

        // --- EFFECT 2: COLOR DRAIN (Desaturation via Lighting) ---
        // Instead of calculating normal block light, we force the rendering engine to use a
        // completely flat, dark gray light value. This strips the vibrancy from the copied block.
        int drainedLight = 0x00F000F0; // This is an unnatural, flat lighting code.

        // Actually draw the copied block using our modified pose stack and lighting
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                copiedState,
                poseStack,
                bufferSource,
                drainedLight, // The "Drained" lighting
                packedOverlay
        );

        poseStack.popPose();
    }
}