package net.zharok01.coralinesystems.client.gui.recipe_book;

import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.Recipe;

public interface RecipePinAccess {
    @Nullable
    Recipe<?> coraline$getPinned(RecipeCollection collection);

    void coraline$pin(RecipeCollection collection, Recipe<?> recipe);

    Recipe<?> coraline$getShownRecipe(RecipeCollection collection);
}