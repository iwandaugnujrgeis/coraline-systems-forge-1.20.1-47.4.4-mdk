package net.zharok01.coralinesystems.client.gui.creative_tabs;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.client.gui.TabIconCycle;

public final class CreativeTabIconCycler {
    private static final Map<CreativeModeTab, TabIconCycle> CYCLES = new IdentityHashMap<>();

    public static ItemStack getIcon(CreativeModeTab tab) {
        if (tab.getType() != CreativeModeTab.Type.CATEGORY) {
            return tab.getIconItem();
        }

        TabIconCycle cycle = CYCLES.computeIfAbsent(tab, key -> new TabIconCycle());
        if (cycle.isDue()) {
            cycle.showOneOf(tab.getDisplayItems());
        }
        return cycle.shown().isEmpty() ? tab.getIconItem() : cycle.shown();
    }

    private CreativeTabIconCycler() {
    }
}