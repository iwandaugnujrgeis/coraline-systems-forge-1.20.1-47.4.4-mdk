package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.gui.ContainerBlockMenu;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import org.jetbrains.annotations.NotNull;

public class ContainerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_COUNT = 96;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private String ownerName = "Unknown";

    public ContainerBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.CONTAINER_BLOCK_ENTITY.get(), pos, state);
    }

    public void addItem(ItemStack stack) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            stack = inventory.insertItem(i, stack, false);
            if (stack.isEmpty()) break;
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void setOwnerName(String name) {
        this.ownerName = name;
        setChanged();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container." + CoralineSystems.MOD_ID + ".container_block");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new ContainerBlockMenu(containerId, playerInventory, inventory, ContainerLevelAccess.create(level, getBlockPos()));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("WidePockets", inventory.serializeNBT());
        tag.putString("OwnerName", ownerName);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        CompoundTag savedInventory = tag.getCompound("WidePockets");
        savedInventory.putInt("Size", SLOT_COUNT);
        inventory.deserializeNBT(savedInventory);
        ownerName = tag.getString("OwnerName");
    }
}