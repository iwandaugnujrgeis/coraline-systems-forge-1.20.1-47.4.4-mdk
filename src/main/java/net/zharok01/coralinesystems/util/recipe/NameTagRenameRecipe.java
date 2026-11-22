package net.zharok01.coralinesystems.util.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.item.CoralineNameTagItem;
import net.zharok01.coralinesystems.registry.CoralineSerializers;
import org.jetbrains.annotations.NotNull;

public class NameTagRenameRecipe extends CustomRecipe {

    public NameTagRenameRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, @NotNull Level level) {
        ItemStack nameTag = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof CoralineNameTagItem && nameTag.isEmpty()) {
                nameTag = stack;
            } else if (target.isEmpty()) {
                target = stack;
            } else {
                return false;
            }
        }

        return !nameTag.isEmpty() && !target.isEmpty() && nameTag.hasCustomHoverName();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer container, @NotNull RegistryAccess registryAccess) {
        ItemStack nameTag = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof CoralineNameTagItem && nameTag.isEmpty()) {
                nameTag = stack;
            } else {
                target = stack;
            }
        }

        ItemStack result = target.copy();
        result.setCount(1);
        result.setHoverName(nameTag.getHoverName());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return CoralineSerializers.NAME_TAG_RENAME.get();
    }
}