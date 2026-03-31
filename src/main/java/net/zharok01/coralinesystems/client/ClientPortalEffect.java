package net.zharok01.coralinesystems.client;

import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.zharok01.coralinesystems.content.block.StaticPortalBlock;
import net.zharok01.coralinesystems.registry.CoralineBlocks;

@OnlyIn(Dist.CLIENT)
public class ClientPortalEffect {

    /** Counts up while the player is inside the portal, counts back down when they leave. */
    private static int staticTicks = 0;

    public static void init() {
        // Both the Tick logic AND the Render logic now run on the FORGE event bus
        MinecraftForge.EVENT_BUS.addListener(ClientPortalEffect::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ClientPortalEffect::onPostGameOverlay);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) return;

        // Check the block at foot-level and head-level so tall portals trigger correctly
        BlockPos feetPos = mc.player.blockPosition();
        boolean inPortal =
                mc.level.getBlockState(feetPos).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get()) ||
                        mc.level.getBlockState(feetPos.above()).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get());

        if (inPortal) {
            // Charge up at the same rate as the server-side countdown
            staticTicks = Math.min(staticTicks + 1, StaticPortalBlock.PORTAL_TICKS_REQUIRED);
        } else {
            // Fade out faster than it fades in — snappy departure feels better than a slow linger
            staticTicks = Math.max(staticTicks - 4, 0);
        }
    }

    private static void onPostGameOverlay(RenderGuiOverlayEvent.Post event) {
        // Only render if the timer has started
        if (staticTicks <= 0) return;

        // Hijack the HELMET vanilla overlay specifically, just like Alex's Mobs does
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HELMET.id())) {
            Minecraft mc = Minecraft.getInstance();
            float alpha = staticTicks / (float) StaticPortalBlock.PORTAL_TICKS_REQUIRED;

            // Use the scaled width/height so it properly fits your GUI scale
            float screenWidth = event.getWindow().getGuiScaledWidth();
            float screenHeight = event.getWindow().getGuiScaledHeight();

            // --- Layer 1: Alex's Mobs Static Screen ---
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);

            float ageInTicks = mc.level.getGameTime() + event.getPartialTick();
            float staticIndexX = (float) Math.sin(ageInTicks * 0.2F) * 2;
            float staticIndexY = (float) Math.cos(ageInTicks * 0.2F + 3F) * 2;

            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            // Link the Alex's Mobs static transparency directly to our alpha timer
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            RenderSystem.setShaderTexture(0, AMRenderTypes.STATIC_TEXTURE);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            float minU = 10 * staticIndexX * 0.125F;
            float maxU = 10 * (0.5F + staticIndexX * 0.125F);
            float minV = 10 * staticIndexY * 0.125F;
            float maxV = 10 * (0.125F + staticIndexY * 0.125F);

            bufferbuilder.vertex(0.0D, screenHeight, -190.0D).uv(minU, maxV).endVertex();
            bufferbuilder.vertex(screenWidth, screenHeight, -190.0D).uv(maxU, maxV).endVertex();
            bufferbuilder.vertex(screenWidth, 0.0D, -190.0D).uv(maxU, minV).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, -190.0D).uv(minU, minV).endVertex();

            tesselator.end();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset color to prevent messing up the rest of the GUI
        }
    }
}