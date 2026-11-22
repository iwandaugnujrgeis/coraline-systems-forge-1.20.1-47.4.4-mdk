package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeArrowButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends AbstractContainerScreen<CraftingMenu> {
    private CraftingScreenMixin(CraftingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void coraline$replaceRecipeButton(CallbackInfo callbackInfo) {
        ImageButton vanillaButton = RecipeArrowButton.coraline$findVanillaButton(this);

        this.removeWidget(vanillaButton);
        this.addRenderableWidget(new RecipeArrowButton(vanillaButton, RecipeArrowButton.CRAFTING_LAYOUT, () -> this.leftPos, () -> this.topPos));
    }
}