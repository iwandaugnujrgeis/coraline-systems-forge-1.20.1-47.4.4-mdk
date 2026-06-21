package net.zharok01.coralinesystems.client.entity.orb;

import com.legacy.rediscovered.client.RediscoveredRenderRefs;
import com.legacy.rediscovered.client.render.RediscoveredRenderType;
import com.legacy.rediscovered.client.render.model.DragonPylonRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
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
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class OrbRenderer<T extends OrbEntity> extends EntityRenderer<T> {

    public static final Material ORB_TEXTURE = new Material(
            InventoryMenu.BLOCK_ATLAS,
            CoralineSystems.of("entity/orb/shell")
    );

    private final ModelPart shellPart;

    public OrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shellPart = context.bakeLayer(RediscoveredRenderRefs.DRAGON_PYLON).getChild("cube");
    }

    @Override
    protected int getBlockLightLevel(@NotNull T entity, @NotNull BlockPos pos) {
        return 15;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack pose,
                       @NotNull MultiBufferSource bufferSource, int packedLight) {

        VertexConsumer buffer = ORB_TEXTURE.buffer(bufferSource, RediscoveredRenderType::energy);

        pose.pushPose();

        int hash = Math.abs(entity.getUUID().hashCode());
        float rot = ((float) entity.tickCount + (hash % 360) + partialTicks) * 8.0F;
        float rotSpeed = 1.0F / ((hash % 2) + 1);

        float bob = Mth.cos((entity.tickCount + partialTicks) * 0.08F) * 0.15F;
        pose.translate(0.0F, 0.5F + bob, 0.0F);

        float scale = 2.0F;
        pose.scale(scale, scale, scale);

        DragonPylonRenderer.renderPylonShields(
                this.shellPart, pose, buffer,
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