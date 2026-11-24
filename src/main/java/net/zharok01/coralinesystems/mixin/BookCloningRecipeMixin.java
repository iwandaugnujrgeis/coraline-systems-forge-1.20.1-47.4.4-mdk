package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BookCloningRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookCloningRecipe.class)
public abstract class BookCloningRecipeMixin {

    @Inject(
            method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void coraline$copyDyeColorToClones(CraftingContainer container, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result.isEmpty()) {
            return;
        }

        Integer color = null;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack ingredient = container.getItem(slot);
            if (ingredient.getItem() == Items.WRITABLE_BOOK
                    && ingredient.getItem() instanceof DyeableLeatherItem dyeable
                    && dyeable.hasCustomColor(ingredient)) {
                int ingredientColor = dyeable.getColor(ingredient);
                if (color != null && color != ingredientColor) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
                color = ingredientColor;
            }
        }

        if (color != null && result.getItem() instanceof DyeableLeatherItem dyeableResult) {
            dyeableResult.setColor(result, color);
        }
    }
}
