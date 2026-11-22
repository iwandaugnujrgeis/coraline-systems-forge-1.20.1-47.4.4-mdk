package net.zharok01.coralinesystems.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeBookLayout;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeTabStyle;
import org.jetbrains.annotations.NotNull;

public class ContainerBlockScreen extends AbstractContainerScreen<ContainerBlockMenu> {

    private static final ResourceLocation PANEL_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    private static final int PANEL_WIDTH = 176;
    private static final int TOP_PANEL_HEIGHT = ContainerBlockMenu.VISIBLE_ROWS * ContainerBlockMenu.SLOT_SIZE + 17;
    private static final int BOTTOM_PANEL_TEXTURE_V = 126;
    private static final int BOTTOM_PANEL_HEIGHT = 96;

    private static final int SLOT_FRAME_OFFSET = 1;
    private static final int PANEL_BODY_COLOR = 0xFFC6C6C6;
    private static final int SLOT_TEXTURE_U = 7;
    private static final int SLOT_TEXTURE_V = 17;
    private static final int VANILLA_GRID_WIDTH = 9 * ContainerBlockMenu.SLOT_SIZE;
    private static final int GRID_HEIGHT = ContainerBlockMenu.VISIBLE_ROWS * ContainerBlockMenu.SLOT_SIZE;

    private static final int TRACK_SHADOW_COLOR = 0xFF373737;
    private static final int TRACK_HIGHLIGHT_COLOR = 0xFFFFFFFF;
    private static final int TRACK_FILL_COLOR = 0xFF8B8B8B;
    private static final int TRACK_GAP = 4;
    private static final int TRACK_X = ContainerBlockMenu.CONTAINER_SLOTS_X - SLOT_FRAME_OFFSET + ContainerBlockMenu.CONTAINER_COLUMNS * ContainerBlockMenu.SLOT_SIZE + TRACK_GAP;
    private static final int TRACK_Y = ContainerBlockMenu.CONTAINER_SLOTS_Y - SLOT_FRAME_OFFSET;
    private static final int TRACK_HEIGHT = GRID_HEIGHT;
    private static final int KNOB_TRAVEL = TRACK_HEIGHT - 2 - RecipeBookLayout.KNOB_HEIGHT;

    private float coraline$scrollOffset;
    private boolean coraline$isScrolling;

    public ContainerBlockScreen(ContainerBlockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = 222;
        this.inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(PANEL_TEXTURE, leftPos, topPos, 0, 0, imageWidth, TOP_PANEL_HEIGHT);
        graphics.blit(PANEL_TEXTURE, leftPos, topPos + TOP_PANEL_HEIGHT, 0, BOTTOM_PANEL_TEXTURE_V, imageWidth, BOTTOM_PANEL_HEIGHT);

        int gridLeft = leftPos + ContainerBlockMenu.CONTAINER_SLOTS_X - SLOT_FRAME_OFFSET;
        int gridTop = topPos + ContainerBlockMenu.CONTAINER_SLOTS_Y - SLOT_FRAME_OFFSET;
        graphics.fill(gridLeft, gridTop, gridLeft + VANILLA_GRID_WIDTH, gridTop + GRID_HEIGHT, PANEL_BODY_COLOR);

        for (int row = 0; row < ContainerBlockMenu.VISIBLE_ROWS; row++) {
            for (int column = 0; column < ContainerBlockMenu.CONTAINER_COLUMNS; column++) {
                graphics.blit(PANEL_TEXTURE, gridLeft + column * ContainerBlockMenu.SLOT_SIZE, gridTop + row * ContainerBlockMenu.SLOT_SIZE,
                        SLOT_TEXTURE_U, SLOT_TEXTURE_V, ContainerBlockMenu.SLOT_SIZE, ContainerBlockMenu.SLOT_SIZE);
            }
        }

        int trackLeft = leftPos + TRACK_X;
        int trackTop = topPos + TRACK_Y;
        int trackRight = trackLeft + RecipeBookLayout.TRACK_WIDTH;
        int trackBottom = trackTop + TRACK_HEIGHT;
        graphics.fill(trackLeft, trackTop, trackRight, trackBottom, TRACK_HIGHLIGHT_COLOR);
        graphics.fill(trackLeft, trackTop, trackRight - 1, trackBottom - 1, TRACK_SHADOW_COLOR);
        graphics.fill(trackLeft + 1, trackTop + 1, trackRight - 1, trackBottom - 1, TRACK_FILL_COLOR);

        int knobY = trackTop + 1 + (int) (KNOB_TRAVEL * coraline$scrollOffset);
        graphics.blit(RecipeTabStyle.CREATIVE_TABS_TEXTURE, trackLeft + 1, knobY, RecipeBookLayout.KNOB_U_ENABLED, 0, RecipeBookLayout.KNOB_WIDTH, RecipeBookLayout.KNOB_HEIGHT);
    }

    private void coraline$scrollToMouse(double mouseY) {
        double knobTop = topPos + TRACK_Y + 1;
        coraline$scrollOffset = Mth.clamp((float) ((mouseY - knobTop - RecipeBookLayout.KNOB_HEIGHT / 2.0) / KNOB_TRAVEL), 0.0F, 1.0F);
        menu.coraline$scrollTo(coraline$scrollOffset);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double trackLeft = leftPos + TRACK_X;
        double trackTop = topPos + TRACK_Y;
        if (button == 0 && mouseX >= trackLeft && mouseX < trackLeft + RecipeBookLayout.TRACK_WIDTH
                && mouseY >= trackTop && mouseY < trackTop + TRACK_HEIGHT) {
            coraline$isScrolling = true;
            coraline$scrollToMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (coraline$isScrolling) {
            coraline$scrollToMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            coraline$isScrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        coraline$scrollOffset = menu.coraline$scrollBy(coraline$scrollOffset, delta);
        menu.coraline$scrollTo(coraline$scrollOffset);
        return true;
    }
}