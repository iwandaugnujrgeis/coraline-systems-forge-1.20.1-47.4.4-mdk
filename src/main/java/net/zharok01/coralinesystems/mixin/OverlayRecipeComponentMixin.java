package net.zharok01.coralinesystems.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeBookLayout;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeGhostAccess;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipePinAccess;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeStripAccess;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeStripState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OverlayRecipeComponent.class)
public abstract class OverlayRecipeComponentMixin implements RecipeStripAccess, RecipeStripState {
    @Shadow private boolean isVisible;
    @Shadow private Minecraft minecraft;
    @Shadow private RecipeCollection collection;
    @Shadow @Nullable private Recipe<?> lastRecipeClicked;

    @Unique private List<Recipe<?>> coraline$recipes = List.of();
    @Unique private int coraline$selected;
    @Unique private int coraline$stripX;
    @Unique private int coraline$stripY;
    @Unique private RecipePinAccess coraline$pins;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void coraline$openStrip(Minecraft minecraft, RecipeCollection collection, int cellX, int cellY, int anchorX, int anchorY, float cellWidth, CallbackInfo ci) {
        this.minecraft = minecraft;
        this.collection = collection;
        this.coraline$recipes = new ArrayList<>(collection.getDisplayRecipes(true));
        this.coraline$recipes.addAll(collection.getDisplayRecipes(false));
        this.coraline$selected = this.coraline$recipes.indexOf(this.coraline$pins.coraline$getShownRecipe(collection));

        int selectedSlotTop = RecipeBookLayout.STRIP_CAP_HEIGHT + this.coraline$getSelectedSlot() * RecipeBookLayout.CELL_SIZE;
        int maxY = minecraft.getWindow().getGuiScaledHeight() - this.coraline$getStripHeight();
        this.coraline$stripX = cellX - RecipeBookLayout.STRIP_SLOT_INSET_X;
        this.coraline$stripY = Mth.clamp(cellY - selectedSlotTop, 0, maxY);

        this.isVisible = true;
        this.lastRecipeClicked = null;

        RecipeGhostAccess ghost = this.coraline$ghostAccess();
        ghost.coraline$stopCycle();
        ghost.coraline$previewRecipe(this.coraline$recipes.get(this.coraline$selected));
        ci.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void coraline$clickStrip(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0 || !this.coraline$isOverStrip(mouseX, mouseY)) {
            cir.setReturnValue(false);
            return;
        }

        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

        int relativeY = Mth.floor(mouseY) - this.coraline$stripY;
        int stripHeight = this.coraline$getStripHeight();

        this.lastRecipeClicked = null;
        if (relativeY < RecipeBookLayout.STRIP_CAP_HEIGHT) {
            this.coraline$step(-1);
        } else if (relativeY >= stripHeight - RecipeBookLayout.STRIP_CAP_HEIGHT) {
            this.coraline$step(1);
        } else {
            int clickedSlot = (relativeY - RecipeBookLayout.STRIP_CAP_HEIGHT) / RecipeBookLayout.CELL_SIZE;
            int selectedSlot = this.coraline$getSelectedSlot();

            if (clickedSlot == selectedSlot) {
                Recipe<?> picked = this.coraline$recipes.get(this.coraline$selected);
                RecipeGhostAccess ghost = this.coraline$ghostAccess();

                this.coraline$pins.coraline$pin(this.collection, picked);

                boolean isAlreadyShown = !this.collection.isCraftable(picked) && ghost.coraline$getGhostRecipe() == picked;
                this.lastRecipeClicked = isAlreadyShown ? null : picked;
                ghost.coraline$commitPreview();
                this.isVisible = false;
            } else {
                this.coraline$step(clickedSlot - selectedSlot);
            }
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void coraline$renderStrip(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ci.cancel();
        if (!this.isVisible) {
            return;
        }

        int slotCount = this.coraline$getSlotCount();
        int selectedSlot = this.coraline$getSelectedSlot();
        int stripHeight = this.coraline$getStripHeight();
        int slotsLeft = this.coraline$stripX + RecipeBookLayout.STRIP_SLOT_INSET_X;
        int slotsTop = this.coraline$stripY + RecipeBookLayout.STRIP_CAP_HEIGHT;
        ResourceLocation texture = slotCount == 2 ? RecipeBookLayout.STRIP_PAIR_TEXTURE : RecipeBookLayout.STRIP_TRIPLE_TEXTURE;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, RecipeBookLayout.STRIP_Z);
        RenderSystem.enableBlend();
        guiGraphics.blit(texture, this.coraline$stripX, this.coraline$stripY, 0.0F, 0.0F, RecipeBookLayout.STRIP_WIDTH, stripHeight, RecipeBookLayout.STRIP_WIDTH, stripHeight);

        ItemStack hoveredStack = ItemStack.EMPTY;
        for (int slot = 0; slot < slotCount; slot++) {
            Recipe<?> recipe = this.coraline$recipes.get(Math.floorMod(this.coraline$selected + slot - selectedSlot, this.coraline$recipes.size()));
            ItemStack stack = recipe.getResultItem(this.collection.registryAccess());
            int slotY = slotsTop + slot * RecipeBookLayout.CELL_SIZE;
            boolean isHovered = mouseX >= slotsLeft && mouseX < slotsLeft + RecipeBookLayout.CELL_SIZE && mouseY >= slotY && mouseY < slotY + RecipeBookLayout.CELL_SIZE;

            guiGraphics.renderFakeItem(stack, slotsLeft + 1, slotY + 1);
            if (!this.collection.isCraftable(recipe)) {
                guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), slotsLeft + 1, slotY + 1, slotsLeft + 17, slotY + 17, RecipeBookLayout.UNCRAFTABLE_OVERLAY_COLOR);
            }
            if (isHovered) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, RecipeBookLayout.OVERLAY_Z);
                guiGraphics.fill(slotsLeft + 1, slotY + 1, slotsLeft + 17, slotY + 17, RecipeBookLayout.HOVER_OVERLAY_COLOR);
                guiGraphics.pose().popPose();
                hoveredStack = stack;
            }
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, RecipeBookLayout.OVERLAY_Z);

        if (this.coraline$isOverStrip(mouseX, mouseY)) {
            int relativeY = mouseY - this.coraline$stripY;
            int arrowX = this.coraline$stripX + RecipeBookLayout.ARROW_X;
            if (relativeY < RecipeBookLayout.STRIP_CAP_HEIGHT) {
                guiGraphics.blit(RecipeBookLayout.ARROW_UP_TEXTURE, arrowX, this.coraline$stripY + RecipeBookLayout.ARROW_UP_OFFSET_Y, 0.0F, 0.0F, RecipeBookLayout.ARROW_WIDTH, RecipeBookLayout.ARROW_HEIGHT, RecipeBookLayout.ARROW_WIDTH, RecipeBookLayout.ARROW_HEIGHT);
            } else if (relativeY >= stripHeight - RecipeBookLayout.STRIP_CAP_HEIGHT) {
                guiGraphics.blit(RecipeBookLayout.ARROW_DOWN_TEXTURE, arrowX, this.coraline$stripY + stripHeight - RecipeBookLayout.STRIP_CAP_HEIGHT + RecipeBookLayout.ARROW_DOWN_OFFSET_Y, 0.0F, 0.0F, RecipeBookLayout.ARROW_WIDTH, RecipeBookLayout.ARROW_HEIGHT, RecipeBookLayout.ARROW_WIDTH, RecipeBookLayout.ARROW_HEIGHT);
            }
        }

        guiGraphics.blit(RecipeBookLayout.SELECTOR_TEXTURE, this.coraline$stripX + (RecipeBookLayout.STRIP_WIDTH - RecipeBookLayout.SELECTOR_SIZE) / 2, slotsTop + selectedSlot * RecipeBookLayout.CELL_SIZE - RecipeBookLayout.SELECTOR_MARGIN, 0.0F, 0.0F, RecipeBookLayout.SELECTOR_SIZE, RecipeBookLayout.SELECTOR_SIZE, RecipeBookLayout.SELECTOR_SIZE, RecipeBookLayout.SELECTOR_SIZE);
        guiGraphics.pose().popPose();

        if (!hoveredStack.isEmpty()) {
            guiGraphics.renderTooltip(this.minecraft.font, hoveredStack, mouseX, mouseY);
        }
        guiGraphics.pose().popPose();
    }

    @Inject(method = "setVisible", at = @At("HEAD"))
    private void coraline$dropPreviewOnClose(boolean visible, CallbackInfo ci) {
        if (!visible && this.isVisible) {
            this.coraline$ghostAccess().coraline$clearPreview();
        }
    }

    @Override
    public void coraline$step(int direction) {
        this.coraline$selected = Math.floorMod(this.coraline$selected + direction, this.coraline$recipes.size());
        this.coraline$ghostAccess().coraline$previewRecipe(this.coraline$recipes.get(this.coraline$selected));
    }

    @Override
    public void coraline$bindPins(RecipePinAccess pins) {
        this.coraline$pins = pins;
    }

    @Override
    public boolean coraline$isOverStrip(double mouseX, double mouseY) {
        return this.isVisible
                && mouseX >= this.coraline$stripX && mouseX < this.coraline$stripX + RecipeBookLayout.STRIP_WIDTH
                && mouseY >= this.coraline$stripY && mouseY < this.coraline$stripY + this.coraline$getStripHeight();
    }

    @Override
    public boolean coraline$isShowing(RecipeCollection collection) {
        return this.isVisible && this.collection == collection;
    }

    @Override
    public int[] coraline$getSelectedSlotCenter() {
        int slotTop = this.coraline$stripY + RecipeBookLayout.STRIP_CAP_HEIGHT + this.coraline$getSelectedSlot() * RecipeBookLayout.CELL_SIZE;
        return new int[]{this.coraline$stripX + RecipeBookLayout.STRIP_WIDTH / 2, slotTop + RecipeBookLayout.CELL_SIZE / 2};
    }

    @Unique
    private RecipeGhostAccess coraline$ghostAccess() {
        return (RecipeGhostAccess) ((RecipeUpdateListener) this.minecraft.screen).getRecipeBookComponent();
    }

    @Unique
    private int coraline$getSlotCount() {
        return this.coraline$recipes.size() == 2 ? 2 : 3;
    }

    @Unique
    private int coraline$getSelectedSlot() {
        return this.coraline$recipes.size() <= RecipeBookLayout.STRIP_MAX_SLOTS ? this.coraline$selected : RecipeBookLayout.STRIP_CAROUSEL_SELECTED_SLOT;
    }

    @Unique
    private int coraline$getStripHeight() {
        return RecipeBookLayout.STRIP_CAP_HEIGHT * 2 + this.coraline$getSlotCount() * RecipeBookLayout.CELL_SIZE;
    }
}
