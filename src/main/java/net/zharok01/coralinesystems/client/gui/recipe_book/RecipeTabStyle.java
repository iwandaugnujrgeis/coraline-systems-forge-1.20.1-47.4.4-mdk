package net.zharok01.coralinesystems.client.gui.recipe_book;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.resources.ResourceLocation;

public final class RecipeTabStyle {
    public static final ResourceLocation CREATIVE_TABS_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    public static final int TAB_WIDTH = 26;
    public static final int TAB_HEIGHT = 32;
    public static final int TAB_STRIDE = 27;
    public static final int TAB_VISIBLE_HEIGHT = 28;

    public static final int SPRITE_CELL_WIDTH = 26;
    public static final int SPRITE_COLUMN_FIRST = 0;
    public static final int SPRITE_COLUMN_MIDDLE = 1;
    public static final int SPRITE_COLUMN_LAST = 6;
    public static final int SPRITE_V_UNSELECTED = 0;
    public static final int SPRITE_V_SELECTED = 32;

    public static final int ICON_OFFSET_X = 5;
    public static final int ICON_OFFSET_Y = 9;

    public static boolean isAllRecipesTab(RecipeBookCategories category) {
        return category.name().endsWith("_SEARCH");
    }

    private RecipeTabStyle() {
    }
}