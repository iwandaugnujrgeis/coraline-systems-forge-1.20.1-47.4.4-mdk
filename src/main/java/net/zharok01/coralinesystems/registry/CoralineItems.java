package net.zharok01.coralinesystems.registry;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;

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

	public static final RegistryObject<Item> STATIC_BLOCK_ITEM = REGISTRY.register("static_block",
			() -> new BlockItem(CoralineBlocks.STATIC_BLOCK.get(), new Item.Properties()));

	public static final RegistryObject<Item> HELPER_SPAWN_EGG = register("helper_spawn_egg",
			() -> new ForgeSpawnEggItem(IsotopicEntities.HELPER, 0x322136, 0x1fe770, new Item.Properties()));

	public static final RegistryObject<Item> MONSTER_SPAWN_EGG = register("monster_spawn_egg",
			() -> new ForgeSpawnEggItem(IsotopicEntities.MONSTER, 0x1A1A1A, 0xE0E0FF, new Item.Properties()));

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> supplier) {
		return REGISTRY.register(name, supplier);
	}

	public static void register(IEventBus bus) {
		REGISTRY.register(bus);
	}

}
