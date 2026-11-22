package net.zharok01.coralinesystems.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CoralineLoadingUtils {

    private static final Component TEXT_MINECRAFT_VERSION = Component.translatable("coraline_systems.loading.version.minecraft");
    private static final Component TEXT_FORGE_VERSION = Component.translatable("coraline_systems.loading.version.forge");
    private static final Component TEXT_MODPACK_VERSION = Component.translatable("coraline_systems.loading.version.modpack");
    private static final Component TEXT_COPYRIGHT_MOJANG = Component.translatable("coraline_systems.loading.copyright.mojang");
    private static final Component TEXT_COPYRIGHT_FORGE = Component.translatable("coraline_systems.loading.copyright.forge");
    private static final Component TEXT_COPYRIGHT_MODPACK = Component.translatable("coraline_systems.loading.copyright.modpack");

    private static final String LANG_READY_PROBE_KEY = "coraline_systems.loading.version.minecraft";

    @SuppressWarnings("removal")
    private static final ResourceLocation SPRITE_SUSPICIOUS_BLOCK =
            new ResourceLocation("coraline_systems", "block/suspicious_block");

    private static final int TILE_SIZE = 32;
    private static final float TILE_BRIGHTNESS = 0.25F;

    private static final float GRADIENT_TOP = 0.1F;
    private static final float GRADIENT_BOTTOM = 0.5F;

    private static final int FOOTER_MARGIN = 5;
    private static final int FULL_ALPHA = 255;
    private static final int MIN_TEXT_ALPHA = 5;

    private static final long ATLAS_TICK_INTERVAL_MS = 70L;
    private static long lastAtlasTick = 0L;

    private static final int STARTUP_CELL_ADVANCE = 8;
    private static final int STARTUP_ROW_GAP = 2;
    private static final int STARTUP_GRID_HALF_WIDTH = 8;
    private static final int STARTUP_GRID_HALF_HEIGHT = 3;
    private static final int STARTUP_MIN_LENGTH = 3;
    private static final int STARTUP_START_MAX_LENGTH = 5;
    private static final int STARTUP_FINAL_MAX_LENGTH = 16;
    private static final int STARTUP_MAX_TAIL_DROPS_PER_STEP = 2;
    private static final int STARTUP_RECOLOR_ODDS = 4;
    private static final int STARTUP_MAX_CATCH_UP_STEPS = 5;
    private static final long STARTUP_STEP_MS = 110L;

    private static final int[] STARTUP_COLORS = {0xFFFFFF, 0xFF5555, 0x5555FF, 0x55FF55, 0xAA55FF};
    private static final int[][] STARTUP_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private static final List<int[]> startupSnake = new ArrayList<>();
    private static final Random startupRandom = new Random();
    private static int startupDirection = 0;
    private static int startupTargetLength = STARTUP_START_MAX_LENGTH;
    private static int startupPhaseStepsLeft = 0;
    private static long startupLastStepTime = 0L;

    private static void tickAtlasAnimation() {
        long now = Util.getMillis();
        if (now - lastAtlasTick < ATLAS_TICK_INTERVAL_MS) return;
        lastAtlasTick = now;

        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager()
                .getTexture(InventoryMenu.BLOCK_ATLAS);
        atlas.cycleAnimationFrames();
    }

    private static void drawTiledBackground(GuiGraphics guiGraphics, int width, int height) {
        tickAtlasAnimation();

        guiGraphics.fill(0, 0, width, height, 0xFF000000);

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(SPRITE_SUSPICIOUS_BLOCK);

        if (!SPRITE_SUSPICIOUS_BLOCK.equals(sprite.contents().name())) {
            drawGradient(guiGraphics, width, height);
            return;
        }

        int cols = (int) Math.ceil((double) width / TILE_SIZE);
        int rows = (int) Math.ceil((double) height / TILE_SIZE);
        float uMin = sprite.getU(0);
        float vMin = sprite.getV(0);

        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int tileX = col * TILE_SIZE;
                int tileY = row * TILE_SIZE;
                int drawWidth = Math.min(TILE_SIZE, width - tileX);
                int drawHeight = Math.min(TILE_SIZE, height - tileY);
                float uMax = sprite.getU((double) drawWidth / TILE_SIZE * 16.0);
                float vMax = sprite.getV((double) drawHeight / TILE_SIZE * 16.0);

                buffer.vertex(matrix, tileX, tileY, 0).color(TILE_BRIGHTNESS, TILE_BRIGHTNESS, TILE_BRIGHTNESS, 1F).uv(uMin, vMin).endVertex();
                buffer.vertex(matrix, tileX, tileY + drawHeight, 0).color(TILE_BRIGHTNESS, TILE_BRIGHTNESS, TILE_BRIGHTNESS, 1F).uv(uMin, vMax).endVertex();
                buffer.vertex(matrix, tileX + drawWidth, tileY + drawHeight, 0).color(TILE_BRIGHTNESS, TILE_BRIGHTNESS, TILE_BRIGHTNESS, 1F).uv(uMax, vMax).endVertex();
                buffer.vertex(matrix, tileX + drawWidth, tileY, 0).color(TILE_BRIGHTNESS, TILE_BRIGHTNESS, TILE_BRIGHTNESS, 1F).uv(uMax, vMin).endVertex();
            }
        }

        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();

        drawGradient(guiGraphics, width, height);
    }

    private static void drawGradient(GuiGraphics guiGraphics, int width, int height) {
        int topColor = FastColor.ARGB32.color((int) (255 * GRADIENT_TOP), 0, 0, 0);
        int bottomColor = FastColor.ARGB32.color((int) (255 * GRADIENT_BOTTOM), 0, 0, 0);
        guiGraphics.fillGradient(0, 0, width, height, topColor, bottomColor);
    }

    private static void drawFooterRow(GuiGraphics guiGraphics, Font font, Component left, Component right, int width, int y, int color) {
        guiGraphics.drawString(font, left, FOOTER_MARGIN, y, color, true);
        guiGraphics.drawString(font, right, width - font.width(right) - FOOTER_MARGIN, y, color, true);
    }

    public static void renderFooter(GuiGraphics guiGraphics, Font font, int width, int height, int alpha) {
        if (alpha < MIN_TEXT_ALPHA || !Language.getInstance().has(LANG_READY_PROBE_KEY)) return;

        int color = FastColor.ARGB32.color(alpha, 255, 255, 255);
        int lineHeight = font.lineHeight + 2;
        int bottom = height - FOOTER_MARGIN;

        drawFooterRow(guiGraphics, font, TEXT_MINECRAFT_VERSION, TEXT_COPYRIGHT_MOJANG, width, bottom - lineHeight * 3, color);
        drawFooterRow(guiGraphics, font, TEXT_FORGE_VERSION, TEXT_COPYRIGHT_FORGE, width, bottom - lineHeight * 2, color);
        drawFooterRow(guiGraphics, font, TEXT_MODPACK_VERSION, TEXT_COPYRIGHT_MODPACK, width, bottom - lineHeight, color);
    }

    private static void drawBranding(GuiGraphics guiGraphics, Font font, int width, int height, boolean drawDots) {
        if (drawDots) {
            renderDots(guiGraphics, font, width, height, FULL_ALPHA);
        }

        renderFooter(guiGraphics, font, width, height, FULL_ALPHA);
    }

    private static int[] createStartupSegment(int gridX, int gridY) {
        int isZero = startupRandom.nextInt(5) < 2 ? 1 : 0;
        return new int[]{gridX, gridY, isZero, startupRandom.nextInt(STARTUP_COLORS.length)};
    }

    private static boolean isStartupCellOccupied(int gridX, int gridY) {
        for (int[] segment : startupSnake) {
            if (segment[0] == gridX && segment[1] == gridY) return true;
        }
        return false;
    }

    private static void stepStartupSnake(int maxLength) {
        startupPhaseStepsLeft--;
        if (startupPhaseStepsLeft <= 0) {
            startupTargetLength = STARTUP_MIN_LENGTH + startupRandom.nextInt(maxLength - STARTUP_MIN_LENGTH + 1);
            startupPhaseStepsLeft = 4 + startupRandom.nextInt(7);
        }

        int[] head = startupSnake.get(0);
        float[] weights = new float[STARTUP_DIRECTIONS.length];
        float totalWeight = 0.0F;

        for (int dir = 0; dir < STARTUP_DIRECTIONS.length; dir++) {
            int nextX = head[0] + STARTUP_DIRECTIONS[dir][0];
            int nextY = head[1] + STARTUP_DIRECTIONS[dir][1];
            if (Math.abs(nextX) > STARTUP_GRID_HALF_WIDTH || Math.abs(nextY) > STARTUP_GRID_HALF_HEIGHT) continue;
            if (isStartupCellOccupied(nextX, nextY)) continue;

            float weight = dir == startupDirection ? 4.0F : 2.0F;
            boolean nearSideEdge = Math.abs(head[0]) > STARTUP_GRID_HALF_WIDTH - 3;
            boolean nearVerticalEdge = Math.abs(head[1]) > STARTUP_GRID_HALF_HEIGHT - 1;
            if (nearSideEdge && Math.abs(nextX) < Math.abs(head[0])) weight += 3.0F;
            if (nearVerticalEdge && Math.abs(nextY) < Math.abs(head[1])) weight += 3.0F;

            weights[dir] = weight;
            totalWeight += weight;
        }

        if (totalWeight == 0.0F) {
            Collections.reverse(startupSnake);
            return;
        }

        float roll = startupRandom.nextFloat() * totalWeight;
        int chosen = 0;
        for (int dir = 0; dir < weights.length; dir++) {
            if (weights[dir] == 0.0F) continue;
            chosen = dir;
            roll -= weights[dir];
            if (roll <= 0.0F) break;
        }

        startupDirection = chosen;
        startupSnake.add(0, createStartupSegment(head[0] + STARTUP_DIRECTIONS[chosen][0], head[1] + STARTUP_DIRECTIONS[chosen][1]));

        int lengthLimit = Math.min(startupTargetLength, maxLength);
        for (int drop = 0; drop < STARTUP_MAX_TAIL_DROPS_PER_STEP && startupSnake.size() > lengthLimit; drop++) {
            startupSnake.remove(startupSnake.size() - 1);
        }

        if (startupRandom.nextInt(STARTUP_RECOLOR_ODDS) == 0) {
            int[] recolored = startupSnake.get(startupRandom.nextInt(startupSnake.size()));
            recolored[3] = startupRandom.nextInt(STARTUP_COLORS.length);
        }
    }

    public static void renderDots(GuiGraphics guiGraphics, Font font, int width, int height, int alpha) {
        if (alpha < MIN_TEXT_ALPHA) return;

        String dots = LoadingDotsText.get(Util.getMillis());
        int color = FastColor.ARGB32.color(alpha, 255, 255, 255);
        guiGraphics.drawString(font, dots, (width - font.width(dots)) / 2, height / 2, color, false);
    }

    public static void renderStartupProgress(GuiGraphics guiGraphics, Font font, int width, int height, int alpha, float progress) {
        if (alpha < MIN_TEXT_ALPHA) return;

        long time = Util.getMillis();
        int maxLength = STARTUP_START_MAX_LENGTH + (int) (progress * (STARTUP_FINAL_MAX_LENGTH - STARTUP_START_MAX_LENGTH));

        if (startupSnake.isEmpty()) {
            startupSnake.add(createStartupSegment(0, 0));
            startupLastStepTime = time;
        } else if (time - startupLastStepTime > STARTUP_STEP_MS * STARTUP_MAX_CATCH_UP_STEPS) {
            startupLastStepTime = time;
        }

        while (time - startupLastStepTime >= STARTUP_STEP_MS) {
            stepStartupSnake(maxLength);
            startupLastStepTime += STARTUP_STEP_MS;
        }

        int rowAdvance = font.lineHeight + STARTUP_ROW_GAP;
        int originX = (width - STARTUP_CELL_ADVANCE) / 2;
        int originY = height / 2 - font.lineHeight / 2;

        for (int[] segment : startupSnake) {
            int x = originX + segment[0] * STARTUP_CELL_ADVANCE;
            int y = originY + segment[1] * rowAdvance;
            int color = (alpha << 24) | STARTUP_COLORS[segment[3]];
            guiGraphics.drawString(font, segment[2] == 1 ? "0" : "o", x, y, color, false);
        }
    }

    public static void renderSusBackgroundOnly(GuiGraphics guiGraphics, int width, int height) {
        drawTiledBackground(guiGraphics, width, height);
    }

    public static void renderBrandingOnly(GuiGraphics guiGraphics, Font font, int width, int height, boolean drawDots) {
        drawBranding(guiGraphics, font, width, height, drawDots);
    }

    public static void hideTextWidgets(Screen screen) {
        for (Renderable renderable : screen.renderables) {
            if (renderable instanceof AbstractWidget widget && widget.getClass().getSimpleName().equals("TextWidget")) {
                widget.visible = false;
            }
        }
    }
}