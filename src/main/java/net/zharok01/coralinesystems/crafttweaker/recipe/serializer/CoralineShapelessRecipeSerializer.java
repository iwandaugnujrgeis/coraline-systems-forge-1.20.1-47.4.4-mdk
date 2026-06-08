package net.zharok01.coralinesystems.crafttweaker.recipe.serializer;

import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.helper.AccessibleElementsProvider;
import net.zharok01.coralinesystems.crafttweaker.recipe.CTShapelessRecipeWithMeta;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CoralineShapelessRecipeSerializer implements RecipeSerializer<CTShapelessRecipeWithMeta> {

    @Override
    public CTShapelessRecipeWithMeta fromJson(ResourceLocation id, JsonObject json) {
        return new CTShapelessRecipeWithMeta(
                "invalid_recipe", "", CraftingBookCategory.MISC,
                IItemStack.of(new ItemStack(Items.BARRIER)),
                new IIngredient[]{IItemStack.of(new ItemStack(Items.BARRIER))},
                null
        );
    }

    @Override
    public CTShapelessRecipeWithMeta fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        IIngredient[] ingredients = new IIngredient[count];
        for (int i = 0; i < count; i++) {
            ingredients[i] = IIngredient.fromIngredient(Ingredient.fromNetwork(buffer));
        }
        ItemStack output             = buffer.readItem();
        String group                 = buffer.readUtf();                               // ← new
        CraftingBookCategory cat     = buffer.readEnum(CraftingBookCategory.class);    // ← new
        return new CTShapelessRecipeWithMeta(
                recipeId.getPath(), group, cat, IItemStack.of(output), ingredients, null
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, CTShapelessRecipeWithMeta recipe) {
        buffer.writeVarInt(recipe.getIngredients().size());
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.toNetwork(buffer);
        }
        buffer.writeItem(AccessibleElementsProvider.get().registryAccess(recipe::getResultItem));
        buffer.writeUtf(recipe.getGroup());           // ← new
        buffer.writeEnum(recipe.category());          // ← new
    }
}