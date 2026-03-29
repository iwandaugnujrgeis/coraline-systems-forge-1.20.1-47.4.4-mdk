package net.zharok01.coralinesystems.mixin;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;

@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin {

    @Shadow protected Inventory inventory;

    @Inject(method = "moveItemToGrid", at = @At("HEAD"), cancellable = true)
    private void fallbackToBackpackExtraction(Slot slotToFill, ItemStack ingredient, CallbackInfo ci) {
        // 1. Let vanilla check the normal 36 inventory slots first
        int vanillaSlot = this.inventory.findSlotMatchingUnusedItem(ingredient);
        if (vanillaSlot != -1) {
            return; // Vanilla found it, let the original method execute normally
        }

        // 2. Vanilla failed. Let's check if the player has a backpack on
        ItemStack backpack = this.inventory.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(backpack.getItem() instanceof BackpackItem)) return;

        LazyOptional<IItemHandler> optional = backpack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        if (optional.isPresent()) {
            IItemHandler handler = optional.orElse(null);
            if (handler != null) {
                // 3. Search the backpack slots for the ingredient
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stackInSlot = handler.getStackInSlot(i);

                    // Replicate vanilla's strict checking (no damage, no enchants, no custom names)
                    if (!stackInSlot.isEmpty() &&
                            ItemStack.isSameItemSameTags(ingredient, stackInSlot) &&
                            !stackInSlot.isDamaged() &&
                            !stackInSlot.isEnchanted() &&
                            !stackInSlot.hasCustomHoverName()) {

                        // 4. We found it! Extract exactly 1 item (simulate = false)
                        ItemStack extracted = handler.extractItem(i, 1, false);

                        if (!extracted.isEmpty()) {
                            // 5. Place the extracted item into the crafting grid
                            if (slotToFill.getItem().isEmpty()) {
                                slotToFill.set(extracted);
                            } else {
                                slotToFill.getItem().grow(1);
                            }

                            // 6. Cancel the rest of the vanilla method so it doesn't crash or duplicate
                            ci.cancel();
                            return;
                        }
                    }
                }
            }
        }
    }
}
