package net.zharok01.coralinesystems.client.gui.recipe_book;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;

public final class RecipeBookLayout {
    public static final ResourceLocation CREATIVE_PANEL_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tab_items.png");
    public static final ResourceLocation CREATIVE_SEARCH_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
    public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    public static final ResourceLocation STRIP_PAIR_TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/gui/crafting_2_slots_selection.png");
    public static final ResourceLocation STRIP_TRIPLE_TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/gui/crafting_selection.png");
    public static final ResourceLocation SELECTOR_TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/gui/selector_recipe.png");
    public static final ResourceLocation ARROW_UP_TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/gui/arrow_up.png");
    public static final ResourceLocation ARROW_DOWN_TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/gui/arrow_down.png");

    public static final int PANEL_WIDTH = 159;
    public static final int PANEL_HEIGHT = 166;
    private static final int VANILLA_PANEL_OFFSET_X = 86;
    public static final int PANEL_OFFSET_X = VANILLA_PANEL_OFFSET_X + (PANEL_WIDTH - RecipeBookComponent.IMAGE_WIDTH) / 2;

    public static final int TITLE_X = 8;
    public static final int TITLE_Y = 6;
    public static final int TITLE_COLOR = 0x404040;

    public static final int CONTROLLER_TAB_Y = -10_000;
    public static final int CONTROLLER_TAB_NEXT_X = -10_000;
    public static final int CONTROLLER_TAB_PREV_X = -10_001;

    public static final int SNAP_RANGE_CELL = 9;
    public static final int SNAP_RANGE_TAB = 13;
    public static final int SNAP_RANGE_SEARCH = 6;
    public static final int SNAP_RANGE_TRACK = 9;
    public static final int SNAP_RANGE_STRIP = 8;

    public static final int GRID_X = 7;
    public static final int GRID_Y = 29;
    public static final int CELL_SIZE = 18;
    public static final int GRID_COLUMNS = 7;
    public static final int GRID_ROWS = 7;

    public static final int SLOT_U = 7;
    public static final int SLOT_V = 17;

    public static final int TRACK_X = GRID_X + GRID_COLUMNS * CELL_SIZE + 5;
    public static final int TRACK_WIDTH = 14;
    public static final int TRACK_HEIGHT = GRID_ROWS * CELL_SIZE;
    public static final int KNOB_WIDTH = 12;
    public static final int KNOB_HEIGHT = 15;
    public static final int KNOB_U_ENABLED = 232;
    public static final int KNOB_U_DISABLED = 244;
    public static final int KNOB_TRAVEL = TRACK_HEIGHT - 2 - KNOB_HEIGHT;

    public static final int SEARCH_FIELD_X = GRID_X;
    public static final int SEARCH_FIELD_Y = 16;
    public static final int SEARCH_FIELD_WIDTH = GRID_COLUMNS * CELL_SIZE;
    public static final int SEARCH_FIELD_HEIGHT = 12;
    public static final int SEARCH_TEXT_PADDING = 2;
    public static final int SEARCH_TEXT_HEIGHT = 9;

    public static final int STRIP_WIDTH = 24;
    public static final int STRIP_CAP_HEIGHT = 8;
    public static final int STRIP_SLOT_INSET_X = (STRIP_WIDTH - CELL_SIZE) / 2;
    public static final int STRIP_MAX_SLOTS = 3;
    public static final int STRIP_CAROUSEL_SELECTED_SLOT = 1;
    public static final float STRIP_Z = 1000.0F;

    public static final int SELECTOR_SIZE = 24;
    public static final int SELECTOR_MARGIN = (SELECTOR_SIZE - CELL_SIZE) / 2;

    public static final int ARROW_WIDTH = 7;
    public static final int ARROW_HEIGHT = 4;
    public static final int ARROW_X = (STRIP_WIDTH - ARROW_WIDTH) / 2;
    public static final int ARROW_UP_OFFSET_Y = (STRIP_CAP_HEIGHT - ARROW_HEIGHT) / 2 + 1;
    public static final int ARROW_DOWN_OFFSET_Y = (STRIP_CAP_HEIGHT - ARROW_HEIGHT) / 2 - 1;

    public static final int VARIANT_GHOST_CYCLE_TICKS = 30;
    public static final int VARIANT_GHOST_ARRIVAL_TIMEOUT_TICKS = 40;

    public static final int UNCRAFTABLE_OVERLAY_COLOR = 0x998B8B8B;
    public static final int HOVER_OVERLAY_COLOR = 0x80FFFFFF;
    public static final int VARIANT_PIP_OUTLINE_COLOR = 0xFF373737;
    public static final int VARIANT_PIP_PINNED_COLOR = 0xFFFFFF55;
    public static final int VARIANT_PIP_PINNED_OUTLINE_COLOR = 0xFF3E3E00;
    public static final int VARIANT_PIP_COLOR = 0xFFFFFFFF;
    public static final float OVERLAY_Z = 200.0F;
    public static final float ITEM_CYCLE_TICKS = 30.0F;
    public static final float HIGHLIGHT_BOUNCE_TICKS = 15.0F;
    public static final float HIGHLIGHT_ICON_POP = 0.2F;

    private RecipeBookLayout() {
    }
}