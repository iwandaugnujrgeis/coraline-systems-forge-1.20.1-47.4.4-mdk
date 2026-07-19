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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
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
		ItemBlockRenderTypes.setRenderLayer(MAGLEV_RAIL.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(POWERED_MAGLEV_RAIL.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(RADIANT_SAPLING.get(), RenderType.cutout());
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
							.sound(CoralineSoundTypes.MUSHROOM)
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

	public static final RegistryObject<Block> MAGLEV_RAIL = register("maglev_rail",
			() -> new MaglevRailBlock(BlockBehaviour.Properties.of()
					.noCollission()
					.strength(0.7F)
					.sound(SoundType.METAL))
	);

	public static final RegistryObject<Block> POWERED_MAGLEV_RAIL = register("powered_maglev_rail",
			() -> new PoweredMaglevRailBlock(BlockBehaviour.Properties.of()
					.noCollission()
					.strength(0.7F)
					.sound(SoundType.METAL))
	);

	public static final RegistryObject<Block> ORGANIC_COMPOST = register("organic_compost",
			() -> new OrganicCompostBlock(
					BlockBehaviour.Properties.copy(Blocks.DIRT)
							.randomTicks()
							.strength(0.6F)
							.sound(SoundType.GRAVEL)
			)
	);

	public static final RegistryObject<Block> COMPOST_FARMLAND = register("organic_compost_farmland",
			() -> new CompostFarmlandBlock(
					BlockBehaviour.Properties.copy(Blocks.FARMLAND)
							.randomTicks()
			)
	);

	public static final RegistryObject<Block> BASKET = register("basket",
			() -> new BasketBlock(
					BlockBehaviour.Properties.of()
							.strength(1.5F)
							.sound(SoundType.WOOD)
			)
	);

	public static final RegistryObject<Block> CENTRIFUGE = register("centrifuge",
			() -> new CentrifugeBlock(
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.METAL)
							.requiresCorrectToolForDrops()
							.strength(3.5f, 6.0f)
							.sound(SoundType.METAL)
							// Idle: no light. Activated (speeding up time): full
							// torch brightness (14). Locked (Redstone-blocked):
							// a dim warning glow, a bit under a redstone torch's
							// level 7 — using 5 here. ACTIVATED takes priority
							// over LOCKED in the unusual case both are briefly
							// true at once (mid-abort coast-down).
							.lightLevel(blockState -> {
								if (blockState.getValue(net.zharok01.coralinesystems.block.CentrifugeBlock.ACTIVATED)) {
									return 14;
								}
								if (blockState.getValue(net.zharok01.coralinesystems.block.CentrifugeBlock.LOCKED)) {
									return 7;
								}
								return 0;
							})
			)
	);

	public static final RegistryObject<Block> RADIANT_LEAVES = register("radiant_leaves",
			() -> new RadiantLeavesBlock(
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.COLOR_GREEN)
							.strength(0.2F)
							.randomTicks()
							.sound(SoundType.CHERRY_LEAVES)
							.noOcclusion()
							.isValidSpawn((state, level, pos, type) -> false)
							.isSuffocating((state, level, pos) -> false)
							.isViewBlocking((state, level, pos) -> false)
							.ignitedByLava()
							.pushReaction(PushReaction.DESTROY)
			)
	);

	public static final RegistryObject<Block> RADIANT_SAPLING = register("radiant_sapling",
			() -> new RadiantSaplingBlock(
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.PLANT)
							.noCollission()
							.randomTicks()
							.instabreak()
							.sound(SoundType.GRASS)
							.pushReaction(PushReaction.DESTROY)
			)
	);

	public static final RegistryObject<Block> BREWING_CAULDRON = register("brewing_cauldron",
			() -> new net.zharok01.coralinesystems.block.BrewingCauldronBlock(
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.METAL)
							.strength(2.0F)
							.sound(SoundType.METAL)
							.noOcclusion()
							.randomTicks() // Session 2: required for BrewingCauldronBlock#randomTick to ever fire
			)
	);

	//---

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