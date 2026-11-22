package net.zharok01.coralinesystems.util.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.registry.CoralineRecipeSerializers;
import net.zharok01.coralinesystems.util.DyeableItemColorHelper;
import net.zharok01.coralinesystems.util.compat.quark.BackpackDurability;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;

public class BackpackRepairRecipe extends CustomRecipe {
    public BackpackRepairRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        List<ItemStack> stacks = coraline$collectIngredients(container);
        return stacks.size() == 2 && stacks.stream().allMatch(stack -> BackpackDurability.isBackpack(stack) && !BackpackItem.doesBackpackHaveItems(stack));
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        List<ItemStack> stacks = coraline$collectIngredients(container);
        ItemStack first = stacks.get(0);
        ItemStack second = stacks.get(1);
        int maxDurability = BackpackDurability.MAX_DURABILITY;

        int combinedRemaining = (maxDurability - first.getDamageValue()) + (maxDurability - second.getDamageValue()) + maxDurability * BackpackDurability.REPAIR_BONUS_PERCENT / 100;
        ItemStack result = first.copy();
        result.setDamageValue(Math.max(0, maxDurability - combinedRemaining));
        DyeableItemColorHelper.coraline$blendColors(result, first, second);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CoralineRecipeSerializers.BACKPACK_REPAIR.get();
    }

    private static List<ItemStack> coraline$collectIngredients(CraftingContainer container) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }
}