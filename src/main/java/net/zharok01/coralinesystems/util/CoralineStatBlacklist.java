package net.zharok01.coralinesystems.util;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

public final class CoralineStatBlacklist {

    public static final Set<String> IDS = Set.of(
            "clean_armor",
            "clean_banner",
            "clean_shulker_box",
            "damage_blocked_by_shield",
            "enchant_item",
            "interact_with_blast_furnace",
            "interact_with_campfire",
            "interact_with_grindstone",
            "interact_with_smithing_table",
            "interact_with_smoker",
            "open_barrel",
            // "fly_one_cm",
            "strider_one_cm",
            "time_since_rest",
            "sleep_in_bed",
            "interact_with_beacon",
            "interact_with_cartography_table",
            "interact_with_loom",
            "open_shulker_box",
            "raid_trigger",
            "talked_to_villager",
            "raid_win",
            "interact_with_anvil",
            "traded_with_villager"
    );

    public static boolean isBlacklisted(Stat<?> stat) {
        if (stat.getType() != Stats.CUSTOM) {
            return false;
        }
        ResourceLocation id = (ResourceLocation) stat.getValue();
        return id.getNamespace().equals("minecraft") && IDS.contains(id.getPath());
    }

    private CoralineStatBlacklist() {
    }
}