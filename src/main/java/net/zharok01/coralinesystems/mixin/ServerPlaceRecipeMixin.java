package net.zharok01.coralinesystems.mixin;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;
import javax.annotation.Nonnull;

@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin {

    @Shadow protected Inventory inventory;
    @Shadow protected RecipeBookMenu<?> menu;
    @Unique private boolean coraline$clearGridIntoBackpack;
    @Inject(method = "moveItemToGrid", at = @At("HEAD"), cancellable = true)
    private void fallbackToBackpackExtraction(Slot slotToFill, @Nonnull ItemStack ingredient, CallbackInfo ci) {
        int vanillaSlot = this.inventory.findSlotMatchingUnusedItem(ingredient);
        if (vanillaSlot != -1) {
            return;
        }
        ItemStack backpack = this.inventory.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(backpack.getItem() instanceof BackpackItem)) return;
        LazyOptional<IItemHandler> optional = backpack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        if (!optional.isPresent()) return;
        IItemHandler handler = optional.orElseThrow(IllegalStateException::new);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stackInSlot = handler.getStackInSlot(i);
            if (!stackInSlot.isEmpty() &&
                    ItemStack.isSameItemSameTags(ingredient, stackInSlot) &&
                    !stackInSlot.isDamaged() &&
                    !stackInSlot.isEnchanted() &&
                    !stackInSlot.hasCustomHoverName()) {
                ItemStack extracted = handler.extractItem(i, 1, false);
                if (!extracted.isEmpty()) {
                    if (slotToFill.getItem().isEmpty()) {
                        slotToFill.set(extracted);
                    } else {
                        slotToFill.getItem().grow(1);
                    }
                    ci.cancel();
                    return;
                }
            }
        }
    }

    @Inject(method = "testClearGrid", at = @At("HEAD"))
    private void coraline$resetBackpackGridClearCheck(CallbackInfoReturnable<Boolean> cir) {
        this.coraline$clearGridIntoBackpack = false;
    }

    @Inject(method = "testClearGrid", at = @At("RETURN"), cancellable = true)
    private void coraline$allowClearingGridIntoBackpack(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || !this.coraline$canStoreGridInBackpack()) {
            return;
        }
        this.coraline$clearGridIntoBackpack = true;
        cir.setReturnValue(true);
    }
    @Redirect(
            method = "clearGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;Z)V"

            )

    )

    private void coraline$returnGridItemToBackpack(Inventory inventory, @Nonnull ItemStack stack, boolean sendPacket) {
        if (!this.coraline$clearGridIntoBackpack || stack.isEmpty()) {
            inventory.placeItemBackInInventory(stack, sendPacket);
            return;
        }
        ItemStack backpack = this.inventory.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(backpack.getItem() instanceof BackpackItem)) {
            inventory.placeItemBackInInventory(stack, sendPacket);
            return;
        }
        LazyOptional<IItemHandler> optional = backpack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        if (!optional.isPresent()) {
            inventory.placeItemBackInInventory(stack, sendPacket);
            return;
        }
        IItemHandler handler = optional.orElseThrow(IllegalStateException::new);
        ItemStack remainder = stack;
        for (int i = 0; i < handler.getSlots() && !remainder.isEmpty(); i++) {
            remainder = handler.insertItem(i, remainder, false);
        }
        stack.setCount(remainder.getCount());
        if (!stack.isEmpty()) {
            inventory.placeItemBackInInventory(stack, sendPacket);
        }
    }

    @Inject(method = "clearGrid", at = @At("TAIL"))
    private void coraline$resetBackpackGridClear(CallbackInfo ci) {
        this.coraline$clearGridIntoBackpack = false;
    }

    @Unique
    private boolean coraline$canStoreGridInBackpack() {
        ItemStack backpack = this.inventory.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return false;
        }
        ItemStack simulatedBackpack = backpack.copy();
        LazyOptional<IItemHandler> optional = simulatedBackpack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        if (!optional.isPresent()) {
            return false;
        }
        IItemHandler handler = optional.orElseThrow(IllegalStateException::new);
        for (int slot = 0; slot < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; slot++) {
            if (slot == this.menu.getResultSlotIndex()) {
                continue;
            }
            ItemStack remainder = this.menu.getSlot(slot).getItem().copy();
            for (int backpackSlot = 0; backpackSlot < handler.getSlots() && !remainder.isEmpty(); backpackSlot++) {
                remainder = handler.insertItem(backpackSlot, remainder, false);
            }
            if (!remainder.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
