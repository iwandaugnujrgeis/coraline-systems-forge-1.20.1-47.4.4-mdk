package net.zharok01.coralinesystems.client.gui.creative_tabs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeBookLayout;

public final class CreativeFrame {
    private static final int TEXTURE_SIZE = 256;

    private static final int PANEL_EDGE = 5;
    private static final int PANEL_RIGHT_U = 190;
    private static final int PANEL_BOTTOM_V = 131;
    private static final int PANEL_CLEAN_U = 100;
    private static final int PANEL_CLEAN_V = 60;
    private static final int PANEL_FILL_V = 4;

    private static final int FIELD_U = 80;
    private static final int FIELD_V = 4;
    private static final int FIELD_CAP_WIDTH = 3;
    private static final int FIELD_STRETCH_U = 100;
    private static final int FIELD_RIGHT_CAP_U = 167;

    public static void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        ResourceLocation panel = RecipeBookLayout.CREATIVE_PANEL_TEXTURE;
        int innerWidth = width - PANEL_EDGE * 2;
        int innerHeight = height - PANEL_EDGE * 2;
        int right = x + width - PANEL_EDGE;
        int bottom = y + height - PANEL_EDGE;

        guiGraphics.blit(panel, x, y, 0, 0, PANEL_EDGE, PANEL_EDGE);
        guiGraphics.blit(panel, right, y, PANEL_RIGHT_U, 0, PANEL_EDGE, PANEL_EDGE);
        guiGraphics.blit(panel, x, bottom, 0, PANEL_BOTTOM_V, PANEL_EDGE, PANEL_EDGE);
        guiGraphics.blit(panel, right, bottom, PANEL_RIGHT_U, PANEL_BOTTOM_V, PANEL_EDGE, PANEL_EDGE);
        guiGraphics.blit(panel, x + PANEL_EDGE, y, innerWidth, PANEL_EDGE, PANEL_CLEAN_U, 0, 1, PANEL_EDGE, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(panel, x + PANEL_EDGE, bottom, innerWidth, PANEL_EDGE, PANEL_CLEAN_U, PANEL_BOTTOM_V, 1, PANEL_EDGE, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(panel, x, y + PANEL_EDGE, PANEL_EDGE, innerHeight, 0, PANEL_CLEAN_V, PANEL_EDGE, 1, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(panel, right, y + PANEL_EDGE, PANEL_EDGE, innerHeight, PANEL_RIGHT_U, PANEL_CLEAN_V, PANEL_EDGE, 1, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(panel, x + PANEL_EDGE, y + PANEL_EDGE, innerWidth, innerHeight, PANEL_CLEAN_U, PANEL_FILL_V, 1, 1, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    public static void drawSearchField(GuiGraphics guiGraphics, int x, int y, int width) {
        ResourceLocation field = RecipeBookLayout.CREATIVE_SEARCH_TEXTURE;
        int height = RecipeBookLayout.SEARCH_FIELD_HEIGHT;

        guiGraphics.blit(field, x, y, FIELD_U, FIELD_V, FIELD_CAP_WIDTH, height);
        guiGraphics.blit(field, x + FIELD_CAP_WIDTH, y, width - FIELD_CAP_WIDTH * 2, height, FIELD_STRETCH_U, FIELD_V, 1, height, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(field, x + width - FIELD_CAP_WIDTH, y, FIELD_RIGHT_CAP_U, FIELD_V, FIELD_CAP_WIDTH, height);
    }

    public static void drawInset(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xFF373737);
        guiGraphics.fill(x + width - 1, y + 1, x + width, y + height, 0xFFFFFFFF);
        guiGraphics.fill(x + 1, y + height - 1, x + width, y + height, 0xFFFFFFFF);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF8B8B8B);
    }

    private CreativeFrame() {
    }
}