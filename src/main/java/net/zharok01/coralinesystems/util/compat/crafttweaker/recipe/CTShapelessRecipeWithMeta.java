package net.zharok01.coralinesystems.util.compat.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.fun.RecipeFunction1D;
import com.blamejared.crafttweaker.api.recipe.type.CTShapelessRecipe;
import net.zharok01.coralinesystems.registry.CoralineSerializers;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CTShapelessRecipeWithMeta extends CTShapelessRecipe {

    private final String group;

    public CTShapelessRecipeWithMeta(String name, String group, CraftingBookCategory category,
                                     IItemStack output, IIngredient[] ingredients,
                                     @Nullable RecipeFunction1D function) {
        super(name, category, output, ingredients, function);
        this.group = group;
    }

    @Override
    public @NotNull String getGroup() {
        return this.group;
    }

    @Override
    public @NotNull RecipeSerializer<CTShapelessRecipeWithMeta> getSerializer() {
        return CoralineSerializers.SHAPELESS_WITH_META.get();
    }
}
