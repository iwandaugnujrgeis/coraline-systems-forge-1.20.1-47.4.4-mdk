package net.zharok01.coralinesystems.client.entity.orb;

import com.legacy.rediscovered.client.RediscoveredRenderRefs;
import com.legacy.rediscovered.client.render.RediscoveredRenderType;
import com.legacy.rediscovered.client.render.model.DragonPylonRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.Material;
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

@OnlyIn(Dist.CLIENT)
public class OrbRenderer<T extends OrbEntity> extends EntityRenderer<T> {

    // The energy shell texture on the block atlas (animated via .mcmeta)
    public static final Material ORB_SHELL_TEXTURE = new Material(
            InventoryMenu.BLOCK_ATLAS,
            CoralineSystems.of("entity/orb/shell")
    );

    // The core cube texture — a standard file-based ResourceLocation,
    // exactly like BrumeRenderer or any MobRenderer texture.
    private static final ResourceLocation ORB_CORE_TEXTURE = new ResourceLocation(
            CoralineSystems.MOD_ID, "textures/entity/orb/core.png"
    );

    // The pylon cube geometry, borrowed from Rediscovered for the energy shells
    private final ModelPart shellPart;

    // Our own core cube model
    private final OrbModel<T> coreModel;

    public OrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shellPart = context.bakeLayer(RediscoveredRenderRefs.DRAGON_PYLON).getChild("cube");
        this.coreModel = new OrbModel<>(context.bakeLayer(CoralineModelLayers.ORB_LAYER));
    }

    @Override
    protected int getBlockLightLevel(@NotNull T entity, @NotNull BlockPos pos) {
        return 15;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack pose,
                       MultiBufferSource bufferSource, int packedLight) {

        float ageInTicks = entity.tickCount + partialTicks;

        // Shared bob: both the core and the shells rise and fall together
        float bob = Mth.cos(ageInTicks * 0.08F) * 0.15F;
        pose.translate(0.0F, 0.5F + bob, 0.0F);

        // --- 1. Render the inner core cube ---
        // Uses the standard entity RenderType so the texture file is loaded normally,
        // exactly the same way BrumeRenderer loads its skin.
        pose.pushPose();
        // Scale the core down slightly so it sits visibly inside the shells
        pose.scale(0.5F, 0.5F, 0.5F);
        this.coreModel.setupAnim(entity, 0, 0, ageInTicks, 0, 0);
        VertexConsumer coreBuffer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(ORB_CORE_TEXTURE)
        );
        this.coreModel.renderToBuffer(pose, coreBuffer, LightTexture.FULL_BRIGHT,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();

        // --- 2. Render the energy shells on top ---
        VertexConsumer shellBuffer = ORB_SHELL_TEXTURE.buffer(bufferSource, RediscoveredRenderType::energy);

        pose.pushPose();
        int hash = Math.abs(entity.getUUID().hashCode());
        float rot = (ageInTicks + (hash % 360)) * 8.0F;
        float rotSpeed = 1.0F / ((hash % 2) + 1);

        pose.scale(2.0F, 2.0F, 2.0F);

        DragonPylonRenderer.renderPylonShields(
                this.shellPart, pose, shellBuffer,
                LightTexture.FULL_BRIGHT,
                rot * rotSpeed,
                1.0F,
                1.12F,
                3,
                0.65F
        );
        pose.popPose();

        super.render(entity, entityYaw, partialTicks, pose, bufferSource, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}