package net.zharok01.coralinesystems.mixin.client.gui.screens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.zharok01.coralinesystems.client.gui.CoralineDragEnd;
import net.zharok01.coralinesystems.client.gui.CoralineScrollable;
import net.zharok01.coralinesystems.client.gui.creative_tabs.CreativeFrame;
import net.zharok01.coralinesystems.client.gui.recipe_book.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookPage.class)
public abstract class RecipeBookPageMixin implements CoralineScrollable, CoralineDragEnd, RecipeStripState, RecipePinAccess, RecipePageNavigation {

    @Shadow @Final private List<RecipeButton> buttons;
    @Shadow @Final private OverlayRecipeComponent overlay;
    @Shadow private Minecraft minecraft;
    @Shadow private List<RecipeCollection> recipeCollections;
    @Shadow private StateSwitchingButton forwardButton;
    @Shadow private StateSwitchingButton backButton;
    @Shadow private Recipe<?> lastClickedRecipe;
    @Shadow private RecipeCollection lastClickedRecipeCollection;

    @Unique private int coraline$originX;
    @Unique private int coraline$originY;
    @Unique private float coraline$scrollOffs;
    @Unique private boolean coraline$draggingScrollbar;
    @Unique private boolean coraline$overlayWasOpen;
    @Unique private final Map<RecipeCollection, Recipe<?>> coraline$pinned = new HashMap<>();

    @Override
    public boolean coraline$isOverStrip(double mouseX, double mouseY) {
        return ((RecipeStripState) this.overlay).coraline$isOverStrip(mouseX, mouseY);
    }

    @Override
    public boolean coraline$isShowing(RecipeCollection collection) {
        return ((RecipeStripState) this.overlay).coraline$isShowing(collection);
    }

    @Override
    public int[] coraline$getSelectedSlotCenter() {
        return ((RecipeStripState) this.overlay).coraline$getSelectedSlotCenter();
    }

    @Override
    public Recipe<?> coraline$getPinned(RecipeCollection collection) {
        return this.coraline$pinned.get(collection);
    }

    @Override
    public void coraline$pin(RecipeCollection collection, Recipe<?> recipe) {
        this.coraline$pinned.put(collection, recipe);
    }

    @Override
    public Recipe<?> coraline$getShownRecipe(RecipeCollection collection) {
        for (RecipeButton button : this.buttons) {
            if (button.visible && button.getCollection() == collection) {
                return button.getRecipe();
            }
        }
        throw new IllegalStateException("No visible recipe button for the collection that opened the strip");
    }

    @Override
    public void coraline$collectSnapTargets(List<int[]> targets) {
        for (RecipeButton button : this.buttons) {
            if (button.visible) {
                targets.add(new int[]{button.getX() + RecipeBookLayout.CELL_SIZE / 2, button.getY() + RecipeBookLayout.CELL_SIZE / 2, RecipeBookLayout.SNAP_RANGE_CELL});
            }
        }

        if (this.coraline$getHiddenRows() > 0) {
            int trackCenterX = this.coraline$originX + RecipeBookLayout.TRACK_X + RecipeBookLayout.TRACK_WIDTH / 2;
            for (int row = 0; row < RecipeBookLayout.GRID_ROWS; row++) {
                int rowCenterY = this.coraline$originY + RecipeBookLayout.GRID_Y + row * RecipeBookLayout.CELL_SIZE + RecipeBookLayout.CELL_SIZE / 2;
                targets.add(new int[]{trackCenterX, rowCenterY, RecipeBookLayout.SNAP_RANGE_TRACK});
            }
        }
    }

    @Override
    public boolean coraline$isOverVariantCell(double mouseX, double mouseY) {
        for (RecipeButton button : this.buttons) {
            if (button.visible && button.isMouseOver(mouseX, mouseY)) {
                RecipeCollection collection = button.getCollection();
                return collection.getDisplayRecipes(true).size() + collection.getDisplayRecipes(false).size() > 1;
            }
        }
        return false;
    }

    @Override
    public boolean coraline$isStripOpen() {
        return this.overlay.isVisible();
    }

