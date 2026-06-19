package net.zharok01.coralinesystems.client.screen;

import com.legacy.structure_gel.api.block.GelPortalBlock;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.client.portal.PortalTransitionContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class TranscendingScreen extends Screen
{
    // ── Timing constants ────────────────────────────────────────────────────
    private static final long FADE_IN_MS  = 500L;
    private static final long MIN_HOLD_MS = 4000L;
    private static final long LINGER_MS   = 7000L;
    private static final long FADE_OUT_MS = 2500L;

    // ── Chunk-ready watchdog ────────────────────────────────────────────────
    private static final long TIMEOUT_MS = 30_000L;

    // ── Static handle so external code can check if we're active ───────────
    @Nullable
    public static TranscendingScreen ACTIVE = null;

    // ── Per-instance state ───────────────────────────────────────────────────
    @Nullable
    public final GelPortalBlock portalBlock;
    public final boolean isNetherPortal;

    private final long createdAt = System.currentTimeMillis();
    private long fadeOutStartMs = -1L;

    private boolean loadingPacketsReceived = false;
    private boolean oneTickSkipped         = false;

    public TranscendingScreen()
    {
        super(GameNarrator.NO_TITLE);
        this.portalBlock    = PortalTransitionContext.consumeAndClear();
        this.isNetherPortal = (this.portalBlock == null);
    }

    // ── Screen lifecycle ────────────────────────────────────────────────────

    @Override
    public void init()
    {
        super.init();
        ACTIVE = this;
        assert this.minecraft != null;
        this.minecraft.mouseHandler.grabMouse();
    }

    @Override
    public void removed()
    {
        ACTIVE = null;
        super.removed();
    }

    @Override public boolean shouldCloseOnEsc()           { return false; }
    @Override protected boolean shouldNarrateNavigation() { return false; }
    @Override public boolean isPauseScreen()              { return false; }

    public void loadingPacketsReceived()
    {
        this.loadingPacketsReceived = true;
    }

    // ── Tick ────────────────────────────────────────────────────────────────

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

            if (chunksReady && fadeOutStartMs == -1L && minHoldElapsed)
                fadeOutStartMs = System.currentTimeMillis();
        }
        else
        {
            this.oneTickSkipped = this.loadingPacketsReceived;
        }

        if (fadeOutStartMs != -1L)
        {
            long elapsedSinceFadeTrigger = System.currentTimeMillis() - fadeOutStartMs;
            if (elapsedSinceFadeTrigger >= LINGER_MS + FADE_OUT_MS)
                this.onClose();
        }
    }

    // ── Render ──────────────────────────────────────────────────────────────

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        float alpha = computeAlpha();
        if (alpha <= 0.0F) return;

        assert this.minecraft != null;

        // Resolve sprite — Gel portal block, or fall back to vanilla Nether portal
        TextureAtlasSprite sprite;
        if (portalBlock != null)
        {
            sprite = portalBlock.getPortalTexture();
        }
        else
        {
            sprite = this.minecraft.getBlockRenderer()
                    .getBlockModelShaper()
                    .getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        }

        float screenWidth  = this.width;
        float screenHeight = this.height;

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

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        // Do NOT call super.render() — we don't want the dirt background
    }

    // ── Alpha ────────────────────────────────────────────────────────────────

    public float computeAlpha()
    {
        long now = System.currentTimeMillis();

        if (fadeOutStartMs != -1L)
        {
            long elapsed = now - fadeOutStartMs;
            if (elapsed < LINGER_MS) return 1.0F;
            float t = (float)(elapsed - LINGER_MS) / (float) FADE_OUT_MS;
            return Mth.clamp(1.0F - t, 0.0F, 1.0F);
        }

        float t = (float)(now - createdAt) / (float) FADE_IN_MS;
        return Mth.clamp(t, 0.0F, 1.0F);
    }

    // ── Close ───────────────────────────────────────────────────────────────

    @Override
    public void onClose()
    {
        assert this.minecraft != null;
        this.minecraft.getNarrator().sayNow(
                Component.translatable("narrator.ready_to_play"));
        super.onClose();
    }
}