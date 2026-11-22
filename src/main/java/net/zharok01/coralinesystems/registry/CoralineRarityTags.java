package net.zharok01.coralinesystems.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class CoralineRarityTags {

    public static final TagKey<Item> RARITY_COMMON = tag("rarity_common");
    public static final TagKey<Item> RARITY_UNCOMMON = tag("rarity_uncommon");
    public static final TagKey<Item> RARITY_SPECIAL = tag("rarity_special");
    public static final TagKey<Item> RARITY_RARE = tag("rarity_rare");
    public static final TagKey<Item> RARITY_LEGENDARY = tag("rarity_legendary");
    public static final TagKey<Item> RARITY_EPIC = tag("rarity_epic");

    private static TagKey<Item> tag(String name) {
        return ItemTags.create(new ResourceLocation("coraline_systems", name));
    }

    private CoralineRarityTags() {}
}