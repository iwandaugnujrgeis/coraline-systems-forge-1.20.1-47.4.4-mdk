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

@Mixin(value = FurnaceRecipeManager.class, remap = false)
public abstract class FurnaceRecipeManagerMixin implements ICookingRecipeManager<SmeltingRecipe> {

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
        String fixed = this.fixRecipeName(name);
        CookingBookCategory resolved = category != null ? category : CookingBookCategory.MISC;

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
                "meta"
        ));
    }
}