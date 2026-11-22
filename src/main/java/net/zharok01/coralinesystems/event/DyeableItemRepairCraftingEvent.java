package net.zharok01.coralinesystems.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.DyeableItemColorHelper;
import net.zharok01.coralinesystems.util.compat.quark.BackpackDurability;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DyeableItemRepairCraftingEvent {
    @SubscribeEvent
    public static void coraline$blendRepairedItemColors(PlayerEvent.ItemCraftedEvent event) {
        ItemStack first = ItemStack.EMPTY;
        ItemStack second = ItemStack.EMPTY;

        for (int slot = 0; slot < event.getInventory().getContainerSize(); slot++) {
            ItemStack stack = event.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (first.isEmpty()) {
                first = stack;
            } else if (second.isEmpty()) {
                second = stack;
            } else {
                return;
            }
        }

        ItemStack result = event.getCrafting();
        if (!first.isEmpty() && !second.isEmpty()
                && !BackpackDurability.isBackpack(result)
                && first.getItem() == second.getItem()
                && first.getItem() == result.getItem()
                && first.isDamageableItem()
                && (first.getDamageValue() > 0 || second.getDamageValue() > 0)) {
            DyeableItemColorHelper.coraline$blendColors(result, first, second);
        }
    }
}
