package net.zharok01.coralinesystems.registry;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.block.*;

		import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CoralineBlocks {

	public static final DeferredRegister<Block> REGISTRY =
			DeferredRegister.create(ForgeRegistries.BLOCKS, CoralineSystems.MOD_ID);

	/**
	 * Placeholder feature key for the huge white mushroom.
	 * Intentionally NOT backed by a registered ConfiguredFeature yet (no datagen
	 * set up for this mod). MushroomBlock requires a ResourceKey in its
	 * constructor, but since bonemeal no longer calls growMushroom() (see the
	 * MushroomBlock Mixin), an unresolved key here is harmless: any future code
	 * path that still calls growMushroom() will simply find the registry lookup
	 * empty and no-op. Wire this up to a real ConfiguredFeature once the
	 * "different, undefined" huge mushroom growth method is decided.
	 */
	public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_WHITE_MUSHROOM_FEATURE =
			ResourceKey.create(Registries.CONFIGURED_FEATURE, CoralineSystems.of("huge_white_mushroom"));

	private static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) {
		return true;
	}

	public static void registerRenderLayers() {
		ItemBlockRenderTypes.setRenderLayer(WALL_TORCH.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(TORCH.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(STATIC_PORTAL_BLOCK.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(WHITE_MUSHROOM.get(), RenderType.cutout());
	}

	public static final RegistryObject<Block> STATIC_PORTAL_BLOCK = registerWithoutItem("static_portal_block",
			() -> new StaticPortalBlock(BlockBehaviour.Properties.of()
					.noCollission()
					.noOcclusion()
					.strength(-1.0F) // Unbreakable like bedrock/portals
					.randomTicks()
					.sound(SoundType.GLASS))
	);

	public static final RegistryObject<Block> SUBWOOFER = register("subwoofer",
			() -> new SubwooferBlock(BlockBehaviour.Properties.of()
					.strength(3.5F, 6.0F)
					.sound(SoundType.STONE)
					.requiresCorrectToolForDrops())
	);

	public static final RegistryObject<Block> CONTAINER_BLOCK = register("container",
			() -> new ContainerBlock(BlockBehaviour.Properties.of()
					.strength(1.5F, 6.0F) // Similar to stone so it doesn't break instantly
					.sound(SoundType.STONE)
					//.requiresCorrectToolForDrops()
			)
	);

	public static final RegistryObject<Block> VIBRATION_SENSOR = register("vibration_sensor",
			() -> new VibrationSensorBlock(BlockBehaviour.Properties.of()
					.strength(1.5F).sound(SoundType.STONE).noOcclusion()));

	public static final RegistryObject<Block> TORCH = registerWithoutItem("torch",
			() -> new LimitedTorchBlock(BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY), ParticleTypes.FLAME)
	);

	public static final RegistryObject<Block> WALL_TORCH = registerWithoutItem("wall_torch",
			() -> new LimitedWallTorchBlock(BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY), ParticleTypes.FLAME)
	);

	public static final RegistryObject<Block> WHITE_MUSHROOM = register("white_mushroom",
			() -> new MushroomBlock(
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.SNOW)
							.noCollission()
							.randomTicks()
							.instabreak()
							.sound(SoundType.GRASS)
							.lightLevel(state -> 1)
							.hasPostProcess(CoralineBlocks::always)
							.pushReaction(PushReaction.DESTROY),
					HUGE_WHITE_MUSHROOM_FEATURE
			)
	);

	public static final RegistryObject<Block> WHITE_MUSHROOM_BLOCK = register("white_mushroom_block",
			() -> new HugeMushroomBlock(
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.SNOW)
							.instrument(NoteBlockInstrument.BASS)
							.strength(0.2F)
							.sound(SoundType.WOOD)
							.ignitedByLava()
			)
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