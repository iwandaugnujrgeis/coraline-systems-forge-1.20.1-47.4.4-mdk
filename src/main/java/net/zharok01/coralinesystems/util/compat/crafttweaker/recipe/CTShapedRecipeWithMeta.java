package net.zharok01.coralinesystems.util.compat.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.MirrorAxis;
import com.blamejared.crafttweaker.api.recipe.fun.RecipeFunction2D;
import com.blamejared.crafttweaker.api.recipe.type.CTShapedRecipe;
import net.zharok01.coralinesystems.registry.CoralineSerializers;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;

public class CTShapedRecipeWithMeta extends CTShapedRecipe {

    private final String group;

    public CTShapedRecipeWithMeta(String name, String group, CraftingBookCategory category,
                                  IItemStack output, IIngredient[][] ingredients,
                                  MirrorAxis mirrorAxis, @Nullable RecipeFunction2D function) {
        super(name, category, output, ingredients, mirrorAxis, function);
        this.group = group;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public RecipeSerializer<CTShapedRecipeWithMeta> getSerializer() {
        return CoralineSerializers.SHAPED_WITH_META.get();
    }
}