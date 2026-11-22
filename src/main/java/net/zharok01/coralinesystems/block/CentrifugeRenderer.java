package net.zharok01.coralinesystems.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CentrifugeRenderer implements BlockEntityRenderer<CentrifugeBlockEntity> {

    public static final int MAX_RENDER_Y = 1024;
    private static final long SCROLL_LOOP_TICKS = 100L;

    public CentrifugeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CentrifugeBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!blockEntity.getBlockState().getValue(CentrifugeBlock.ACTIVATED)) {
            return;
        }

        assert blockEntity.getLevel() != null;
        int scrollTicks = (int) (blockEntity.getLevel().getGameTime() % SCROLL_LOOP_TICKS);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        TesseractBlockEntityRenderer.renderBeam(0.0f, MAX_RENDER_Y, 0.0f, partialTick, scrollTicks,
                poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull CentrifugeBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(CentrifugeBlockEntity blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos())
                .multiply(1.0D, 0.0D, 1.0D)
                .closerThan(cameraPos.multiply(1.0D, 0.0D, 1.0D), this.getViewDistance());
    }
}