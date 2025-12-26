package net.zharok01.coralinesystems.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CoralineBlocks {

	public static final DeferredRegister<Block> REGISTRY =
		DeferredRegister.create(ForgeRegistries.BLOCKS, CoralineSystems.MOD_ID);

//	public static final RegistryObject<Block> TEST = register("test",
//		() -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE))
//	);

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> supplier) {
		return register(name, supplier, new Item.Properties());
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> supplier, Item.Properties itemProperties) {
		return register(name, supplier, BlockItem::new, itemProperties);
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> supplier, BiFunction<Block, Item.Properties, BlockItem> itemFunction, Item.Properties itemProperties) {
		final RegistryObject<T> registryObject = REGISTRY.register(name, supplier);
		if (itemProperties != null) CoralineItems.REGISTRY.register(name, () -> itemFunction == null ? new BlockItem(registryObject.get(), itemProperties) : itemFunction.apply(registryObject.get(), itemProperties));
		return registryObject;
	}

	public static void register(IEventBus bus) {
		REGISTRY.register(bus);
	}

}
