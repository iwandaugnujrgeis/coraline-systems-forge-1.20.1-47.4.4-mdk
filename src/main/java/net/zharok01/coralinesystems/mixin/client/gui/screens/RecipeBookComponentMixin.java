package net.zharok01.coralinesystems.mixin.client.gui.screens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;
import net.zharok01.coralinesystems.client.gui.CoralineDragEnd;
import net.zharok01.coralinesystems.client.gui.CoralineScrollable;
import net.zharok01.coralinesystems.client.gui.creative_tabs.CreativeFrame;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeBookLayout;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeControllerAccess;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeGhostAccess;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipePageNavigation;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeStripState;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeTabRowAccess;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeTabStyle;
import net.zharok01.coralinesystems.network.OpenRecipeBookPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin implements CoralineScrollable, CoralineDragEnd, RecipeGhostAccess, RecipeControllerAccess {
    @Shadow @Final private List<RecipeBookTabButton> tabButtons;
    @Shadow @Final private RecipeBookPage recipeBookPage;
    @Shadow @Final protected GhostRecipe ghostRecipe;

    @Shadow private RecipeBookTabButton selectedTab;
    @Shadow private EditBox searchBox;
    @Shadow private ClientRecipeBook book;
    @Shadow protected StateSwitchingButton filterButton;
    @Shadow protected RecipeBookMenu<?> menu;
    @Shadow protected Minecraft minecraft;
    @Shadow private int xOffset;
    @Shadow private int width;
    @Shadow private int height;
    @Shadow private boolean visible;

    @Shadow public abstract void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots);

    @Unique private RecipeCollection coraline$cycleCollection;
    @Unique private List<Recipe<?>> coraline$cycleRecipes = List.of();
    @Unique private int coraline$cycleIndex;
    @Unique private int coraline$cycleTicks;
    @Unique private int coraline$cycleGraceTicks;
    @Unique private boolean coraline$cycleArmed;
    @Unique private boolean coraline$previewActive;

    @ModifyConstant(method = {"initVisuals", "render", "mouseClicked", "hasClickedOutside"}, constant = @Constant(intValue = 147))
    private int coraline$widenPanel(int vanillaWidth) {
        return RecipeBookLayout.PANEL_WIDTH;
    }

    @ModifyConstant(method = {"initVisuals", "isOffsetNextToMainGUI"}, constant = @Constant(intValue = 86))
    private int coraline$shiftPanelLeft(int vanillaOffset) {
        return RecipeBookLayout.PANEL_OFFSET_X;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void coraline$renderInactiveTabsBehindPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!this.visible) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        for (RecipeBookTabButton tab : this.tabButtons) {
            RecipeTabRowAccess tabAccess = (RecipeTabRowAccess) tab;
            tabAccess.coraline$setBehindPanelPass(true);
            tab.render(guiGraphics, mouseX, mouseY, partialTick);
            tabAccess.coraline$setBehindPanelPass(false);
        }
        guiGraphics.pose().popPose();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void coraline$drawCreativeStylePanel(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        CreativeFrame.drawPanel(guiGraphics, x, y, width, height);
        CreativeFrame.drawSearchField(guiGraphics, x + RecipeBookLayout.SEARCH_FIELD_X, y + RecipeBookLayout.SEARCH_FIELD_Y, RecipeBookLayout.SEARCH_FIELD_WIDTH);

        String categoryKey = this.selectedTab.getCategory().name().toLowerCase(Locale.ROOT);
        Component title = Component.translatable("gui.coraline_systems.recipe_book.category." + categoryKey);
        guiGraphics.drawString(this.minecraft.font, title, x + RecipeBookLayout.TITLE_X, y + RecipeBookLayout.TITLE_Y, RecipeBookLayout.TITLE_COLOR, false);
    }

    @Inject(method = "initVisuals", at = @At("HEAD"))
    private void coraline$disableCraftableFilter(CallbackInfo ci) {
        this.book.setFiltering(this.menu.getRecipeBookType(), false);
    }

    @Inject(method = "initVisuals", at = @At("TAIL"))
    private void coraline$layoutSearchRow(CallbackInfo ci) {
        int panelLeft = this.coraline$getPanelLeft();
        int panelTop = this.coraline$getPanelTop();
        int padding = RecipeBookLayout.SEARCH_TEXT_PADDING;

        this.searchBox.setBordered(false);
        this.searchBox.setHint(Component.empty());
        this.searchBox.setX(panelLeft + RecipeBookLayout.SEARCH_FIELD_X + padding);
        this.searchBox.setY(panelTop + RecipeBookLayout.SEARCH_FIELD_Y + padding);
        this.searchBox.setWidth(RecipeBookLayout.SEARCH_FIELD_WIDTH - padding * 2);
        this.searchBox.setHeight(RecipeBookLayout.SEARCH_TEXT_HEIGHT);

        this.filterButton.visible = false;
    }

    @Inject(method = "updateTabs", at = @At("TAIL"))
    private void coraline$layoutTabsAbovePanel(CallbackInfo ci) {
        int panelLeft = this.coraline$getPanelLeft();
        int tabY = this.coraline$getPanelTop() - RecipeTabStyle.TAB_VISIBLE_HEIGHT;
        int leftTabCount = 0;

        for (RecipeBookTabButton tab : this.tabButtons) {
            if (!tab.visible) {
                continue;
            }

            int tabX;
            int spriteColumn;
            if (RecipeTabStyle.isAllRecipesTab(tab.getCategory())) {
                tabX = panelLeft + RecipeBookLayout.PANEL_WIDTH - RecipeTabStyle.TAB_WIDTH;
                spriteColumn = RecipeTabStyle.SPRITE_COLUMN_LAST;
            } else {
                tabX = panelLeft + RecipeTabStyle.TAB_STRIDE * leftTabCount;
                spriteColumn = leftTabCount == 0 ? RecipeTabStyle.SPRITE_COLUMN_FIRST : RecipeTabStyle.SPRITE_COLUMN_MIDDLE;
                leftTabCount++;
            }

            tab.setWidth(RecipeTabStyle.TAB_WIDTH);
            tab.setHeight(RecipeTabStyle.TAB_HEIGHT);
            tab.setPosition(tabX, tabY);
            ((RecipeTabRowAccess) tab).coraline$setSpriteColumn(spriteColumn);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void coraline$switchTabFromController(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        int direction = mouseX == RecipeBookLayout.CONTROLLER_TAB_NEXT_X ? 1 : mouseX == RecipeBookLayout.CONTROLLER_TAB_PREV_X ? -1 : 0;
        if (mouseY != RecipeBookLayout.CONTROLLER_TAB_Y || direction == 0 || !this.visible) {
            return;
        }

        List<RecipeBookTabButton> rowOrder = this.tabButtons.stream().filter(tab -> tab.visible).sorted(Comparator.comparingInt(AbstractWidget::getX)).toList();
        RecipeBookTabButton target = rowOrder.get(Math.floorMod(rowOrder.indexOf(this.selectedTab) + direction, rowOrder.size()));

        this.recipeBookPage.setInvisible();
        ((RecipeBookComponent) (Object) this).mouseClicked(target.getX() + 1, target.getY() + 1, 0);
        cir.setReturnValue(true);
    }

    @Override
    public boolean coraline$scroll(double mouseX, double mouseY, double delta) {
        return this.visible && ((CoralineScrollable) this.recipeBookPage).coraline$scroll(mouseX, mouseY, delta);
    }

    @Override
    public void coraline$endDrag() {
        ((CoralineDragEnd) this.recipeBookPage).coraline$endDrag();
    }

    @Override
    public List<int[]> coraline$collectSnapTargets() {
        List<int[]> targets = new ArrayList<>();
        if (!this.visible) {
            return targets;
        }

        if (this.coraline$isStripOpen()) {
            int[] center = ((RecipeStripState) this.recipeBookPage).coraline$getSelectedSlotCenter();
            targets.add(new int[]{center[0], center[1], RecipeBookLayout.SNAP_RANGE_STRIP});
            return targets;
        }

        for (RecipeBookTabButton tab : this.tabButtons) {
            if (tab.visible) {
                targets.add(new int[]{tab.getX() + RecipeTabStyle.TAB_WIDTH / 2, tab.getY() + RecipeTabStyle.TAB_VISIBLE_HEIGHT / 2, RecipeBookLayout.SNAP_RANGE_TAB});
            }
        }

        int searchCenterX = this.coraline$getPanelLeft() + RecipeBookLayout.SEARCH_FIELD_X + RecipeBookLayout.SEARCH_FIELD_WIDTH / 2;
        int searchCenterY = this.coraline$getPanelTop() + RecipeBookLayout.SEARCH_FIELD_Y + RecipeBookLayout.SEARCH_FIELD_HEIGHT / 2;
        targets.add(new int[]{searchCenterX, searchCenterY, RecipeBookLayout.SNAP_RANGE_SEARCH});

        ((RecipePageNavigation) this.recipeBookPage).coraline$collectSnapTargets(targets);
        return targets;
    }

    @Override
    public boolean coraline$isOverVariantCell(double mouseX, double mouseY) {
        return this.visible && ((RecipePageNavigation) this.recipeBookPage).coraline$isOverVariantCell(mouseX, mouseY);
    }

    @Override
    public boolean coraline$isStripOpen() {
        return this.visible && ((RecipePageNavigation) this.recipeBookPage).coraline$isStripOpen();
    }

    @Override
    public void coraline$stepStrip(int direction) {
        ((RecipePageNavigation) this.recipeBookPage).coraline$stepStrip(direction);
    }

    @Override
    public void coraline$closeStrip() {
        this.recipeBookPage.setInvisible();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void coraline$advanceVariantCycle(CallbackInfo ci) {
        if (this.coraline$cycleCollection == null) {
            return;
        }

        Recipe<?> shown = this.coraline$cycleRecipes.get(this.coraline$cycleIndex);
        if (this.ghostRecipe.getRecipe() != shown) {
            if (this.coraline$cycleArmed || --this.coraline$cycleGraceTicks <= 0) {
                this.coraline$stopCycle();
            }
            return;
        }

        this.coraline$cycleArmed = true;
        if (!Screen.hasControlDown() && ++this.coraline$cycleTicks >= RecipeBookLayout.VARIANT_GHOST_CYCLE_TICKS) {
            this.coraline$cycleTicks = 0;
            this.coraline$cycleIndex = (this.coraline$cycleIndex + 1) % this.coraline$cycleRecipes.size();
            this.ghostRecipe.clear();
            this.setupGhostRecipe(this.coraline$cycleRecipes.get(this.coraline$cycleIndex), this.menu.slots);
        }
    }

    @Override
    public void coraline$startCycle(RecipeCollection collection, List<Recipe<?>> recipes) {
        this.coraline$cycleCollection = collection;
        this.coraline$cycleRecipes = recipes;
        this.coraline$cycleIndex = 0;
        this.coraline$cycleTicks = 0;
        this.coraline$cycleGraceTicks = RecipeBookLayout.VARIANT_GHOST_ARRIVAL_TIMEOUT_TICKS;
        this.coraline$cycleArmed = false;
        this.coraline$previewActive = false;
    }

    @Override
    public void coraline$stopCycle() {
        this.coraline$cycleCollection = null;
        this.coraline$cycleRecipes = List.of();
    }

    @Override
    public boolean coraline$isCycling(RecipeCollection collection) {
        return this.coraline$cycleCollection == collection;
    }

    @Override
    public void coraline$previewRecipe(Recipe<?> recipe) {
        if (!this.coraline$isGridEmpty()) {
            return;
        }

        this.ghostRecipe.clear();
        this.setupGhostRecipe(recipe, this.menu.slots);
        this.coraline$previewActive = true;
    }

    @Override
    public void coraline$commitPreview() {
        this.coraline$previewActive = false;
    }

    @Override
    public void coraline$clearPreview() {
        if (this.coraline$previewActive) {
            this.ghostRecipe.clear();
            this.coraline$previewActive = false;
        }
    }

    @Override
    @Nullable
    public Recipe<?> coraline$getGhostRecipe() {
        return this.ghostRecipe.getRecipe();
    }

    @Unique
    private boolean coraline$isGridEmpty() {
        int firstGridSlot = this.menu.getResultSlotIndex() == 0 ? 1 : 0;
        int gridSize = this.menu.getGridWidth() * this.menu.getGridHeight();

        for (int i = firstGridSlot; i < firstGridSlot + gridSize; i++) {
            if (this.menu.slots.get(i).hasItem()) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private int coraline$getPanelLeft() {
        return (this.width - RecipeBookLayout.PANEL_WIDTH) / 2 - this.xOffset;
    }

    @Unique
    private int coraline$getPanelTop() {
        return (this.height - RecipeBookLayout.PANEL_HEIGHT) / 2;
    }

    @Inject(method = "toggleVisibility", at = @At("HEAD"))
    private void coraline$onToggleVisibility(CallbackInfo ci) {
        if (!this.visible) {
            CoralinePacketHandler.sendToServer(new OpenRecipeBookPacket());
        }
    }
}