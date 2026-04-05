package net.zharok01.coralinesystems.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;

public class ContainerBlockEntity extends BlockEntity {

    // 41 slots: 36 inventory + 4 armor + 1 offhand
    private final ItemStackHandler inventory = new ItemStackHandler(41) {
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("GraveInventory", inventory.serializeNBT());
        tag.putString("OwnerName", ownerName);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("GraveInventory"));
        ownerName = tag.getString("OwnerName");
    }
}