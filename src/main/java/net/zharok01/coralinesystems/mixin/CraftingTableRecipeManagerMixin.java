package net.zharok01.coralinesystems.mixin;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.MirrorAxis;
import com.blamejared.crafttweaker.api.recipe.fun.RecipeFunction1D;
import com.blamejared.crafttweaker.api.recipe.fun.RecipeFunction2D;
import com.blamejared.crafttweaker.api.recipe.manager.CraftingTableRecipeManager;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import net.zharok01.coralinesystems.crafttweaker.recipe.CTShapedRecipeWithMeta;
import net.zharok01.coralinesystems.crafttweaker.recipe.CTShapelessRecipeWithMeta;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.openzen.zencode.java.ZenCodeType;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

// FIX #2: Declare that this mixin implements IRecipeManager<CraftingRecipe>.
//
// CraftingTableRecipeManager already implements this interface, so Mixin will NOT
// add a duplicate interface declaration to the target class — it just skips it.
// The benefit for us is compile-time: "this" is now typed as IRecipeManager<CraftingRecipe>
// by the Java compiler, so no cast is needed anywhere inside these methods.
//
// Since the class is abstract, we are NOT required to implement any interface methods here.
// The concrete implementations already live in CraftingTableRecipeManager itself.
@Mixin(value = CraftingTableRecipeManager.class, remap = false)
public abstract class CraftingTableRecipeManagerMixin implements IRecipeManager<CraftingRecipe> {

    // FIX #1: @Unique is removed from all three methods.
    //
    // Why the original code caused the "name does not match pattern" warning:
    //   The Mixin AP (and IntelliJ Mixin plugin) enforce a naming convention for @Unique members:
    //   the method name MUST contain at least one underscore or dollar sign (pattern: .+[_$].+).
    //   This protects against future name collisions in the target class.
    //
    // Why we CANNOT follow that convention here:
    //   @ZenCodeType.Method uses the actual Java method name as the ZenScript method name —
    //   there is no "value" parameter to override it. If we renamed to
    //   "coralinesystems$addShapedMeta", ZenScript would expose that name verbatim, and
    //   a dollar sign is not a valid character in a ZenScript identifier. The scripts would
    //   break entirely.
    //
    // Why removing @Unique is safe:
    //   @Unique is a safety marker that tells Mixin a method is intentionally new. Without it,
    //   Mixin still copies the method verbatim into the target class — the injection still works.
    //   The only risk is an undiscovered name collision, but none of these three names
    //   (addShapedMeta, addShapedMirroredMeta, addShapelessMeta) exist anywhere in
    //   CraftingTableRecipeManager or IRecipeManager, so there is nothing to collide with.
    //
    // The remaining @SuppressWarnings("unused") silences the "method is never used" warning.
    //   IntelliJ only tracks Java callers; it cannot see that ZenScript will invoke these
    //   methods reflectively at script-load time. The warning is a false positive.

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
        // FIX #2 in action: "this" is IRecipeManager<CraftingRecipe> — no cast needed.
        // fixRecipeName() is a default method on IRecipeManager, so it's callable directly.
        String fixed = this.fixRecipeName(recipeName);
        CraftingBookCategory resolved = category != null ? category : CraftingBookCategory.MISC;
        CraftTweakerAPI.apply(new ActionAddRecipe<>(
                this,  // already IRecipeManager<CraftingRecipe> — no cast, no warning
                new CTShapedRecipeWithMeta(fixed, group, resolved, output, ingredients, MirrorAxis.NONE, recipeFunction),
                "shaped with meta"
        ));
    }

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