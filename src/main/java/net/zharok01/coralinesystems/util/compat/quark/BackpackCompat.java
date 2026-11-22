package net.zharok01.coralinesystems.util.compat.quark;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;
import org.violetmoon.quark.base.util.InventoryIIH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BackpackCompat {

    public static final String QUARK_ODDITIES_MOD_ID = "quarkoddities";

    private BackpackCompat() {
    }

    public static List<ItemStack> coraline$emptyBackpacks(Collection<ItemEntity> drops) {
        List<ItemStack> contents = new ArrayList<>();

        for (ItemEntity drop : drops) {
            ItemStack backpack = drop.getItem();
            if (!backpack.is(BackpackModule.backpack)) continue;

            InventoryIIH backpackInventory = new InventoryIIH(backpack);
            for (int slot = 0; slot < backpackInventory.getSlots(); slot++) {
                contents.add(backpackInventory.getStackInSlot(slot).copy());
                backpackInventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }

        return contents;
    }
}