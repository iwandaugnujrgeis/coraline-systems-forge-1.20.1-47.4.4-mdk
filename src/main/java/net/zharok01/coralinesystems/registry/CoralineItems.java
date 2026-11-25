package net.zharok01.coralinesystems.registry;

import net.minecraft.core.Direction;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.CoralineDyeColors;
import net.zharok01.coralinesystems.entity.projectile.ThrownCaimanEgg;
import net.zharok01.coralinesystems.entity.projectile.ThrownPlatypusEgg;
import net.zharok01.coralinesystems.entity.projectile.ThrownTurtleEgg;
import net.zharok01.coralinesystems.item.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class CoralineItems {

	public static final DeferredRegister<Item> REGISTRY =
			DeferredRegister.create(ForgeRegistries.ITEMS, CoralineSystems.MOD_ID);

	public static final RegistryObject<Item> TORCH = register("torch", () -> new StandingAndWallBlockItem(
			CoralineBlocks.TORCH.get(),
			CoralineBlocks.WALL_TORCH.get(),
			new Item.Properties(),
			Direction.DOWN
	));

	public static final RegistryObject<Item> RAW_VENISON = register("raw_venison",
			() -> new Item(new Item.Properties().stacksTo(8)
					.food(new FoodProperties.Builder()
							.nutrition(3)
							.saturationMod(0.3F)
							.meat()
							.build()))
	);

	public static final RegistryObject<Item> COOKED_VENISON = register("cooked_venison",
			() -> new Item(new Item.Properties().stacksTo(8)
					.food(new FoodProperties.Builder()
							.nutrition(7)
							.saturationMod(0.8F)
							.meat()
							.build()))
	);

	public static final RegistryObject<Item> ONYX = register("onyx",
			() -> new OnyxItem(new Item.Properties().stacksTo(64)));

	public static final RegistryObject<Item> ONYX_SWORD = register("onyx_sword",
			() -> new OnyxSwordItem(
					OnyxTier.INSTANCE,
					3,
					-2.4F,
					new Item.Properties().durability(800)
			));

	public static final RegistryObject<Item> APPLE = register("apple",
			() -> new Item(new Item.Properties().stacksTo(8)
					.food(new FoodProperties.Builder()
							.nutrition(4)
							.saturationMod(0.3F)
							.build()))
	);

	public static final RegistryObject<Item> BANANA = register("banana",
			() -> new Item(new Item.Properties().stacksTo(8)
					.food(new FoodProperties.Builder()
							.nutrition(5)
							.saturationMod(0.3F)
							.build()))
	);

	public static final RegistryObject<Item> MANGO = register("mango",
			() -> new Item(new Item.Properties().stacksTo(8)
					.food(new FoodProperties.Builder()
							.nutrition(5)
							.saturationMod(0.4F)
							.build()))
	);

	public static final RegistryObject<Item> WALNUT = register("walnut",
			() -> new Item(new Item.Properties().stacksTo(8)
					.food(new FoodProperties.Builder()
							.nutrition(3)
							.saturationMod(0.6F)
							.build()))
	);

	public static final RegistryObject<Item> DIMENSIONAL_SHARD = REGISTRY.register("dimensional_shard",
			() -> new DimensionalShardItem(new Item.Properties().stacksTo(16)));

	public static final RegistryObject<Item> TUNING_FORK = register("tuning_fork",
			() -> new TuningForkItem(new Item.Properties().stacksTo(1).durability(128)));

	public static final RegistryObject<Item> TIMEDUST = register("timedust",
			() -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> HELPER_SPAWN_EGG = register("helper_spawn_egg",
			() -> new ForgeSpawnEggItem(IsotopicEntities.HELPER, 0x322136, 0x1fe770, new Item.Properties()));

	public static final RegistryObject<Item> MONSTER_SPAWN_EGG = register("monster_spawn_egg",
			() -> new ForgeSpawnEggItem(IsotopicEntities.MONSTER, 0x1A1A1A, 0xE0E0FF, new Item.Properties()));

	public static final RegistryObject<Item> BRUME_SPAWN_EGG = register("brume_spawn_egg",
			() -> new ForgeSpawnEggItem(IsotopicEntities.BRUME, 0x108E9C, 0xDEF7F7, new Item.Properties()));

	public static final RegistryObject<Item> MILK_BOTTLE = register("milk_bottle",
			() -> new MilkBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<Item> ORB_SPAWN_EGG = register("orb_spawn_egg",
			() -> new ForgeSpawnEggItem(IsotopicEntities.ORB, 0xA47CC2, 0xF095F5, new Item.Properties()));

	public static final RegistryObject<Item> COBALT_PANTS = REGISTRY.register("cobalt_pants",
			() -> new ArmorItem(CobaltPants.COBALT, ArmorItem.Type.LEGGINGS,
					new Item.Properties()));

	public static final RegistryObject<Item> ORB = REGISTRY.register(
			"orb",
			() -> new OrbItem(new Item.Properties().stacksTo(64))
	);

	public static final RegistryObject<Item> OBSIDIAN_BOAT = REGISTRY.register(
			"obsidian_boat",
			ObsidianBoatItem::new
	);

	public static final RegistryObject<Item> TEA_LEAVES = register("tea_leaves",
			() -> new Item(new Item.Properties().stacksTo(16)
			));

	public static final RegistryObject<Item> SOUL = register("soul",
			() -> new Item(new Item.Properties().stacksTo(64)));

	public static final RegistryObject<Item> SOUL_HEART = register("soul_heart",
			() -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> WINE_BOTTLE = register("wine_bottle",
			() -> new WineBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<Item> KOMBUCHA_BOTTLE = register("kombucha_bottle",
			() -> new KombuchaBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<CoralineNameTagItem> NAME_TAG = register("name_tag",
			() -> new CoralineNameTagItem(new Item.Properties().stacksTo(1)));

	public static final RegistryObject<Item> TEA_BOTTLE = register("tea_bottle",
			() -> new TeaBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<Item> DREGS_BOTTLE = register("dregs_bottle",
			() -> new DregsBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<Item> MULBERRY_JUICE_BOTTLE = register("mulberry_juice_bottle",
			() -> new MulberryJuiceBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<Item> PLATYPUS_EGG = register("platypus_egg",
			() -> new ModdedEggItem(
					ThrownPlatypusEgg::new,
					new Item.Properties().stacksTo(16)
			));

	public static final RegistryObject<Item> CAIMAN_EGG = register("caiman_egg",
			() -> new ModdedEggItem(
					ThrownCaimanEgg::new,
					new Item.Properties().stacksTo(16)
			));

	public static final RegistryObject<Item> TURTLE_EGG = register("turtle_egg",
			() -> new ModdedEggItem(
					ThrownTurtleEgg::new,
					new Item.Properties().stacksTo(16)
			));

	public static final Map<DyeColor, RegistryObject<DyeItem>> DYES = new EnumMap<>(DyeColor.class);

	static {
		for (CoralineDyeColors color : CoralineDyeColors.values()) {
			DyeColor dye = color.dyeColor();
			DYES.put(dye, REGISTRY.register(
					color.getName() + "_dye",
					() -> new DyeItem(dye, new Item.Properties())
			));
		}
	}

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> supplier) {
		return REGISTRY.register(name, supplier);
	}

	public static void register(IEventBus bus) {
		REGISTRY.register(bus);
	}
}