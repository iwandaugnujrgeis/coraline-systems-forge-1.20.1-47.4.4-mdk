package net.zharok01.coralinesystems.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.zharok01.coralinesystems.CoralineSystems;

public class CoralineTags {

    public static final TagKey<Block> HELPER_MINEABLE = TagKey.create(Registries.BLOCK,
            new ResourceLocation(CoralineSystems.MOD_ID, "helper_mineable"));

    public static final TagKey<Block> FRAGILE = TagKey.create(Registries.BLOCK,
            new ResourceLocation(CoralineSystems.MOD_ID, "fragile"));

    public static final TagKey<Block> RETURNABLE = TagKey.create(Registries.BLOCK,
            new ResourceLocation(CoralineSystems.MOD_ID, "returnable"));

    public static final TagKey<Block> CAMEL_SPAWNABLE_ON = TagKey.create(Registries.BLOCK,
            new ResourceLocation(CoralineSystems.MOD_ID, "camel_spawnable_on"));

    public static final TagKey<Item> ZIPLINEABLE = TagKey.create(Registries.ITEM,
            new ResourceLocation(CoralineSystems.MOD_ID, "ziplineable"));

    public static final TagKey<Item> PIG_FOOD_DROPPED = TagKey.create(Registries.ITEM,
            new ResourceLocation(CoralineSystems.MOD_ID, "pig_food_dropped"));
}