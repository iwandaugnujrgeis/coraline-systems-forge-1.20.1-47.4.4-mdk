package net.zharok01.coralinesystems.util.compat.quark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.util.helper.BagBreakHelper;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;
import org.violetmoon.quark.base.util.InventoryIIH;

public final class BackpackDurability {
    public static final int MAX_DURABILITY = 600;
    public static final int REPAIR_BONUS_PERCENT = 5;

    private static final Map<ServerPlayer, List<ItemEntity>> coraline$brokenBackpackDrops = new WeakHashMap<>();

    private BackpackDurability() {
    }

    public static boolean isBackpack(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem;
    }

    public static void damage(ServerPlayer player, ItemStack backpack, int amount) {
        if (player.getAbilities().instabuild) {
            return;
        }
        int newDamage = backpack.getDamageValue() + amount;
        if (newDamage < MAX_DURABILITY) {
            backpack.setDamageValue(newDamage);
            return;
        }

        InventoryIIH handler = new InventoryIIH(backpack);
        List<ItemStack> contents = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                contents.add(stack);
            }
        }
        List<ItemEntity> drops = BagBreakHelper.destroy(player, backpack, contents);
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(CoralineItems.COBALT_PANTS.get())) {
            coraline$brokenBackpackDrops.computeIfAbsent(player, ignored -> new ArrayList<>()).addAll(drops);
        }
    }

    public static List<ItemStack> coraline$collectBrokenBackpackDrops(ServerPlayer player) {
        List<ItemStack> contents = new ArrayList<>();
        List<ItemEntity> drops = coraline$brokenBackpackDrops.remove(player);
        if (drops == null) {
            return contents;
        }

        for (ItemEntity drop : drops) {
            if (!drop.isRemoved() && !drop.getItem().isEmpty()) {
                contents.add(drop.getItem().copy());
                drop.discard();
            }
        }
        return contents;
    }

    public static boolean coraline$hasBrokenBackpackDrops(ServerPlayer player) {
        List<ItemEntity> drops = coraline$brokenBackpackDrops.get(player);
        return drops != null && drops.stream().anyMatch(drop -> !drop.isRemoved() && !drop.getItem().isEmpty());
    }

    public static void coraline$forgetBrokenBackpackDrops(ServerPlayer player) {
        coraline$brokenBackpackDrops.remove(player);
    }

    public static boolean[] coraline$snapshotFilledSlots(ItemStack backpack) {
        InventoryIIH handler = new InventoryIIH(backpack);
        boolean[] filledSlots = new boolean[handler.getSlots()];
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            filledSlots[slot] = !handler.getStackInSlot(slot).isEmpty();
        }
        return filledSlots;
    }

    public static void coraline$chargeSlotChanges(ServerPlayer player, ItemStack backpack, boolean[] filledSlotsBefore) {
        if (!isBackpack(backpack)) {
            return;
        }
        InventoryIIH handler = new InventoryIIH(backpack);
        int changedSlots = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (filledSlotsBefore[slot] != !handler.getStackInSlot(slot).isEmpty()) {
                changedSlots++;
            }
        }
        if (changedSlots > 0) {
            damage(player, backpack, changedSlots);
        }
    }
}