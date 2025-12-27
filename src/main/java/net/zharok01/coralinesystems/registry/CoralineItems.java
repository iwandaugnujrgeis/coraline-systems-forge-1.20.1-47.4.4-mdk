package net.zharok01.coralinesystems.registry;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
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

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> supplier) {
		return REGISTRY.register(name, supplier);
	}

	public static void register(IEventBus bus) {
		REGISTRY.register(bus);
	}

}
