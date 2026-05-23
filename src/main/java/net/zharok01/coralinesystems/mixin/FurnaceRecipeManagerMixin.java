package net.zharok01.coralinesystems.mixin;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.FurnaceRecipeManager;
import com.blamejared.crafttweaker.api.recipe.manager.base.ICookingRecipeManager;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.openzen.zencode.java.ZenCodeType;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

// WHY THIS IS SIMPLER THAN THE CRAFTING TABLE CASE:
//
// SmeltingRecipe is a vanilla class. Its constructor signature is:
//   SmeltingRecipe(ResourceLocation id, String group, CookingBookCategory category,
//                  Ingredient input, ItemStack output, float xp, int cookTime)
//
// FurnaceRecipeManager.makeRecipe just hardcodes "" as the group — the slot is already there.
// We only need to fill it. No recipe subclass and no custom serializer are needed:
// vanilla's SimpleCookingSerializer already reads and writes group + category over the
// network, so the recipe book on the client displays correctly out of the box.
@Mixin(value = FurnaceRecipeManager.class, remap = false)
public abstract class FurnaceRecipeManagerMixin implements ICookingRecipeManager<SmeltingRecipe> {

    /**
     * Adds a smelting recipe with an explicit Recipe Book group and/or category.
     *
     * Group:    a grouping key shown in the recipe book, e.g. "iron_ingot".
     *           Recipes sharing the same group key appear collapsed under one icon.
     *           Defaults to "" (no grouping) if omitted.
     *
     * Category: which tab the recipe appears under in the Furnace recipe book.
     *           Valid values: blocks, food, misc  (misc is the default).
     *           ZenScript bracket:  <constant:minecraft:cookingbookcategory:blocks>
     *                               <constant:minecraft:cookingbookcategory:food>
     *                               <constant:minecraft:cookingbookcategory:misc>
     *
     * ZenScript examples:
     *
     *   // Iron ore → iron ingot, under the existing "iron_ingot" group, in the Blocks tab
     *   furnace.addRecipeMeta(
     *       "mymod:custom_iron",
     *       <item:minecraft:iron_ingot>,
     *       <tag:items:minecraft:iron_ores>,
     *       0.7, 200,
     *       "iron_ingot",
     *       <constant:minecraft:cookingbookcategory:blocks>
     *   );
     *
     *   // Porkchop → cooked porkchop, Food tab, default group
     *   furnace.addRecipeMeta(
     *       "mymod:alt_pork",
     *       <item:minecraft:cooked_porkchop>,
     *       <item:minecraft:porkchop>,
     *       0.35, 200,
     *       "",
     *       <constant:minecraft:cookingbookcategory:food>
     *   );
     *
     *   // Category only (group defaults to "")
     *   furnace.addRecipeMeta(
     *       "mymod:sand_to_glass",
     *       <item:minecraft:glass>,
     *       <item:minecraft:sand>,
     *       0.1, 200,
     *       "",
     *       <constant:minecraft:cookingbookcategory:blocks>
     *   );
     */
    @SuppressWarnings("unused")
    @ZenCodeType.Method
    public void addRecipeMeta(
            String name,
            IItemStack output,
            IIngredient input,
            float xp,
            int cookTime,
            @ZenCodeType.Optional("\"\"") String group,
            @ZenCodeType.Optional @Nullable CookingBookCategory category
    ) {
        // fixRecipeName is a default method on IRecipeManager (parent of ICookingRecipeManager).
        // "this" is ICookingRecipeManager<SmeltingRecipe>, so no cast is needed anywhere.
        String fixed = this.fixRecipeName(name);
        CookingBookCategory resolved = category != null ? category : CookingBookCategory.MISC;

        // We build SmeltingRecipe directly, passing `group` instead of the hardcoded "".
        // CraftTweakerConstants.rl(fixed) mirrors exactly what FurnaceRecipeManager.makeRecipe does.
        // Vanilla's SimpleCookingSerializer handles group + category on the wire automatically.
        CraftTweakerAPI.apply(new ActionAddRecipe<>(
                this,
                new SmeltingRecipe(
                        CraftTweakerConstants.rl(fixed),
                        group,
                        resolved,
                        input.asVanillaIngredient(),
                        output.getInternal(),
                        xp,
                        cookTime
                ),
                ""
        ));
    }
}