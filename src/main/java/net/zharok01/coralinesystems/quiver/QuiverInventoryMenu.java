package net.zharok01.coralinesystems.quiver;

import com.legacy.rediscovered.data.RediscoveredTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.mixin.accessor.AbstractContainerMenuAccessor;
import net.zharok01.coralinesystems.mixin.accessor.SlotAccessor;
import net.zharok01.coralinesystems.registry.CoralineMenuTypes;

public class QuiverInventoryMenu extends InventoryMenu {
    public static final int IMAGE_HEIGHT = 188;
    public static final int FIRST_QUIVER_SLOT = SHIELD_SLOT + 1;
    public static final int END_QUIVER_SLOT = FIRST_QUIVER_SLOT + QuiverContents.SLOT_COUNT;
    private static final int PLAYER_SLOT_SHIFT = 22;
    private static final int QUIVER_ROW_X = 8;
    private static final int QUIVER_ROW_Y = 84;
    private static final int SLOT_SIZE = 18;

    private final Player coraline$owner;
    private final QuiverContainer coraline$quiverContainer;

    public QuiverInventoryMenu(int containerId, Player owner) {
        super(owner.getInventory(), !owner.level().isClientSide, owner);
        ((AbstractContainerMenuAccessor) this).coraline$setContainerId(containerId);
        this.coraline$owner = owner;

        Inventory inventory = owner.getInventory();
        for (Slot slot : this.slots) {
            if (slot.container == inventory && slot.getContainerSlot() < Inventory.INVENTORY_SIZE) {
                ((SlotAccessor) slot).coraline$setY(slot.y + PLAYER_SLOT_SHIFT);
            }
        }

        this.coraline$quiverContainer = new QuiverContainer(owner);
        for (int index = 0; index < QuiverContents.SLOT_COUNT; index++) {
            this.addSlot(new QuiverAmmoSlot(this.coraline$quiverContainer, index, QUIVER_ROW_X + index * SLOT_SIZE, QUIVER_ROW_Y, owner));
        }
    }

    @Override
    public MenuType<?> getType() {
        return CoralineMenuTypes.QUIVER_INVENTORY.get();
    }

    @Override
    public void broadcastChanges() {
        if (QuiverContents.isWornBy(this.coraline$owner) && (!this.coraline$quiverContainer.isBoundToWornQuiver() || this.coraline$quiverContainer.coraline$isOutOfSync())) {
            this.coraline$quiverContainer.rebindToWornQuiver();
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        boolean fromQuiver = index >= FIRST_QUIVER_SLOT;
        boolean arrowToQuiver = !fromQuiver && index >= INV_SLOT_START && index < USE_ROW_SLOT_END && slot.getItem().is(RediscoveredTags.Items.QUIVER_AMMO);
        if (!fromQuiver && !arrowToQuiver) {
            return super.quickMoveStack(player, index);
        }
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = fromQuiver
                ? this.moveItemStackTo(stack, INV_SLOT_START, USE_ROW_SLOT_END, true)
                : this.moveItemStackTo(stack, FIRST_QUIVER_SLOT, END_QUIVER_SLOT, false);
        if (!moved) {
            return fromQuiver ? ItemStack.EMPTY : super.quickMoveStack(player, index);
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }
}