// net/zharok01/coralinesystems/client/screen/TranscendingScreen.java
package net.zharok01.coralinesystems.client.screen;

import com.legacy.structure_gel.api.block.GelPortalBlock;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.zharok01.coralinesystems.client.portal.PortalTransitionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class TranscendingScreen extends Screen
{
    // ── Timing constants ────────────────────────────────────────────────────
    private static final long FADE_IN_MS  = 500L;
    private static final long MIN_HOLD_MS = 2000L; // Hold for at least 2s
    private static final long FADE_OUT_MS = 2500L; // Extended for a smoother fade

    // ── Chunk-ready watchdog ────────────────────────────────────────────────
    private static final long TIMEOUT_MS = 30_000L;

    // ── State ───────────────────────────────────────────────────────────────
    @Nullable
    private final GelPortalBlock portalBlock;
    private final boolean isNetherPortal;

    private final long createdAt = System.currentTimeMillis();
    private long fadeOutStartMs = -1L;

    private boolean loadingPacketsReceived = false;
    private boolean oneTickSkipped         = false;

    public TranscendingScreen()
    {
        super(GameNarrator.NO_TITLE);
        this.portalBlock = PortalTransitionContext.consumeAndClear();
        // Since this screen only gets instantiated if hasTransition() is true,
        // a null portal block guarantees it's a Nether portal transition.
        this.isNetherPortal = (this.portalBlock == null);
    }

    // ── Screen lifecycle ────────────────────────────────────────────────────

    @Override
    public void init()
    {
        super.init();
        assert this.minecraft != null;
        // Grab mouse immediately to hide the cursor during the transition
        this.minecraft.mouseHandler.grabMouse();
    }

    @Override
    public void removed()
    {
        super.removed();
        // Mouse will naturally be re-grabbed or released by whatever comes next
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    protected boolean shouldNarrateNavigation() { return false; }

    @Override
    public boolean isPauseScreen() { return false; }

    public void loadingPacketsReceived()
    {
        this.loadingPacketsReceived = true;
    }

    // ── Tick: chunk-ready watchdog + fade-out trigger ───────────────────────

    @Override
    public void tick()
    {
        if (System.currentTimeMillis() > createdAt + TIMEOUT_MS)
        {
            this.onClose();
            return;
        }

        if (this.oneTickSkipped)
        {
            assert this.minecraft != null;
            if (this.minecraft.player == null) return;

            BlockPos pos = this.minecraft.player.blockPosition();
            boolean outsideBuildHeight = this.minecraft.level != null
                    && this.minecraft.level.isOutsideBuildHeight(pos.getY());

            boolean chunksReady = outsideBuildHeight
                    || this.minecraft.levelRenderer.isChunkCompiled(pos)
                    || this.minecraft.player.isSpectator()
                    || !this.minecraft.player.isAlive();

            long elapsed = System.currentTimeMillis() - createdAt;
            boolean minHoldElapsed = elapsed > FADE_IN_MS + MIN_HOLD_MS;

            // Wait for both the chunks to load AND the minimum hold time to elapse
            if (chunksReady && fadeOutStartMs == -1L && minHoldElapsed)
            {
                fadeOutStartMs = System.currentTimeMillis();
            }
        }
        else
        {
            this.oneTickSkipped = this.loadingPacketsReceived;
        }

        if (fadeOutStartMs != -1L)
        {
            float fadeOutProgress = (float)(System.currentTimeMillis() - fadeOutStartMs)
                    / (float) FADE_OUT_MS;
            if (fadeOutProgress >= 2.0F)
            {
                this.onClose();
            }
        }
    }

    // ── Render ──────────────────────────────────────────────────────────────

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        float alpha = computeAlpha();
        if (alpha <= 0.0F) return;

        if (this.portalBlock != null || this.isNetherPortal)
        {
            renderPortalOverlay(guiGraphics, alpha);
        }
        else
        {
            guiGraphics.fill(0, 0, this.width, this.height,
                    (int)(alpha * 255F) << 24);
        }

        // Deliberately NOT calling super.render() to prevent "Joining world..." overlays leaking
    }

    private float computeAlpha()
    {
        long now = System.currentTimeMillis();

        if (fadeOutStartMs != -1L)
        {
            // Fade-out phase
            float t = (float)(now - fadeOutStartMs) / (float) FADE_OUT_MS;
            return Mth.clamp(1.0F - t, 0.0F, 1.0F);
        }

        // Fade-in phase
        float t = (float)(now - createdAt) / (float) FADE_IN_MS;
        return Mth.clamp(t, 0.0F, 1.0F);
    }

    private void renderPortalOverlay(GuiGraphics guiGraphics, float alpha)
    {
        assert this.minecraft != null;

        TextureAtlasSprite sprite;
        if (this.portalBlock != null)
        {
            sprite = this.portalBlock.getPortalTexture();
        }
        else
        {
            // Dynamically fetch the native Nether Portal texture
            sprite = this.minecraft.getBlockRenderer()
                    .getBlockModelShaper()
                    .getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU1();
        float v1 = sprite.getV1();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(0,          this.height, -90.0).uv(u0, v1).endVertex();
        buf.vertex(this.width, this.height, -90.0).uv(u1, v1).endVertex();
        buf.vertex(this.width, 0,           -90.0).uv(u1, v0).endVertex();
        buf.vertex(0,          0,           -90.0).uv(u0, v0).endVertex();
        tesselator.end();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Override
    public void onClose()
    {
        assert this.minecraft != null;
        this.minecraft.getNarrator().sayNow(
                Component.translatable("narrator.ready_to_play"));
        super.onClose();
    }
}