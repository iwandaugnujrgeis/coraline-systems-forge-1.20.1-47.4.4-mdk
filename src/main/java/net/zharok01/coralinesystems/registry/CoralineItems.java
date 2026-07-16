package net.zharok01.coralinesystems.registry;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.item.CobaltPants;
import net.zharok01.coralinesystems.item.DregsBottleItem;
import net.zharok01.coralinesystems.item.DregsBucketItem;
import net.zharok01.coralinesystems.item.KombuchaBottleItem;
import net.zharok01.coralinesystems.item.KombuchaBucketItem;
import net.zharok01.coralinesystems.item.MulberryJuiceBottleItem;
import net.zharok01.coralinesystems.item.MulberryJuiceBucketItem;
import net.zharok01.coralinesystems.item.OrbItem;
import net.zharok01.coralinesystems.item.TeaBottleItem;
import net.zharok01.coralinesystems.item.TeaBucketItem;
import net.zharok01.coralinesystems.item.WineBottleItem;
import net.zharok01.coralinesystems.item.WineBucketItem;

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

	public static final RegistryObject<Item> DIMENSIONAL_SHARD = REGISTRY.register("dimensional_shard",
			() -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> HELPER_SPAWN_EGG = register("helper_spawn_egg",
			() -> new ForgeSpawnEggItem(IsotopicEntities.HELPER, 0x322136, 0x1fe770, new Item.Properties()));

	public static final RegistryObject<Item> MONSTER_SPAWN_EGG = register("monster_spawn_egg",
			() -> new ForgeSpawnEggItem(IsotopicEntities.MONSTER, 0x1A1A1A, 0xE0E0FF, new Item.Properties()));

	public static final RegistryObject<Item> BRUME_SPAWN_EGG = register("brume_spawn_egg",
			() -> new ForgeSpawnEggItem(IsotopicEntities.BRUME, 0x108E9C, 0xDEF7F7, new Item.Properties()));

	public static final RegistryObject<Item> COBALT_PANTS = REGISTRY.register("cobalt_pants",
			() -> new ArmorItem(CobaltPants.COBALT, ArmorItem.Type.LEGGINGS,
					new Item.Properties()));

	public static final RegistryObject<Item> ORB = REGISTRY.register(
			"orb",
			() -> new OrbItem(new Item.Properties().stacksTo(16))
	);

	public static final RegistryObject<Item> MULBERRIES = register("mulberries",
			() -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> YEAST = register("yeast",
			() -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> TEA_LEAVES = register("tea_leaves",
			() -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> DREGS = register("dregs",
			() -> new Item(new Item.Properties()));

	// ── Drink items — all ten now on AbstractCoralineDrinkItem (Session 1.8) ──
	// Strength (Wine/Tea/Mulberry Juice) is carried via CoralineFluidUtils and
	// surfaced in-tooltip automatically by the base class; Kombucha/Dregs are
	// single-strength and pass no strength translation key.

	public static final RegistryObject<Item> WINE_BOTTLE = register("wine_bottle",
			() -> new WineBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<Item> WINE_BUCKET = register("wine_bucket",
			() -> new WineBucketItem(new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

	public static final RegistryObject<Item> KOMBUCHA_BOTTLE = register("kombucha_bottle",
			() -> new KombuchaBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<Item> KOMBUCHA_BUCKET = register("kombucha_bucket",
			() -> new KombuchaBucketItem(new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

	public static final RegistryObject<Item> TEA_BOTTLE = register("tea_bottle",
			() -> new TeaBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<Item> TEA_BUCKET = register("tea_bucket",
			() -> new TeaBucketItem(new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

	public static final RegistryObject<Item> DREGS_BOTTLE = register("dregs_bottle",
			() -> new DregsBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<Item> DREGS_BUCKET = register("dregs_bucket",
			() -> new DregsBucketItem(new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

	public static final RegistryObject<Item> MULBERRY_JUICE_BOTTLE = register("mulberry_juice_bottle",
			() -> new MulberryJuiceBottleItem(new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE)));

	public static final RegistryObject<Item> MULBERRY_JUICE_BUCKET = register("mulberry_juice_bucket",
			() -> new MulberryJuiceBucketItem(new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

	// ---

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> supplier) {
		return REGISTRY.register(name, supplier);
	}

	public static void register(IEventBus bus) {
		REGISTRY.register(bus);
	}
}
