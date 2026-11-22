package net.zharok01.coralinesystems.quiver;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class QuiverContainer extends SimpleContainer {
    private final Player owner;
    private ItemStack boundQuiver;
    private boolean coraline$reloading;

    public QuiverContainer(Player owner) {
        super(QuiverContents.read(owner.getItemBySlot(EquipmentSlot.CHEST)).toArray(new ItemStack[0]));
        this.owner = owner;
        this.boundQuiver = owner.getItemBySlot(EquipmentSlot.CHEST);
    }

    public boolean isBoundToWornQuiver() {
        ItemStack worn = this.owner.getItemBySlot(EquipmentSlot.CHEST);
        return QuiverContents.isQuiver(worn) && (this.owner.level().isClientSide || worn == this.boundQuiver);
    }

    public boolean coraline$isOutOfSync() {
        NonNullList<ItemStack> contents = QuiverContents.read(this.boundQuiver);
        for (int slot = 0; slot < QuiverContents.SLOT_COUNT; slot++) {
            if (!ItemStack.matches(this.getItem(slot), contents.get(slot))) {
                return true;
            }
        }
        return false;
    }

    public void rebindToWornQuiver() {
        this.boundQuiver = this.owner.getItemBySlot(EquipmentSlot.CHEST);
        NonNullList<ItemStack> contents = QuiverContents.read(this.boundQuiver);
        this.coraline$reloading = true;
        for (int slot = 0; slot < QuiverContents.SLOT_COUNT; slot++) {
            this.setItem(slot, contents.get(slot));
        }
        this.coraline$reloading = false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!this.coraline$reloading && !this.owner.level().isClientSide && this.isBoundToWornQuiver()) {
            NonNullList<ItemStack> contents = NonNullList.withSize(QuiverContents.SLOT_COUNT, ItemStack.EMPTY);
            for (int slot = 0; slot < QuiverContents.SLOT_COUNT; slot++) {
                contents.set(slot, this.getItem(slot));
            }
            QuiverContents.write(this.boundQuiver, contents);
        }
    }
}