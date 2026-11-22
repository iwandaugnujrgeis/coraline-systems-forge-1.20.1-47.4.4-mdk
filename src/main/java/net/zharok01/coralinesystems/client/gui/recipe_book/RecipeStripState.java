package net.zharok01.coralinesystems.client.gui.recipe_book;

import net.minecraft.client.gui.screens.recipebook.RecipeCollection;

public interface RecipeStripState {
    boolean coraline$isOverStrip(double mouseX, double mouseY);

    boolean coraline$isShowing(RecipeCollection collection);

    int[] coraline$getSelectedSlotCenter();

}