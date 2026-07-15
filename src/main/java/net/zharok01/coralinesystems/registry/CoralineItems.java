package net.zharok01.coralinesystems.registry;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.item.CobaltPants;
import net.zharok01.coralinesystems.item.OrbItem;

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

	/**
	 * The product of a spoiled Wine batch (design doc Section 3, "Failure
	 * State — Spoiling") and the required culture input for Kombucha (Section
	 * 4, step 3). Named "Dregs" per creative direction — deliberately avoids
	 * "SCOBY" or other real-world fermentation science terminology per the
	 * design doc's explicit naming constraint (Section 6).
	 */
	public static final RegistryObject<Item> DREGS = register("dregs",
			() -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> WINE = register("wine",
			() -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> KOMBUCHA = register("kombucha",
			() -> new Item(new Item.Properties()));

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> supplier) {
		return REGISTRY.register(name, supplier);
	}

	public static void register(IEventBus bus) {
		REGISTRY.register(bus);
	}
}
