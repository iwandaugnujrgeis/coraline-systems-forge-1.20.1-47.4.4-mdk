package net.zharok01.coralinesystems.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class DespawnCleanupUtil {
    public static final String PICKED_TAG = "coraline_picked";

    public static void onMobPickupItem(Mob mob, EquipmentSlot slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean(PICKED_TAG, true);
        }
    }

    public static void dropPickedItems(Mob mob) {
        if (mob.level().isClientSide) return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = mob.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            CompoundTag tag = stack.getTag();
            if (tag != null && tag.getBoolean(PICKED_TAG)) {
                tag.remove(PICKED_TAG);
                if (tag.isEmpty()) {
                    stack.setTag(null);
                }

                ItemEntity drop = mob.spawnAtLocation(stack);
                if (drop != null) {
                    drop.setDefaultPickUpDelay();
                }

                mob.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }
}