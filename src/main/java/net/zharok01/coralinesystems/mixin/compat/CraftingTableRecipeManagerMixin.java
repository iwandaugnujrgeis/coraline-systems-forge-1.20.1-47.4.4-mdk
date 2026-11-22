package net.zharok01.coralinesystems.mixin.compat;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.MirrorAxis;
import com.blamejared.crafttweaker.api.recipe.fun.RecipeFunction1D;
import com.blamejared.crafttweaker.api.recipe.fun.RecipeFunction2D;
import com.blamejared.crafttweaker.api.recipe.manager.CraftingTableRecipeManager;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import net.zharok01.coralinesystems.util.compat.crafttweaker.recipe.CTShapedRecipeWithMeta;
import net.zharok01.coralinesystems.util.compat.crafttweaker.recipe.CTShapelessRecipeWithMeta;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.openzen.zencode.java.ZenCodeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@Mixin(value = CraftingTableRecipeManager.class, remap = false)
public abstract class CraftingTableRecipeManagerMixin implements IRecipeManager<CraftingRecipe> {
    
    @Unique
    @SuppressWarnings("unused")
    @ZenCodeType.Method
    public void addShapedMeta(
            String recipeName,
            IItemStack output,
            IIngredient[][] ingredients,
            @ZenCodeType.Optional("\"\"") String group,
            @ZenCodeType.Optional @Nullable CraftingBookCategory category,
            @ZenCodeType.Optional @Nullable RecipeFunction2D recipeFunction
    ) {
        String fixed = this.fixRecipeName(recipeName);
        CraftingBookCategory resolved = category != null ? category : CraftingBookCategory.MISC;
        CraftTweakerAPI.apply(new ActionAddRecipe<>(
                this,
                new CTShapedRecipeWithMeta(fixed, group, resolved, output, ingredients, MirrorAxis.NONE, recipeFunction),
                "shaped with meta"
        ));
    }

    @Unique
    @SuppressWarnings("unused")
    @ZenCodeType.Method
    public void addShapedMirroredMeta(
            String recipeName,
            MirrorAxis mirrorAxis,
            IItemStack output,
            IIngredient[][] ingredients,
            @ZenCodeType.Optional("\"\"") String group,
            @ZenCodeType.Optional @Nullable CraftingBookCategory category,
            @ZenCodeType.Optional @Nullable RecipeFunction2D recipeFunction
    ) {
        String fixed = this.fixRecipeName(recipeName);
        CraftingBookCategory resolved = category != null ? category : CraftingBookCategory.MISC;
        CraftTweakerAPI.apply(new ActionAddRecipe<>(
                this,
                new CTShapedRecipeWithMeta(fixed, group, resolved, output, ingredients, mirrorAxis, recipeFunction),
                "mirrored shaped with meta"
        ));
    }

    @Unique
    @SuppressWarnings("unused")
    @ZenCodeType.Method
    public void addShapelessMeta(
            String recipeName,
            IItemStack output,
            IIngredient[] ingredients,
            @ZenCodeType.Optional("\"\"") String group,
            @ZenCodeType.Optional @Nullable CraftingBookCategory category,
            @ZenCodeType.Optional @Nullable RecipeFunction1D recipeFunction
    ) {
        String fixed = this.fixRecipeName(recipeName);
        CraftingBookCategory resolved = category != null ? category : CraftingBookCategory.MISC;
        CraftTweakerAPI.apply(new ActionAddRecipe<>(
                this,
                new CTShapelessRecipeWithMeta(fixed, group, resolved, output, ingredients, recipeFunction),
                "shapeless with meta"
        ));
    }
}