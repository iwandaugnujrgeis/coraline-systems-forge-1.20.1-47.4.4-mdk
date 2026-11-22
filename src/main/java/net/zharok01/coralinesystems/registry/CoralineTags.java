package net.zharok01.coralinesystems.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.zharok01.coralinesystems.CoralineSystems;

@SuppressWarnings("removal")
public class CoralineTags {

        public static final TagKey<Block> HELPER_MINEABLE = TagKey.create(Registries.BLOCK,
                        new ResourceLocation(CoralineSystems.MOD_ID, "helper_mineable"));

        public static final TagKey<Block> FRAGILE = TagKey.create(Registries.BLOCK,
                        new ResourceLocation(CoralineSystems.MOD_ID, "fragile"));

        public static final TagKey<Item> ZOMBIE_HOLDABLE_ITEMS = TagKey.create(Registries.ITEM,
                        new ResourceLocation(CoralineSystems.MOD_ID, "zombie_holdable_items"));

        public static final TagKey<Block> RETURNABLE = TagKey.create(Registries.BLOCK,
                        new ResourceLocation(CoralineSystems.MOD_ID, "returnable"));

        public static final TagKey<Block> CAMEL_SPAWNABLE_ON = TagKey.create(Registries.BLOCK,
                        new ResourceLocation(CoralineSystems.MOD_ID, "camel_spawnable_on"));

        public static final TagKey<Block> REACTOR_CONVERTIBLE = TagKey.create(Registries.BLOCK,
                        new ResourceLocation(CoralineSystems.MOD_ID, "reactor_convertible"));

        public static final TagKey<Item> ZIPLINEABLE = TagKey.create(Registries.ITEM,
                        new ResourceLocation(CoralineSystems.MOD_ID, "ziplineable"));

        public static final TagKey<Item> WOLF_MEAT_DROPPED = TagKey.create(Registries.ITEM,
                        new ResourceLocation(CoralineSystems.MOD_ID, "wolf_meat_dropped"));

        public static final TagKey<Item> PIG_FOOD_DROPPED = TagKey.create(Registries.ITEM,
                        new ResourceLocation(CoralineSystems.MOD_ID, "pig_food_dropped"));

        public static final TagKey<Item> BEAR_TAMEABLES = TagKey.create(Registries.ITEM,
                        new ResourceLocation(CoralineSystems.MOD_ID, "bear_tameables"));

        public static final TagKey<Item> BEAR_HEALABLES = TagKey.create(Registries.ITEM,
                        new ResourceLocation(CoralineSystems.MOD_ID, "bear_healables"));

        public static final TagKey<Item> HORSE_FOOD_ITEMS = TagKey.create(Registries.ITEM,
                        new ResourceLocation(CoralineSystems.MOD_ID, "horse_food"));

        public static final TagKey<Item> CAT_FOOD_ITEMS = TagKey.create(Registries.ITEM,
                        new ResourceLocation(CoralineSystems.MOD_ID, "cat_food"));

        public static final TagKey<EntityType<?>> BLEEDS = TagKey.create(Registries.ENTITY_TYPE,
                        new ResourceLocation(CoralineSystems.MOD_ID, "bleeds"));

        public static final TagKey<EntityType<?>> FISHING_FISH = TagKey.create(Registries.ENTITY_TYPE,
                        new ResourceLocation(CoralineSystems.MOD_ID, "fishing_fish"));

        public static final TagKey<EntityType<?>> LEAF_PERCHERS = TagKey.create(Registries.ENTITY_TYPE,
                        new ResourceLocation(CoralineSystems.MOD_ID, "leaf_perchers"));

        public static final TagKey<Block> PASSABLE_FOLIAGE = TagKey.create(Registries.BLOCK,
                        new ResourceLocation(CoralineSystems.MOD_ID, "passable_foliage"));

        public static final TagKey<Biome> HAS_RUINS = TagKey.create(Registries.BIOME,
                        new ResourceLocation(CoralineSystems.MOD_ID, "has_ruins"));

        public static final TagKey<Biome> HAS_FALLEN_STAR = TagKey.create(Registries.BIOME,
                        new ResourceLocation(CoralineSystems.MOD_ID, "has_fallen_star"));

        public static final TagKey<EntityType<?>> FISH = TagKey.create(Registries.ENTITY_TYPE,
                new ResourceLocation(CoralineSystems.MOD_ID, "fish"));

        public static final TagKey<EntityType<?>> UNDERGROUND_SPAWN_WHITELIST = TagKey.create(Registries.ENTITY_TYPE,
                        new ResourceLocation(CoralineSystems.MOD_ID, "underground_spawn_whitelist"));
}