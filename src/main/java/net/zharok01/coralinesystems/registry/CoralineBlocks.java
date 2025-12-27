package net.zharok01.coralinesystems.registry;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.block.LimitedTorchBlock;
import net.zharok01.coralinesystems.content.block.LimitedWallTorchBlock;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CoralineBlocks {

	public static void registerRenderLayers() {
		ItemBlockRenderTypes.setRenderLayer(WALL_TORCH.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(TORCH.get(), RenderType.cutout());
	}

	public static final DeferredRegister<Block> REGISTRY =
		DeferredRegister.create(ForgeRegistries.BLOCKS, CoralineSystems.MOD_ID);

	public static final RegistryObject<Block> TORCH = registerWithoutItem("torch",
		() -> new LimitedTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH), ParticleTypes.FLAME)
	);

	public static final RegistryObject<Block> WALL_TORCH = registerWithoutItem("wall_torch",
		() -> new LimitedWallTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).dropsLike(TORCH.get()), ParticleTypes.FLAME)
	);

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> supplier) {
		return register(name, supplier, new Item.Properties());
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> supplier, Item.Properties itemProperties) {
		return register(name, supplier, BlockItem::new, itemProperties);
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> supplier, BiFunction<Block, Item.Properties, BlockItem> itemFunction, Item.Properties itemProperties) {
		final RegistryObject<T> result = REGISTRY.register(name, supplier);
		if (itemProperties != null) CoralineItems.REGISTRY.register(name, () -> itemFunction == null ? new BlockItem(result.get(), itemProperties) : itemFunction.apply(result.get(), itemProperties));
		return result;
	}

	private static <T extends Block> RegistryObject<T> registerWithoutItem(String name, Supplier<T> supplier) {
		return REGISTRY.register(name, supplier);
	}

	public static void register(IEventBus bus) {
		REGISTRY.register(bus);
	}

}
