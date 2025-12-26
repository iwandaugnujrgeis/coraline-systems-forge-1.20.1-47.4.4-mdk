package net.zharok01.coralinesystems.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;

import java.util.function.Supplier;

public class CoralineItems {

	public static final DeferredRegister<Item> REGISTRY =
		DeferredRegister.create(ForgeRegistries.ITEMS, CoralineSystems.MOD_ID);

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> supplier) {
		return REGISTRY.register(name, supplier);
	}

	public static void register(IEventBus bus) {
		REGISTRY.register(bus);
	}

}
