package net.zharok01.coralinesystems.mixin.compat.quark;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.util.compat.quark.BackpackDurability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;

@Pseudo
@Mixin(value = BackpackMenu.class, remap = false)
public abstract class BackpackMenuMixin extends InventoryMenu {
    @Unique
    private static final int BACKPACK_FIRST_SLOT = SHIELD_SLOT + 1;
    @Unique
    private static final int BACKPACK_END_SLOT = BACKPACK_FIRST_SLOT + 27;

    @Unique
    private ItemStack coraline$backpackBeforeClick = ItemStack.EMPTY;
    @Unique
    private boolean[] coraline$filledSlotsBeforeClick;

    protected BackpackMenuMixin(Inventory inventory, boolean active, Player owner) {
        super(inventory, active, owner);
    }

    @Inject(method = "m_150399_", at = @At("HEAD"), remap = false, require = 0)
    private void coraline$snapshotBackpackSlots(int slotId, int button, ClickType clickType, Player player, CallbackInfo callback) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack backpack = serverPlayer.getItemBySlot(EquipmentSlot.CHEST);
            if (BackpackDurability.isBackpack(backpack)) {
                this.coraline$backpackBeforeClick = backpack;
                this.coraline$filledSlotsBeforeClick = BackpackDurability.coraline$snapshotFilledSlots(backpack);
                return;
            }
        }
        this.coraline$backpackBeforeClick = ItemStack.EMPTY;
        this.coraline$filledSlotsBeforeClick = null;
    }

    @Inject(method = "m_150399_", at = @At("TAIL"), remap = false, require = 0)
    private void coraline$chargeChangedBackpackSlots(int slotId, int button, ClickType clickType, Player player, CallbackInfo callback) {
        if (player instanceof ServerPlayer serverPlayer && !this.coraline$backpackBeforeClick.isEmpty() && this.coraline$filledSlotsBeforeClick != null) {
            BackpackDurability.coraline$chargeSlotChanges(serverPlayer, this.coraline$backpackBeforeClick, this.coraline$filledSlotsBeforeClick);
        }
        this.coraline$backpackBeforeClick = ItemStack.EMPTY;
        this.coraline$filledSlotsBeforeClick = null;
    }

    @Inject(method = "m_7648_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void coraline$hotbarToBackpackFirst(Player player, int index, CallbackInfoReturnable<ItemStack> callback) {
        if (index < USE_ROW_SLOT_START || index >= USE_ROW_SLOT_END) {
            return;
        }
        if (this.slots.size() < BACKPACK_END_SLOT) {
            return;
        }
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return;
        }

        ItemStack stack = slot.getItem();
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(stack);
        boolean equipsToArmor = equipmentSlot.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(8 - equipmentSlot.getIndex()).hasItem();
        boolean equipsToOffhand = equipmentSlot == EquipmentSlot.OFFHAND && !this.slots.get(SHIELD_SLOT).hasItem();
        if (equipsToArmor || equipsToOffhand) {
            return;
        }

        ItemStack original = stack.copy();
        boolean movedToBackpack = this.moveItemStackTo(stack, BACKPACK_FIRST_SLOT, BACKPACK_END_SLOT, false);
        boolean movedToInventory = !stack.isEmpty() && this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END, false);
        if (!movedToBackpack && !movedToInventory) {
            callback.setReturnValue(ItemStack.EMPTY);
            return;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        callback.setReturnValue(original);
    }
}