package net.zharok01.coralinesystems.mixin.client.gui.screens;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeBookLayout;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipePinAccess;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeStripState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeButton.class)
public abstract class RecipeButtonMixin extends AbstractWidget {
    @Shadow private RecipeCollection collection;
    @Shadow private float time;
    @Shadow private float animationTime;
    @Shadow private int currentIndex;

    @Unique private RecipeStripState coraline$strip;
    @Unique private RecipePinAccess coraline$pins;

    @Shadow protected abstract List<Recipe<?>> getOrderedRecipes();

    protected RecipeButtonMixin() {
        super(0, 0, 0, 0, CommonComponents.EMPTY);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void coraline$useSlotSize(CallbackInfo ci) {
        this.setWidth(RecipeBookLayout.CELL_SIZE);
        this.setHeight(RecipeBookLayout.CELL_SIZE);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void coraline$rememberStrip(RecipeCollection collection, RecipeBookPage recipeBookPage, CallbackInfo ci) {
        this.coraline$strip = (RecipeStripState) recipeBookPage;
        this.coraline$pins = (RecipePinAccess) recipeBookPage;
    }

    @Inject(method = "getWidth", at = @At("HEAD"), cancellable = true)
    private void coraline$reportSlotWidth(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.width);
    }

    @Inject(method = "getTooltipText", at = @At("HEAD"), cancellable = true)
    private void coraline$dropMoreRecipesTip(CallbackInfoReturnable<List<Component>> cir) {
        ItemStack itemStack = this.getOrderedRecipes().get(this.currentIndex).getResultItem(this.collection.registryAccess());
        cir.setReturnValue(Screen.getTooltipFromItem(Minecraft.getInstance(), itemStack));
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void coraline$renderAsContainerSlot(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!Screen.hasControlDown()) {
            this.time += partialTick;
        }

        int x = this.getX();
        int y = this.getY();

        float iconScale = 1.0F;
        if (this.animationTime > 0.0F) {
            iconScale = 1.0F + RecipeBookLayout.HIGHLIGHT_ICON_POP * (float) Math.sin(this.animationTime / RecipeBookLayout.HIGHLIGHT_BOUNCE_TICKS * (float) Math.PI);
            this.animationTime -= partialTick;
        }

        guiGraphics.blit(RecipeBookLayout.SLOT_TEXTURE, x, y, RecipeBookLayout.SLOT_U, RecipeBookLayout.SLOT_V, RecipeBookLayout.CELL_SIZE, RecipeBookLayout.CELL_SIZE);

        List<Recipe<?>> recipes = this.getOrderedRecipes();
        int pinnedIndex = recipes.indexOf(this.coraline$pins.coraline$getPinned(this.collection));
        boolean isPinned = pinnedIndex >= 0;
        this.currentIndex = isPinned ? pinnedIndex : Mth.floor(this.time / RecipeBookLayout.ITEM_CYCLE_TICKS) % recipes.size();
        ItemStack icon = recipes.get(this.currentIndex).getResultItem(this.collection.registryAccess());
        boolean isVeiled = isPinned ? !this.collection.isCraftable(recipes.get(this.currentIndex)) : !this.collection.hasCraftable();

        guiGraphics.pose().pushPose();
        this.coraline$scaleAroundCenter(guiGraphics, x, y, iconScale);
        guiGraphics.renderFakeItem(icon, x + 1, y + 1);
        if (isVeiled) {
            guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), x + 1, y + 1, x + 17, y + 17, RecipeBookLayout.UNCRAFTABLE_OVERLAY_COLOR);
        }
        guiGraphics.pose().popPose();

        boolean isHovered = this.isHoveredOrFocused() && !this.coraline$strip.coraline$isOverStrip(mouseX, mouseY);
        boolean isStripOpenHere = this.coraline$strip.coraline$isShowing(this.collection);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, RecipeBookLayout.OVERLAY_Z);

        if (recipes.size() > 1 && !isStripOpenHere) {
            int pipOutlineColor = isPinned ? RecipeBookLayout.VARIANT_PIP_PINNED_OUTLINE_COLOR : RecipeBookLayout.VARIANT_PIP_OUTLINE_COLOR;
            int pipColor = isPinned ? RecipeBookLayout.VARIANT_PIP_PINNED_COLOR : RecipeBookLayout.VARIANT_PIP_COLOR;
            guiGraphics.fill(x + 13, y + 13, x + 17, y + 17, pipOutlineColor);
            guiGraphics.fill(x + 14, y + 14, x + 16, y + 16, pipColor);

            if (isVeiled) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, -RecipeBookLayout.OVERLAY_Z);
                guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), x + 13, y + 13, x + 17, y + 17, RecipeBookLayout.UNCRAFTABLE_OVERLAY_COLOR);
                guiGraphics.pose().popPose();
            }
        }

        if (isHovered) {
            guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, RecipeBookLayout.HOVER_OVERLAY_COLOR);
        }

        guiGraphics.pose().popPose();
        ci.cancel();
    }

    @Unique
    private void coraline$scaleAroundCenter(GuiGraphics guiGraphics, int x, int y, float scale) {
        int center = RecipeBookLayout.CELL_SIZE / 2;
        guiGraphics.pose().translate(x + center, y + center, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.pose().translate(-(x + center), -(y + center), 0.0F);
    }
}