package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.zharok01.coralinesystems.block.BasketBlock;
import net.zharok01.coralinesystems.inventory.BasketMenu;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Block entity for {@link BasketBlock}.
 *
 * <p>Ported from Farmer's Delight with one key change:
 * the inventory is <strong>9 slots</strong> (one row) instead of 27 (three rows),
 * and the container menu is {@link BasketMenu} instead of {@code ChestMenu.threeRows}.</p>
 */
public class BasketBlockEntity extends RandomizableContainerBlockEntity implements Basket {

    /** Inventory size: a single row of 9 slots. */
    private static final int INVENTORY_SIZE = 9;

    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int transferCooldown = -1;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public BasketBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.BASKET.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Serialisation
    // -------------------------------------------------------------------------

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compound)) {
            ContainerHelper.loadAllItems(compound, this.items);
        }
        this.transferCooldown = compound.getInt("TransferCooldown");
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.items);
        }
        compound.putInt("TransferCooldown", this.transferCooldown);
    }

    // -------------------------------------------------------------------------
    // Container contract
    // -------------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        this.unpackLootTable(null);
        return ContainerHelper.removeItem(this.getItems(), index, count);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.unpackLootTable(null);
        this.getItems().set(index, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    // -------------------------------------------------------------------------
    // Display name
    // -------------------------------------------------------------------------

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.coraline_systems.basket");
    }

    // -------------------------------------------------------------------------
    // Menu creation — uses BasketMenu (9-slot, HopperMenu-style)
    // -------------------------------------------------------------------------

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new BasketMenu(id, playerInventory, this);
    }

    // -------------------------------------------------------------------------
    // Basket interface — world-position accessors
    // -------------------------------------------------------------------------

    @Override
    public double getLevelX() {
        return this.worldPosition.getX() + 0.5D;
    }

    @Override
    public double getLevelY() {
        return this.worldPosition.getY() + 0.5D;
    }

    @Override
    public double getLevelZ() {
        return this.worldPosition.getZ() + 0.5D;
    }

    // -------------------------------------------------------------------------
    // Item capture helpers (unchanged from FD)
    // -------------------------------------------------------------------------

    public static boolean pullItems(Level level, Basket basket, int facingIndex) {
        for (ItemEntity itemEntity : getCaptureItems(level, basket, facingIndex)) {
            if (captureItem(basket, itemEntity)) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemEntity> getCaptureItems(Level level, Basket basket, int facingIndex) {
        return basket.getFacingCollectionArea(facingIndex).toAabbs().stream()
                .flatMap(aabb -> level.getEntitiesOfClass(
                        ItemEntity.class,
                        aabb.move(basket.getLevelX() - 0.5D,
                                basket.getLevelY() - 0.5D,
                                basket.getLevelZ() - 0.5D),
                        EntitySelector.ENTITY_STILL_ALIVE).stream())
                .collect(Collectors.toList());
    }

    public static boolean captureItem(Container inventory, ItemEntity itemEntity) {
        ItemStack entityStack = itemEntity.getItem().copy();
        ItemStack remainder = putStackInInventoryAllSlots(inventory, entityStack);
        if (remainder.isEmpty()) {
            itemEntity.discard();
            return true;
        } else {
            itemEntity.setItem(remainder);
            return false;
        }
    }

    public static ItemStack putStackInInventoryAllSlots(Container destination, ItemStack stack) {
        int size = destination.getContainerSize();
        for (int i = 0; i < size && !stack.isEmpty(); i++) {
            stack = insertStack(destination, stack, i);
        }
        return stack;
    }

    private static ItemStack insertStack(Container destination, ItemStack stack, int index) {
        ItemStack existing = destination.getItem(index);

        if (!canInsertItemInSlot(destination, stack, index, null)) {
            return stack;
        }

        boolean changed = false;
        boolean wasEmpty = destination.isEmpty();

        if (existing.isEmpty()) {
            destination.setItem(index, stack);
            stack = ItemStack.EMPTY;
            changed = true;
        } else if (canCombine(existing, stack)) {
            int space = stack.getMaxStackSize() - existing.getCount();
            int transfer = Math.min(stack.getCount(), space);
            stack.shrink(transfer);
            existing.grow(transfer);
            changed = transfer > 0;
        }

        if (changed) {
            if (wasEmpty && destination instanceof BasketBlockEntity basketEntity) {
                if (!basketEntity.mayTransfer()) {
                    basketEntity.setTransferCooldown(8);
                }
            }
            destination.setChanged();
        }

        return stack;
    }

    private static boolean canInsertItemInSlot(Container inventory, ItemStack stack,
                                               int index, @Nullable Direction side) {
        if (!inventory.canPlaceItem(index, stack)) {
            return false;
        }
        return !(inventory instanceof WorldlyContainer worldly)
                || worldly.canPlaceItemThroughFace(index, stack, side);
    }

    private static boolean canCombine(ItemStack existing, ItemStack incoming) {
        return existing.getCount() < existing.getMaxStackSize()
                && ItemStack.isSameItemSameTags(existing, incoming);
    }

    // -------------------------------------------------------------------------
    // Transfer cooldown
    // -------------------------------------------------------------------------

    public void setTransferCooldown(int ticks) {
        this.transferCooldown = ticks;
    }

    private boolean isOnTransferCooldown() {
        return this.transferCooldown > 0;
    }

    public boolean mayTransfer() {
        return this.transferCooldown > 8;
    }

    // -------------------------------------------------------------------------
    // Hopper-style tick logic
    // -------------------------------------------------------------------------

    private boolean isFull() {
        for (ItemStack stack : this.items) {
            if (stack.isEmpty() || stack.getCount() != stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    private void updateHopper(Supplier<Boolean> supplier) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (!this.isOnTransferCooldown() && this.getBlockState().getValue(BlockStateProperties.ENABLED)) {
            boolean pulled = false;
            if (!this.isFull()) {
                pulled = supplier.get();
            }
            if (pulled) {
                this.setTransferCooldown(8);
                this.setChanged();
            }
        }
    }

    public void onEntityCollision(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            BlockPos pos = this.getBlockPos();
            int facing = this.getBlockState().getValue(BasketBlock.FACING).get3DDataValue();
            if (Shapes.joinIsNotEmpty(
                    Shapes.create(entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ())),
                    this.getFacingCollectionArea(facing),
                    BooleanOp.AND)) {
                this.updateHopper(() -> captureItem(this, itemEntity));
            }
        }
    }

    /**
     * Static ticker method — registered via
     * {@link BasketBlock#getTicker}.
     */
    public static void pushItemsTick(Level level, BlockPos pos, BlockState state,
                                     BasketBlockEntity blockEntity) {
        blockEntity.transferCooldown--;
        if (!blockEntity.isOnTransferCooldown()) {
            blockEntity.setTransferCooldown(0);
            int facing = state.getValue(BasketBlock.FACING).get3DDataValue();
            blockEntity.updateHopper(() -> pullItems(level, blockEntity, facing));
        }
    }
}