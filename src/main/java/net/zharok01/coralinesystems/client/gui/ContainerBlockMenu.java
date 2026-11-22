package net.zharok01.coralinesystems.client.gui;

import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.zharok01.coralinesystems.block.ContainerBlockEntity;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import net.zharok01.coralinesystems.registry.CoralineMenuTypes;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;

public class ContainerBlockMenu extends AbstractContainerMenu {

    public static final int VISIBLE_ROWS = 6;
    public static final int SLOT_SIZE = 18;
    public static final int CONTAINER_SLOTS_X = 8;
    public static final int CONTAINER_SLOTS_Y = 18;
    public static final int CONTAINER_COLUMNS = 8;

    private static final int PLAYER_COLUMNS = 9;
    private static final int PLAYER_INVENTORY_Y = 139;
    private static final int HOTBAR_Y = 197;
    private static final int CONTAINER_ROWS = ContainerBlockEntity.SLOT_COUNT / CONTAINER_COLUMNS;
    private static final int SCROLLABLE_ROWS = CONTAINER_ROWS - VISIBLE_ROWS;

    private final ContainerLevelAccess access;

    public ContainerBlockMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(ContainerBlockEntity.SLOT_COUNT), ContainerLevelAccess.NULL);
    }

    public ContainerBlockMenu(int containerId, Inventory playerInventory, IItemHandler containerHandler, ContainerLevelAccess access) {
        super(CoralineMenuTypes.CONTAINER_BLOCK.get(), containerId);
        this.access = access;

        for (int row = 0; row < CONTAINER_ROWS; row++) {
            for (int column = 0; column < CONTAINER_COLUMNS; column++) {
                addSlot(new ScrollableSlot(containerHandler, row * CONTAINER_COLUMNS + column, CONTAINER_SLOTS_X + column * SLOT_SIZE, CONTAINER_SLOTS_Y + row * SLOT_SIZE));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < PLAYER_COLUMNS; column++) {
                addSlot(new Slot(playerInventory, column + row * PLAYER_COLUMNS + PLAYER_COLUMNS, CONTAINER_SLOTS_X + column * SLOT_SIZE, PLAYER_INVENTORY_Y + row * SLOT_SIZE));
            }
        }

        for (int column = 0; column < PLAYER_COLUMNS; column++) {
            addSlot(new Slot(playerInventory, column, CONTAINER_SLOTS_X + column * SLOT_SIZE, HOTBAR_Y));
        }
    }

    public void coraline$scrollTo(float scrollOffset) {
        int scrollRow = (int) (scrollOffset * SCROLLABLE_ROWS + 0.5F);
        for (Slot slot : slots) {
            if (slot instanceof ScrollableSlot scrollableSlot) {
                scrollableSlot.coraline$scrollToRow(scrollRow);
            }
        }
    }

    public float coraline$scrollBy(float scrollOffset, double wheelDelta) {
        return Mth.clamp(scrollOffset - (float) (wheelDelta / SCROLLABLE_ROWS), 0.0F, 1.0F);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            movedStack = slotStack.copy();

            if (index < ContainerBlockEntity.SLOT_COUNT) {
                if (!moveItemStackTo(slotStack, ContainerBlockEntity.SLOT_COUNT, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(slotStack, 0, ContainerBlockEntity.SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return movedStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(access, player, CoralineBlocks.CONTAINER_BLOCK.get());
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        access.execute((level, pos) -> {
            level.blockEntityChanged(pos);
            level.playSound(null, pos, CoralineSounds.CONTAINER_ACCESS.get(), SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.9F + 0.1F);
        });
    }

    private static class ScrollableSlot extends SlotItemHandler {
        private final int coraline$initialY;

        private ScrollableSlot(IItemHandler itemHandler, int index, int x, int y) {
            super(itemHandler, index, x, y);
            this.coraline$initialY = y;
        }

        private void coraline$scrollToRow(int scrollRow) {
            y = coraline$initialY - scrollRow * SLOT_SIZE;
        }

        @Override
        public boolean isActive() {
            return y >= CONTAINER_SLOTS_Y && y < CONTAINER_SLOTS_Y + VISIBLE_ROWS * SLOT_SIZE;
        }
    }
}