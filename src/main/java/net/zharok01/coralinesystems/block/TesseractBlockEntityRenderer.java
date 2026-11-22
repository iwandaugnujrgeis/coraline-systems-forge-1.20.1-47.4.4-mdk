package net.zharok01.coralinesystems.block;

import com.legacy.rediscovered.RediscoveredMod;
import com.legacy.rediscovered.client.RediscoveredRenderRefs;
import com.legacy.rediscovered.client.render.RediscoveredRenderType;
import com.legacy.rediscovered.client.render.model.DragonPylonRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.mixin.accessor.ClientLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class TesseractBlockEntityRenderer implements BlockEntityRenderer<TesseractBlockEntity> {

    private static final ResourceLocation BEAM_TEXTURE =
            RediscoveredMod.locate("textures/entity/dragon_pylon/beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RediscoveredRenderType.energy(BEAM_TEXTURE);

    private static final float BEAM_WIDTH = 0.4f;

    private static final float[] BEAM_X = new float[7];
    private static final float[] BEAM_Z = new float[7];

    static {
        for (int j = 0; j <= 6; j++) {
            float angle = j * (Mth.TWO_PI / 6.0f);
            BEAM_X[j] = Mth.sin(angle) * 0.75f;
            BEAM_Z[j] = Mth.cos(angle) * 0.75f;
        }
    }

    private final ModelPart shieldCube;

    public TesseractBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.shieldCube = context.bakeLayer(RediscoveredRenderRefs.DRAGON_PYLON).getChild("cube");
    }

    @Override
    public void render(TesseractBlockEntity tesseract, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        if (!tesseract.isCharged) return;

        float rot = (tesseract.ticks + partialTick) * 4.0f;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(3.0f, 3.0f, 3.0f);
        VertexConsumer shieldBuffer = DragonPylonRenderer.ENERGY_SHIELD_TEXTURE
                .buffer(bufferSource, RediscoveredRenderType::energy);
        DragonPylonRenderer.renderPylonShields(shieldCube, poseStack, shieldBuffer, 15728880, rot, 1.0f, 1.18f, 2);
        poseStack.popPose();

        BlockPos altarPos = tesseract.getLinkedAltarPos();
        if (altarPos != null) {
            float distY = tesseract.getBlockPos().getY() - altarPos.getY();
            poseStack.pushPose();
            poseStack.translate(0.5, 0.0, 0.5);
            renderBeam(0.0f, -distY, 0.0f, partialTick, tesseract.ticks, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        }

        UUID dragonId = tesseract.getTetheredDragonUUID();
        if (dragonId == null) return;

        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) return;

        Entity dragon = ((ClientLevelAccessor) clientLevel).invokeGetEntities().get(dragonId);
        if (dragon == null) return;

        Vec3 tesseractCenter = Vec3.atCenterOf(tesseract.getBlockPos());
        Vec3 dragonCenter = dragon.getPosition(partialTick).add(0.0, dragon.getBbHeight() * 0.5, 0.0);

        float dx = (float) (dragonCenter.x - tesseractCenter.x);
        float dy = (float) (dragonCenter.y - tesseractCenter.y);
        float dz = (float) (dragonCenter.z - tesseractCenter.z);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        renderBeam(dx, dy, dz, partialTick, tesseract.ticks, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    static void renderBeam(float x, float y, float z, float partialTick, int tickCount,
                           PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float horizontalDist = Mth.sqrt(x * x + z * z);
        float totalLength = Mth.sqrt(x * x + y * y + z * z);
        if (totalLength < 0.01f) return;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (-Math.atan2(z, x) * Mth.RAD_TO_DEG) - 90.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) (-Math.atan2(horizontalDist, y) * Mth.RAD_TO_DEG) - 90.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(270.0f));

        float motion = (tickCount + partialTick) * 0.03f;
        float vStart = motion;
        float vEnd = totalLength / 32.0f + motion;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        Matrix3f norm = pose.normal();
        VertexConsumer buffer = bufferSource.getBuffer(BEAM_RENDER_TYPE);

        float prevX = BEAM_X[0];
        float prevZ = BEAM_Z[0];
        float prevU = 0.0f;

        for (int j = 1; j <= 6; j++) {
            float nextX = BEAM_X[j];
            float nextZ = BEAM_Z[j];
            float nextU = (float) j / 6.0f;

            buffer.vertex(mat, prevX * BEAM_WIDTH, prevZ * BEAM_WIDTH, 0.0f)
                    .color(1.0f, 1.0f, 1.0f, 1.0f).uv(prevU, vStart)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                    .normal(norm, 0.0f, -1.0f, 0.0f).endVertex();
            buffer.vertex(mat, prevX * BEAM_WIDTH, prevZ * BEAM_WIDTH, totalLength)
                    .color(1.0f, 1.0f, 1.0f, 1.0f).uv(prevU, vEnd)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                    .normal(norm, 0.0f, -1.0f, 0.0f).endVertex();
            buffer.vertex(mat, nextX * BEAM_WIDTH, nextZ * BEAM_WIDTH, totalLength)
                    .color(1.0f, 1.0f, 1.0f, 1.0f).uv(nextU, vEnd)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                    .normal(norm, 0.0f, -1.0f, 0.0f).endVertex();
            buffer.vertex(mat, nextX * BEAM_WIDTH, nextZ * BEAM_WIDTH, 0.0f)
                    .color(1.0f, 1.0f, 1.0f, 1.0f).uv(nextU, vStart)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                    .normal(norm, 0.0f, -1.0f, 0.0f).endVertex();

            prevX = nextX;
            prevZ = nextZ;
            prevU = nextU;
        }

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}