    @Override
    public void coraline$stepStrip(int direction) {
        ((RecipeStripAccess) this.overlay).coraline$step(direction);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void coraline$addExtraRow(CallbackInfo ci) {
        for (int i = RecipeBookPage.ITEMS_PER_PAGE; i < RecipeBookLayout.GRID_COLUMNS * RecipeBookLayout.GRID_ROWS; i++) {
            this.buttons.add(new RecipeButton());
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void coraline$bindOverlayPins(CallbackInfo ci) {
        ((RecipeStripAccess) this.overlay).coraline$bindPins(this);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void coraline$layoutGrid(Minecraft minecraft, int x, int y, CallbackInfo ci) {
        this.coraline$originX = x;
        this.coraline$originY = y;
        this.coraline$draggingScrollbar = false;

        for (int i = 0; i < this.buttons.size(); i++) {
            int column = i % RecipeBookLayout.GRID_COLUMNS;
            int row = i / RecipeBookLayout.GRID_COLUMNS;
            this.buttons.get(i).setPosition(x + RecipeBookLayout.GRID_X + RecipeBookLayout.CELL_SIZE * column, y + RecipeBookLayout.GRID_Y + RecipeBookLayout.CELL_SIZE * row);
        }

        this.forwardButton.visible = false;
        this.backButton.visible = false;

        this.forwardButton.setPosition(RecipeBookLayout.CONTROLLER_TAB_NEXT_X, RecipeBookLayout.CONTROLLER_TAB_Y);
        this.backButton.setPosition(RecipeBookLayout.CONTROLLER_TAB_PREV_X, RecipeBookLayout.CONTROLLER_TAB_Y);
    }

    @Inject(method = "updateCollections", at = @At("HEAD"), cancellable = true)
    private void coraline$updateCollectionsForScrolling(List<RecipeCollection> recipeCollections, boolean resetPageNumber, CallbackInfo ci) {
        recipeCollections.sort(Comparator.comparing(collection -> !collection.hasCraftable()));

        int previousRow = this.coraline$getRowOffset();
        this.recipeCollections = recipeCollections;

        int hiddenRows = this.coraline$getHiddenRows();
        this.coraline$scrollOffs = resetPageNumber || hiddenRows == 0 ? 0.0F : Math.min(previousRow, hiddenRows) / (float) hiddenRows;
        this.coraline$refreshButtons();
        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void coraline$renderScrollbar(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.coraline$draggingScrollbar) {
            this.coraline$scrollToMouse(mouseY);
        }

        int trackX = x + RecipeBookLayout.TRACK_X;
        int trackY = y + RecipeBookLayout.GRID_Y;
        CreativeFrame.drawInset(guiGraphics, trackX, trackY, RecipeBookLayout.TRACK_WIDTH, RecipeBookLayout.TRACK_HEIGHT);

        int knobY = trackY + 1 + (int) (RecipeBookLayout.KNOB_TRAVEL * this.coraline$scrollOffs);
        int knobU = this.coraline$getHiddenRows() > 0 ? RecipeBookLayout.KNOB_U_ENABLED : RecipeBookLayout.KNOB_U_DISABLED;
        guiGraphics.blit(RecipeTabStyle.CREATIVE_TABS_TEXTURE, trackX + 1, knobY, knobU, 0, RecipeBookLayout.KNOB_WIDTH, RecipeBookLayout.KNOB_HEIGHT);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void coraline$clickScrollbar(double mouseX, double mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight, CallbackInfoReturnable<Boolean> cir) {
        this.coraline$overlayWasOpen = this.overlay.isVisible();

        int trackX = panelX + RecipeBookLayout.TRACK_X;
        int trackY = panelY + RecipeBookLayout.GRID_Y;
        boolean isOverTrack = mouseX >= trackX && mouseX < trackX + RecipeBookLayout.TRACK_WIDTH && mouseY >= trackY && mouseY < trackY + RecipeBookLayout.TRACK_HEIGHT;

        if (button != 0 || this.overlay.isVisible() || !isOverTrack) {
            return;
        }

        this.lastClickedRecipe = null;
        this.lastClickedRecipeCollection = null;

        if (this.coraline$getHiddenRows() > 0) {
            this.coraline$draggingScrollbar = true;
            this.coraline$scrollToMouse((float) mouseY);
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void coraline$cycleUncraftableGroup(double mouseX, double mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight, CallbackInfoReturnable<Boolean> cir) {
        if (this.coraline$overlayWasOpen || button != 0 || this.lastClickedRecipe == null) {
            return;
        }

        if (this.lastClickedRecipeCollection.hasCraftable()) {
            this.coraline$pin(this.lastClickedRecipeCollection, this.lastClickedRecipe);
            return;
        }

        RecipeCollection collection = this.lastClickedRecipeCollection;
        List<Recipe<?>> variants = new ArrayList<>(collection.getDisplayRecipes(true));
        variants.addAll(collection.getDisplayRecipes(false));
        if (variants.size() < 2) {
            return;
        }

        Recipe<?> pinned = this.coraline$getPinned(collection);
        if (pinned != null && variants.contains(pinned)) {
            return;
        }

        RecipeGhostAccess ghost = (RecipeGhostAccess) ((RecipeUpdateListener) this.minecraft.screen).getRecipeBookComponent();
        if (ghost.coraline$isCycling(collection)) {
            this.lastClickedRecipe = null;
            this.lastClickedRecipeCollection = null;
            return;
        }

        this.lastClickedRecipe = variants.get(0);
        ghost.coraline$startCycle(collection, variants);
    }

    @Override
    public boolean coraline$scroll(double mouseX, double mouseY, double delta) {
        if (this.overlay.isVisible()) {
            ((RecipeStripAccess) this.overlay).coraline$step(delta > 0.0D ? -1 : 1);
            return true;
        }

        boolean isOverPanel = mouseX >= this.coraline$originX && mouseX < this.coraline$originX + RecipeBookLayout.PANEL_WIDTH && mouseY >= this.coraline$originY && mouseY < this.coraline$originY + RecipeBookLayout.PANEL_HEIGHT;
        int hiddenRows = this.coraline$getHiddenRows();
        if (!isOverPanel || hiddenRows == 0) {
            return false;
        }

        this.coraline$setScroll(this.coraline$scrollOffs - (float) (delta / hiddenRows));
        return true;
    }

    @Override
    public void coraline$endDrag() {
        this.coraline$draggingScrollbar = false;
    }

    @Unique
    private int coraline$getHiddenRows() {
        int totalRows = Mth.positiveCeilDiv(this.recipeCollections.size(), RecipeBookLayout.GRID_COLUMNS);
        return Math.max(0, totalRows - RecipeBookLayout.GRID_ROWS);
    }

    @Unique
    private int coraline$getRowOffset() {
        return Math.max((int) (this.coraline$scrollOffs * this.coraline$getHiddenRows() + 0.5D), 0);
    }

    @Unique
    private void coraline$scrollToMouse(float mouseY) {
        float knobTop = mouseY - (this.coraline$originY + RecipeBookLayout.GRID_Y + 1) - RecipeBookLayout.KNOB_HEIGHT / 2.0F;
        this.coraline$setScroll(knobTop / RecipeBookLayout.KNOB_TRAVEL);
    }

    @Unique
    private void coraline$setScroll(float scroll) {
        int previousRow = this.coraline$getRowOffset();
        this.coraline$scrollOffs = Mth.clamp(scroll, 0.0F, 1.0F);

        if (this.coraline$getRowOffset() != previousRow) {
            this.coraline$refreshButtons();
        }
    }

    @Unique
    private void coraline$refreshButtons() {
        int firstIndex = this.coraline$getRowOffset() * RecipeBookLayout.GRID_COLUMNS;

        for (int i = 0; i < this.buttons.size(); i++) {
            RecipeButton button = this.buttons.get(i);
            if (firstIndex + i < this.recipeCollections.size()) {
                button.init(this.recipeCollections.get(firstIndex + i), (RecipeBookPage) (Object) this);
                button.visible = true;
            } else {
                button.visible = false;
            }
        }
    }
}