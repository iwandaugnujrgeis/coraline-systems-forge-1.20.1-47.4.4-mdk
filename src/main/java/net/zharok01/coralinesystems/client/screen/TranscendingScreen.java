package net.zharok01.coralinesystems.client.screen;

import com.legacy.structure_gel.api.block.GelPortalBlock;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
    private static final long MIN_HOLD_MS = 4000L;
    private static final long LINGER_MS   = 7000L;
    private static final long FADE_OUT_MS = 2500L;

    // ── Chunk-ready watchdog ────────────────────────────────────────────────
    private static final long TIMEOUT_MS = 30_000L;

    // ── Static handle so TranscendingPortalOverlay can read our state ───────
    // Set in init(), cleared in removed(). Never access from the server side.
    @Nullable
    public static TranscendingScreen ACTIVE = null;

    // ── Per-instance state ───────────────────────────────────────────────────
    @Nullable
    public final GelPortalBlock portalBlock;   // public so the overlay can read it
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
        ACTIVE = null;    // ← clear so the overlay stops drawing
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
    // The screen itself draws NOTHING — TranscendingPortalOverlay handles
    // all rendering via RenderGuiOverlayEvent.Post, bypassing GuiGraphics.
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        // Intentionally empty — do NOT call super.render()
    }

    // ── Alpha: readable by the overlay ──────────────────────────────────────

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