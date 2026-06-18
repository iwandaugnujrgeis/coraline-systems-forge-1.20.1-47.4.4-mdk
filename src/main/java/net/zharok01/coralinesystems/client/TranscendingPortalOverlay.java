package net.zharok01.coralinesystems.client;

import com.legacy.structure_gel.api.block.GelPortalBlock;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.zharok01.coralinesystems.client.screen.TranscendingScreen;

@OnlyIn(Dist.CLIENT)
public final class TranscendingPortalOverlay
{
    private TranscendingPortalOverlay() {}

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(TranscendingPortalOverlay::onPostGameOverlay);
    }

    private static void onPostGameOverlay(RenderGuiOverlayEvent.Post event)
    {
        // Mirror ClientStaticPortalEffect: hook exactly the HELMET overlay slot
        // so we run once per frame at the right point in the GUI stack.
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HELMET.id())) return;

        TranscendingScreen screen = TranscendingScreen.ACTIVE;
        if (screen == null) return;

        float alpha = screen.computeAlpha();
        if (alpha <= 0.0F) return;

        Minecraft mc = Minecraft.getInstance();

        // Resolve the sprite — Gel portal block, or fall back to vanilla Nether portal
        TextureAtlasSprite sprite;
        GelPortalBlock portalBlock = screen.portalBlock;
        if (portalBlock != null)
        {
            sprite = portalBlock.getPortalTexture();
        }
        else
        {
            sprite = mc.getBlockRenderer()
                    .getBlockModelShaper()
                    .getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        }

        float screenWidth  = event.getWindow().getGuiScaledWidth();
        float screenHeight = event.getWindow().getGuiScaledHeight();

        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU1();
        float v1 = sprite.getV1();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(0,           screenHeight, -90.0).uv(u0, v1).endVertex();
        buf.vertex(screenWidth, screenHeight, -90.0).uv(u1, v1).endVertex();
        buf.vertex(screenWidth, 0,            -90.0).uv(u1, v0).endVertex();
        buf.vertex(0,           0,            -90.0).uv(u0, v0).endVertex();
        tesselator.end();

        // Always reset — prevents corrupting subsequent GUI rendering
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}