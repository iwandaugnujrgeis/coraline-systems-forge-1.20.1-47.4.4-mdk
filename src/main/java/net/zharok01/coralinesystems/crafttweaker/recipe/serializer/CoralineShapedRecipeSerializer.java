package net.zharok01.coralinesystems.crafttweaker.recipe.serializer;

import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.MirrorAxis;
import com.blamejared.crafttweaker.impl.helper.AccessibleElementsProvider;
import net.zharok01.coralinesystems.crafttweaker.recipe.CTShapedRecipeWithMeta;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CoralineShapedRecipeSerializer implements RecipeSerializer<CTShapedRecipeWithMeta> {

    @Override
    public CTShapedRecipeWithMeta fromJson(ResourceLocation id, JsonObject json) {
        // ZenScript-only recipe type; mirroring CraftTweaker's own convention here.
        return new CTShapedRecipeWithMeta(
                "invalid_recipe", "", CraftingBookCategory.MISC,
                IItemStack.of(new ItemStack(Items.BARRIER)),
                new IIngredient[][]{{IItemStack.of(new ItemStack(Items.BARRIER))}},
                MirrorAxis.NONE, null
        );
    }

    @Override
    public CTShapedRecipeWithMeta fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        int height = buffer.readVarInt();
        int width  = buffer.readVarInt();
        IIngredient[][] inputs = new IIngredient[height][width];
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                inputs[h][w] = IIngredient.fromIngredient(Ingredient.fromNetwork(buffer));
            }
        }
        MirrorAxis mirrorAxis = buffer.readEnum(MirrorAxis.class);
        ItemStack output      = buffer.readItem();
        String group          = buffer.readUtf();                              // ← new
        CraftingBookCategory cat = buffer.readEnum(CraftingBookCategory.class); // ← new
        return new CTShapedRecipeWithMeta(
                recipeId.getPath(), group, cat, IItemStack.of(output), inputs, mirrorAxis, null
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, CTShapedRecipeWithMeta recipe) {
        buffer.writeVarInt(recipe.getHeight());
        buffer.writeVarInt(recipe.getWidth());
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.toNetwork(buffer);
        }
        buffer.writeEnum(recipe.getMirrorAxis());
        buffer.writeItem(AccessibleElementsProvider.get().registryAccess(recipe::getResultItem));
        buffer.writeUtf(recipe.getGroup());               // ← new
        buffer.writeEnum(recipe.category());              // ← new
    }
}

