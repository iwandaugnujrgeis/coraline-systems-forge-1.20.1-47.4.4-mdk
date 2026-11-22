package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeArrowButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {
    private InventoryScreenMixin(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void coraline$replaceRecipeButton(CallbackInfo callbackInfo) {
        ImageButton vanillaButton = RecipeArrowButton.coraline$findVanillaButton(this);

        if (vanillaButton == null) {
            return;
        }

        this.removeWidget(vanillaButton);
        this.addRenderableWidget(new RecipeArrowButton(vanillaButton, RecipeArrowButton.INVENTORY_LAYOUT, () -> this.leftPos, () -> this.topPos));
    }
}