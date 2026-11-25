package net.zharok01.coralinesystems.registry;

import com.teamabnormals.atmospheric.core.registry.AtmosphericBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.block.*;
import net.zharok01.coralinesystems.util.CoralineDyeColors;
import net.zharok01.coralinesystems.mixin.accessor.FireBlockInvoker;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CoralineBlocks {
	public static final DeferredRegister<Block> REGISTRY =
			DeferredRegister.create(ForgeRegistries.BLOCKS, CoralineSystems.MOD_ID);

	public static final RegistryObject<Block> APPLE_FRUIT = registerWithoutItem("apple_fruit",
			() -> new HangingFruitBlock(
					BlockBehaviour.Properties.of()
							.noCollission()
							.randomTicks()
							.instabreak()
							.sound(SoundType.CROP)
							.pushReaction(PushReaction.DESTROY),
					CoralineBlockTags.APPLE_LEAVES,
					CoralineItems.APPLE,
					1, 2
			)
	);

	public static final RegistryObject<Block> BANANA_FRUIT = registerWithoutItem("banana_fruit",
			() -> new HangingFruitBlock(
					BlockBehaviour.Properties.of()
							.noCollission()
							.randomTicks()
							.instabreak()
							.sound(SoundType.CROP)
							.pushReaction(PushReaction.DESTROY),
					CoralineBlockTags.BANANA_LEAVES,
					CoralineItems.BANANA,
					3, 6,
					Block.box(3, 6, 3, 13, 16, 13),
					Block.box(3, 2, 3, 13, 16, 13),
					Block.box(3, 1, 3, 13, 16, 13)
			)
	);

	public static final RegistryObject<Block> MANGO_FRUIT = registerWithoutItem("mango_fruit",
			() -> new HangingFruitBlock(
					BlockBehaviour.Properties.of()
							.noCollission()
							.randomTicks()
							.instabreak()
							.sound(SoundType.CROP)
							.pushReaction(PushReaction.DESTROY),
					CoralineBlockTags.MANGO_LEAVES,
					CoralineItems.MANGO,
					1, 3
			)
	);

	public static final RegistryObject<Block> WALNUT_FRUIT = registerWithoutItem("walnut_fruit",
			() -> new HangingFruitBlock(
					BlockBehaviour.Properties.of()
							.noCollission()
							.randomTicks()
							.instabreak()
							.sound(SoundType.CROP)
							.pushReaction(PushReaction.DESTROY),
					CoralineBlockTags.WALNUT_LEAVES,
					CoralineItems.WALNUT,
					2, 4
			)
	);

	public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_WHITE_MUSHROOM_FEATURE =
			ResourceKey.create(Registries.CONFIGURED_FEATURE, CoralineSystems.of("huge_white_mushroom"));

	public static final RegistryObject<Block> CONCRETE = register("concrete",
			() -> new ConcreteBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F)
					.sound(SoundType.STONE))
	);

	public static final RegistryObject<Block> CEMENT = register("cement",
			() -> new CementBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_LIGHT_GRAY)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.strength(0.8F, 3.0F)
					.sound(SoundType.SAND))
	);

	public static final RegistryObject<Block> STATIC_PORTAL_BLOCK = registerWithoutItem("static_portal_block",
			() -> new StaticPortalBlock(BlockBehaviour.Properties.of()
					.noCollission()
					.noOcclusion()
					.strength(-1.0F)
					.sound(SoundType.GLASS))
	);

	public static final RegistryObject<Block> SUBWOOFER = register("subwoofer",
			() -> new SubwooferBlock(BlockBehaviour.Properties.of()
					.strength(3.5F, 12.0F)
					.sound(SoundType.STONE)
					.requiresCorrectToolForDrops())
	);

	public static final RegistryObject<Block> BLOODSTONE = register("bloodstone",
			() -> new BloodstoneBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.strength(3.0F, 6.0F)
					.sound(SoundType.STONE)
					.lightLevel(state -> state.getValue(BloodstoneBlock.LEVEL)))
	);

	public static final RegistryObject<Block> CONTAINER_BLOCK = register("container",
			() -> new ContainerBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.STONE)
					.strength(2.0F, 1200.0F)
					.sound(SoundType.METAL)
					.pushReaction(PushReaction.BLOCK)
			)
	);

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

	public static final RegistryObject<Block> CENTRIFUGE = register("centrifuge",
			() -> new CentrifugeBlock(
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.METAL)
							.requiresCorrectToolForDrops()
							.strength(1.5f, 1200.0f)
							.sound(SoundType.METAL)
							.lightLevel(blockState -> 15)
			)
	);

	public static final RegistryObject<Block> REACTOR = register("reactor",
			() -> new ReactorBlock(
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.METAL)
							.requiresCorrectToolForDrops()
							.strength(1.5f, 6.0f)
							.sound(SoundType.METAL)
							.lightLevel(blockState -> 15)
			)
	);

	public static final RegistryObject<Block> ALTAR = register("altar",
			() -> new AltarBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_PURPLE)
					.requiresCorrectToolForDrops()
					.strength(3.5F, 12.0F)
					.sound(SoundType.DEEPSLATE))
	);

	public static final RegistryObject<Block> TESSERACT = register("tesseract",
			() -> new TesseractBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_LIGHT_BLUE)
					.requiresCorrectToolForDrops()
					.strength(5.0F, 1200.0F)
					.sound(SoundType.AMETHYST)
					.lightLevel(state -> 8)
					.noOcclusion())
	);

	public static final RegistryObject<Block> SUSPICIOUS_BLOCK = register("suspicious_block",
			() -> new SuspiciousBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_LIGHT_BLUE)
					.sound(SoundType.AMETHYST)
					.requiresCorrectToolForDrops()
					.strength(5.0F, 6.0F)
			)
	);

	public static final RegistryObject<Block> HUGE_BRICK = register("huge_brick",
			() -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.STONE)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.strength(2.0F, 6.0F)
					.sound(SoundType.STONE))
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

	public static final RegistryObject<Block> BREWING_CAULDRON = registerWithoutItem("brewing_cauldron",
			() -> new net.zharok01.coralinesystems.block.BrewingCauldronBlock(
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.METAL)
							.requiresCorrectToolForDrops()
							.strength(2.0F)
							.sound(SoundType.METAL)
							.noOcclusion()
							.randomTicks()
			)
	);

	public static final RegistryObject<Block> BASALT_STAIRS = register("basalt_stairs",
			() -> new StairBlock(() -> Blocks.BASALT.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BASALT))
	);

	public static final RegistryObject<Block> BASALT_SLAB = register("basalt_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BASALT))
	);

	public static final RegistryObject<Block> BASALT_WALL = register("basalt_wall",
			() -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.BASALT))
	);

	public static final RegistryObject<Block> SMOOTH_BASALT_STAIRS = register("smooth_basalt_stairs",
			() -> new StairBlock(() -> Blocks.SMOOTH_BASALT.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SMOOTH_BASALT))
	);

	public static final RegistryObject<Block> SMOOTH_BASALT_SLAB = register("smooth_basalt_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SMOOTH_BASALT))
	);

	public static final RegistryObject<Block> SMOOTH_BASALT_WALL = register("smooth_basalt_wall",
			() -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.SMOOTH_BASALT))
	);

	public static final RegistryObject<Block> POLISHED_BASALT_STAIRS = register("polished_basalt_stairs",
			() -> new StairBlock(() -> Blocks.POLISHED_BASALT.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT))
	);

	public static final RegistryObject<Block> POLISHED_BASALT_SLAB = register("polished_basalt_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT))
	);

	public static final RegistryObject<Block> POLISHED_BASALT_WALL = register("polished_basalt_wall",
			() -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT))
	);

	public static final Map<DyeColor, RegistryObject<Block>> WOOL = new EnumMap<>(DyeColor.class);
	public static final Map<DyeColor, RegistryObject<Block>> CARPET = new EnumMap<>(DyeColor.class);
	public static final Map<DyeColor, RegistryObject<Block>> TERRACOTTA = new EnumMap<>(DyeColor.class);
	public static final Map<DyeColor, RegistryObject<Block>> TERRACOTTA_STAIRS = new EnumMap<>(DyeColor.class);
	public static final Map<DyeColor, RegistryObject<Block>> TERRACOTTA_SLAB = new EnumMap<>(DyeColor.class);
	public static final Map<DyeColor, RegistryObject<Block>> TERRACOTTA_WALL = new EnumMap<>(DyeColor.class);
	public static final Map<DyeColor, RegistryObject<Block>> STAINED_GLASS = new EnumMap<>(DyeColor.class);
	public static final Map<DyeColor, RegistryObject<Block>> STAINED_GLASS_PANE = new EnumMap<>(DyeColor.class);

	static {
		for (CoralineDyeColors color : CoralineDyeColors.values()) {
			DyeColor dye = color.dyeColor();
			String n = color.getName() + "_";
			WOOL.put(dye, register(n + "wool",
					() -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).mapColor(dye))
			));
			CARPET.put(dye, register(n + "carpet",
					() -> new WoolCarpetBlock(dye, BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).mapColor(dye))
			));
			TERRACOTTA.put(dye, register(n + "terracotta",
					() -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_TERRACOTTA).mapColor(dye))
			));
			TERRACOTTA_STAIRS.put(dye, register(n + "terracotta_stairs",
					() -> new StairBlock(
							() -> TERRACOTTA.get(dye).get().defaultBlockState(),
							BlockBehaviour.Properties.copy(Blocks.WHITE_TERRACOTTA).mapColor(dye))
			));
			TERRACOTTA_SLAB.put(dye, register(n + "terracotta_slab",
					() -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_TERRACOTTA).mapColor(dye))
			));
			TERRACOTTA_WALL.put(dye, register(n + "terracotta_wall",
					() -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_TERRACOTTA).mapColor(dye))
			));
			STAINED_GLASS.put(dye, register(n + "stained_glass",
					() -> new StainedGlassBlock(dye, BlockBehaviour.Properties.copy(Blocks.WHITE_STAINED_GLASS))
			));
			STAINED_GLASS_PANE.put(dye, register(n + "stained_glass_pane",
					() -> new StainedGlassPaneBlock(dye, BlockBehaviour.Properties.copy(Blocks.WHITE_STAINED_GLASS_PANE))
			));
		}
	}

	@Nullable
	public static ItemLike getWoolForColor(DyeColor color) {
		RegistryObject<Block> obj = WOOL.get(color);
		return obj != null ? obj.get() : null;
	}

	public static void registerFlammables() {
		FireBlockInvoker fire = (FireBlockInvoker) Blocks.FIRE;
		WOOL.values().forEach(ro -> fire.invokeSetFlammable(ro.get(), 30, 60));
		CARPET.values().forEach(ro -> fire.invokeSetFlammable(ro.get(), 60, 20));
		List<RegistryObject<Block>> grimwoodBlocks = List.of(
				AtmosphericBlocks.GRIMWOOD_LOG, AtmosphericBlocks.GRIMWOOD,
				AtmosphericBlocks.STRIPPED_GRIMWOOD_LOG, AtmosphericBlocks.STRIPPED_GRIMWOOD,
				AtmosphericBlocks.GRIMWOOD_LEAVES, AtmosphericBlocks.GRIMWOOD_LEAF_PILE,
				AtmosphericBlocks.GRIMWOOD_PLANKS, AtmosphericBlocks.GRIMWOOD_STAIRS,
				AtmosphericBlocks.GRIMWOOD_SLAB, AtmosphericBlocks.GRIMWOOD_FENCE,
				AtmosphericBlocks.GRIMWOOD_FENCE_GATE, AtmosphericBlocks.GRIMWOOD_BOARDS,
				AtmosphericBlocks.GRIMWOOD_BOOKSHELF, AtmosphericBlocks.CHISELED_GRIMWOOD_BOOKSHELF,
				AtmosphericBlocks.GRIMWOOD_LADDER
		);
		grimwoodBlocks.forEach(ro -> fire.invokeSetFlammable(ro.get(), 0, 0));
	}

	@SuppressWarnings("removal")
	public static void registerRenderLayers() {
		ItemBlockRenderTypes.setRenderLayer(WALL_TORCH.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(TORCH.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(STATIC_PORTAL_BLOCK.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(WHITE_MUSHROOM.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(RADIANT_SAPLING.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BANANA_FRUIT.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(MANGO_FRUIT.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(WALNUT_FRUIT.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(APPLE_FRUIT.get(), RenderType.cutout());
		STAINED_GLASS.values().forEach(ro -> ItemBlockRenderTypes.setRenderLayer(ro.get(), RenderType.translucent()));
		STAINED_GLASS_PANE.values().forEach(ro -> ItemBlockRenderTypes.setRenderLayer(ro.get(), RenderType.translucent()));
	}

	private static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) {
		return true;
	}

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
