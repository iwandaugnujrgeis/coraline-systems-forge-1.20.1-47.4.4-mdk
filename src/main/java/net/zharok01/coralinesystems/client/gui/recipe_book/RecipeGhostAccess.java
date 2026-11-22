package net.zharok01.coralinesystems.client.gui.recipe_book;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.Recipe;

public interface RecipeGhostAccess {
    void coraline$startCycle(RecipeCollection collection, List<Recipe<?>> recipes);

    void coraline$stopCycle();

    boolean coraline$isCycling(RecipeCollection collection);

    void coraline$previewRecipe(Recipe<?> recipe);

    void coraline$commitPreview();

    void coraline$clearPreview();

    @Nullable
    Recipe<?> coraline$getGhostRecipe();
}