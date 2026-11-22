package net.zharok01.coralinesystems.event;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineRarity;
import net.zharok01.coralinesystems.registry.CoralineRarityTags;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public final class RarityTagHandler {

    private static final Rarity[] TIER_ORDER = {
            Rarity.COMMON,
            Rarity.UNCOMMON,
            CoralineRarity.SPECIAL,
            Rarity.RARE,
            CoralineRarity.LEGENDARY,
            Rarity.EPIC
    };

    private static volatile Map<Item, Rarity> coraline$rarityOverrides = Map.of();

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        Map<Item, Rarity> overrides = new HashMap<>();

        putTagEntries(overrides, CoralineRarityTags.RARITY_COMMON, Rarity.COMMON);
        putTagEntries(overrides, CoralineRarityTags.RARITY_UNCOMMON, Rarity.UNCOMMON);
        putTagEntries(overrides, CoralineRarityTags.RARITY_SPECIAL, CoralineRarity.SPECIAL);
        putTagEntries(overrides, CoralineRarityTags.RARITY_RARE, Rarity.RARE);
        putTagEntries(overrides, CoralineRarityTags.RARITY_LEGENDARY, CoralineRarity.LEGENDARY);
        putTagEntries(overrides, CoralineRarityTags.RARITY_EPIC, Rarity.EPIC);

        coraline$rarityOverrides = Map.copyOf(overrides);
    }

    private static void putTagEntries(Map<Item, Rarity> map, net.minecraft.tags.TagKey<Item> tag, Rarity rarity) {
        net.minecraftforge.registries.ForgeRegistries.ITEMS
                .tags()
                .getTag(tag)
                .forEach(item -> map.put(item, rarity));
    }

    public static Map<Item, Rarity> getOverrides() {
        return coraline$rarityOverrides;
    }
}