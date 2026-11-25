package net.zharok01.coralinesystems.event.static_portal;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.block.StaticPortalBlock;

@OnlyIn(Dist.CLIENT)
public class StaticPortalOverlay {

    private static final float OVERLAY_U_SPAN = 1.0F;
    private static final float OVERLAY_V_SPAN = 0.14F;

    @SuppressWarnings("removal")
    private static final ResourceLocation STATIC_PORTAL_OVERLAY_TEXTURE =
            new ResourceLocation(CoralineSystems.MOD_ID, "textures/misc/static_portal_overlay.png");

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(StaticPortalOverlay::onPostGameOverlay);
    }

    private static void onPostGameOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int staticTicks = StaticPortalManager.getTicksInPortal(mc.player);
        if (staticTicks <= 0) return;

        if (event.getOverlay().id().equals(VanillaGuiOverlay.HELMET.id())) {

            float alpha = (staticTicks / (float) StaticPortalBlock.PORTAL_TICKS_REQUIRED) * 0.70F; // Max opacity

            float screenWidth = event.getWindow().getGuiScaledWidth();
            float screenHeight = event.getWindow().getGuiScaledHeight();

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);

            float ageInTicks = mc.level.getGameTime() + event.getPartialTick();
            float staticIndexX = (float) Math.sin(ageInTicks * 0.2F) * 0.5F + 0.5F;
            float staticIndexY = (float) Math.cos(ageInTicks * 0.2F + 3F) * 0.43F + 0.5F;

            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            RenderSystem.setShaderTexture(0, STATIC_PORTAL_OVERLAY_TEXTURE);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            float minU = staticIndexX;
            float maxU = minU + OVERLAY_U_SPAN;
            float minV = staticIndexY - OVERLAY_V_SPAN * 0.5F;
            float maxV = minV + OVERLAY_V_SPAN;

            bufferbuilder.vertex(0.0D, screenHeight, -190.0D).uv(minU, maxV).endVertex();
            bufferbuilder.vertex(screenWidth, screenHeight, -190.0D).uv(maxU, maxV).endVertex();
            bufferbuilder.vertex(screenWidth, 0.0D, -190.0D).uv(maxU, minV).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, -190.0D).uv(minU, minV).endVertex();

            tesselator.end();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
