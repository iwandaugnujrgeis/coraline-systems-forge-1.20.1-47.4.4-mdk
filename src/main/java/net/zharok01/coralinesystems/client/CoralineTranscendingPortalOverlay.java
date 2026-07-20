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
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public final class CoralineTranscendingPortalOverlay
{
    // ── Timing ──────────────────────────────────────────────────────────────
    private static final long FADE_IN_MS  =   500L;
    private static final long MIN_HOLD_MS = 4_000L;   // minimum opaque hold before we even check chunks
    private static final long FADE_OUT_MS = 2_500L;
    private static final long TIMEOUT_MS  = 30_000L;  // hard bail-out if chunks never arrive

    // ── State machine ────────────────────────────────────────────────────────
    private enum State { IDLE, FADING_IN, HOLDING, FADING_OUT }

    private static State state = State.IDLE;

    /** The portal whose texture we render; null means Nether portal (use vanilla sprite). */
    @Nullable
    private static GelPortalBlock activePortal = null;

    /** Timestamps set when we enter each phase. */
    private static long phaseStartMs     = 0L;
    private static long transitionStartMs = 0L;  // absolute start (used for MIN_HOLD_MS + TIMEOUT)

    /**
     * Set to true when handleSetSpawn fires — mirrors the vanilla
     * ReceivingLevelScreen.loadingPacketsReceived() gate.  We never
     * allow the chunk-readiness check to pass until the server has
     * confirmed the spawn position, because levelRenderer.isChunkCompiled()
     * can return true spuriously on the very first tick before any real
     * chunks are in place.
     */
    private static boolean loadingPacketsReceived = false;

    // ── Public entry-points called by the mixin ──────────────────────────────

    /**
     * Kick off a transition.  Pass the {@link GelPortalBlock} for a Gel portal,
     * or {@code null} to signal a vanilla Nether portal.
     */
    public static void beginTransition(@Nullable GelPortalBlock portal)
    {
        activePortal          = portal;
        state                 = State.FADING_IN;
        phaseStartMs          = System.currentTimeMillis();
        transitionStartMs     = phaseStartMs;
        loadingPacketsReceived = false;
    }

    /**
     * Called by the mixin when handleSetSpawn fires.
     * Mirrors ReceivingLevelScreen.loadingPacketsReceived().
     */
    public static void notifyLoadingPacketsReceived()
    {
        if (state != State.IDLE)
            loadingPacketsReceived = true;
    }

    /** True while the overlay is active (any phase other than IDLE). */
    public static boolean isActive()
    {
        return state != State.IDLE;
    }

    // ── Registration ─────────────────────────────────────────────────────────

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(CoralineTranscendingPortalOverlay::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(CoralineTranscendingPortalOverlay::onPostGameOverlay);
    }

    // ── Tick: drives state transitions ───────────────────────────────────────

    private static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;
        if (state == State.IDLE) return;

        long now     = System.currentTimeMillis();
        long elapsed = now - phaseStartMs;

        // Hard timeout — prevents permanent black screen if something goes wrong
        if (now - transitionStartMs > TIMEOUT_MS)
        {
            reset();
            return;
        }

        switch (state)
        {
            case FADING_IN ->
            {
                if (elapsed >= FADE_IN_MS)
                {
                    state        = State.HOLDING;
                    phaseStartMs = now;
                }
            }

            case HOLDING ->
            {
                // Only move to fade-out once BOTH conditions are met:
                //   1.  We've held for at least MIN_HOLD_MS
                //   2.  Chunks at the player's position are compiled (or player is spectator/dead)
                boolean minHoldElapsed = elapsed >= MIN_HOLD_MS;
                boolean chunksReady    = areChunksReady();

                if (minHoldElapsed && chunksReady)
                {
                    state        = State.FADING_OUT;
                    phaseStartMs = now;
                }
            }

            case FADING_OUT ->
            {
                if (elapsed >= FADE_OUT_MS)
                    reset();
            }

            default -> { /* IDLE — already handled above */ }
        }
    }

    // ── Chunk-readiness check (mirrors ReceivingLevelScreen logic) ────────────

    private static boolean areChunksReady()
    {
        // Don't even start checking until the server has sent the spawn position.
        // This prevents a false-positive on the first tick after setLevel().
        if (!loadingPacketsReceived) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;

        BlockPos pos = mc.player.blockPosition();

        // Outside build height is treated as "ready" (same as vanilla ReceivingLevelScreen)
        if (mc.level.isOutsideBuildHeight(pos.getY())) return true;

        return mc.levelRenderer.isChunkCompiled(pos)
                || mc.player.isSpectator()
                || !mc.player.isAlive();
    }

    // ── Alpha computation ─────────────────────────────────────────────────────

    private static float computeAlpha()
    {
        long elapsed = System.currentTimeMillis() - phaseStartMs;

        return switch (state)
        {
            case FADING_IN  -> Mth.clamp((float) elapsed / FADE_IN_MS,  0.0F, 1.0F);
            case HOLDING    -> 1.0F;
            case FADING_OUT -> Mth.clamp(1.0F - (float) elapsed / FADE_OUT_MS, 0.0F, 1.0F);
            default         -> 0.0F;
        };
    }

    // ── Render ────────────────────────────────────────────────────────────────

    private static void onPostGameOverlay(RenderGuiOverlayEvent.Post event)
    {
        // Mirror CoralineClientStaticPortalEffect: hook HELMET slot — fires once per frame
        // at the right point in the vanilla GUI stack.
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HELMET.id())) return;
        if (state == State.IDLE) return;

        float alpha = computeAlpha();
        if (alpha <= 0.0F) return;

        Minecraft mc = Minecraft.getInstance();

        // Resolve the sprite — Gel portal, or fall back to vanilla Nether portal texture
        TextureAtlasSprite sprite;
        if (activePortal != null)
        {
            sprite = activePortal.getPortalTexture();
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

        Tesselator     tesselator = Tesselator.getInstance();
        BufferBuilder  buf        = tesselator.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(0,           screenHeight, -90.0).uv(u0, v1).endVertex();
        buf.vertex(screenWidth, screenHeight, -90.0).uv(u1, v1).endVertex();
        buf.vertex(screenWidth, 0,            -90.0).uv(u1, v0).endVertex();
        buf.vertex(0,           0,            -90.0).uv(u0, v0).endVertex();
        tesselator.end();

        // Always reset render state — prevents corrupting subsequent GUI rendering
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void reset()
    {
        state                 = State.IDLE;
        activePortal          = null;
        phaseStartMs          = 0L;
        transitionStartMs     = 0L;
        loadingPacketsReceived = false;
    }
}