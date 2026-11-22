package net.zharok01.coralinesystems.worldgen.feature;

import com.legacy.rediscovered.registry.RediscoveredBlocks;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("deprecation")
public class LabyrinthFeature extends Feature<NoneFeatureConfiguration> {

    private static final int GRID = 11;
    private static final int CELL_PITCH = 4;
    private static final int CELL_OPEN_WIDTH = CELL_PITCH - 1;
    private static final int SIZE = GRID * CELL_PITCH + 1;
    private static final int CENTER_OFFSET = SIZE / 2;

    private static final int SLAB_THICKNESS = 2;
    private static final int OPEN_HEIGHT = 4;
    private static final int LEVEL_PITCH = SLAB_THICKNESS + OPEN_HEIGHT;
    private static final int MIN_FLOORS = 2;
    private static final int MAX_FLOORS = 5;

    private static final int MIN_SPIKE_PIT_DEPTH = 5;
    private static final int MAX_SPIKE_PIT_DEPTH = 7;

    private static final int MIN_TRAP_POTIONS = 3;
    private static final int MAX_TRAP_POTIONS = 6;

    private static final int BOTTOM_SAFETY_MARGIN = 5;
    private static final int SURFACE_COVER = 4;
    private static final int COVER_SAMPLE_STEP = 11;
    private static final float MAX_AIR_FRACTION = 0.25F;
    private static final float MAX_SLAB_AIR_FRACTION = 0.2F;

    private static final int MIN_ACTIVE_CELLS = 45;
    private static final int MAX_ACTIVE_CELLS = 70;
    private static final int WALKER_MIN_STEPS = 4;
    private static final int WALKER_MAX_STEPS = 10;
    private static final float WALKER_TURN_CHANCE = 0.35F;

    private static final int ROOM_PLACEMENT_ATTEMPTS = 20;
    private static final int ROOM_CORNER_OFFSET = CELL_PITCH * 2 - 2;
    private static final int ROOM_CENTER_OFFSET = ROOM_CORNER_OFFSET / 2;
    private static final float SPAWNER_ROOM_CHANCE = 0.3F;

    private static final float RANDOM_PICK_CHANCE = 0.4F;
    private static final float BRAID_CHANCE = 0.03F;
    private static final float DECORATIVE_PILLAR_CHANCE = 0.3F;
    private static final float WALL_DECAY_CHANCE = 0.03F;
    private static final float COBWEB_CHANCE = 0.05F;
    private static final float BASE_MOSSY_CHANCE = 0.3F;
    private static final float MOSSY_PER_FLOOR = 0.12F;
    private static final float SPIKE_COVERAGE = 0.75F;
    private static final int MAX_TRAPS_PER_FLOOR = 3;

    private static final int TILE_NONE = -1;
    private static final int TILE_OPEN = 0;
    private static final int TILE_WALL = 1;
    private static final int TILE_PILLAR = 2;
    private static final int TILE_EDGE_WALL = 3;

    private static final int PIT_NONE = 0;
    private static final int PIT_DROP = 1;
    private static final int PIT_SPIKED = 2;

    @SuppressWarnings("removal")
    private static final ResourceLocation ACCENT_BRICKS_ID = new ResourceLocation("kubejs", "smooth_stone_bricks");

    @SuppressWarnings("removal")
    private static final ResourceLocation ROOM_LOOT = new ResourceLocation("gamma", "chests/dungeon_loot");

    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final BlockState COBWEB = Blocks.COBWEB.defaultBlockState();

    public LabyrinthFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int floors = random.nextInt(MIN_FLOORS, MAX_FLOORS + 1);
        int minX = (origin.getX() & ~15) + 8 - CENTER_OFFSET;
        int minZ = (origin.getZ() & ~15) + 8 - CENTER_OFFSET;
        int maxX = minX + SIZE - 1;
        int maxZ = minZ + SIZE - 1;

        int topSlabY = origin.getY();
        int bottomSlabY = topSlabY - (floors - 1) * LEVEL_PITCH;
        int volumeBottom = bottomSlabY - MAX_SPIKE_PIT_DEPTH - 1;
        int volumeTop = topSlabY + LEVEL_PITCH;

        if (volumeBottom - 1 < level.getMinBuildHeight() + BOTTOM_SAFETY_MARGIN || volumeTop + 1 >= level.getMaxBuildHeight()) {
            return false;
        }

        int shallowestSurface = Integer.MAX_VALUE;
        for (int x = minX; x <= maxX; x += COVER_SAMPLE_STEP) {
            for (int z = minZ; z <= maxZ; z += COVER_SAMPLE_STEP) {
                shallowestSurface = Math.min(shallowestSurface, level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z));
            }
        }
        if (volumeTop + SURFACE_COVER >= shallowestSurface) {
            return false;
        }

        List<Maze> mazes = new ArrayList<>();
        for (int floor = 0; floor < floors; floor++) {
            mazes.add(this.coraline$planFloor(random, floor == 0 ? null : mazes.get(floor - 1)));
        }
        this.coraline$planPits(random, mazes);

        List<Room> allRooms = new ArrayList<>();
        for (Maze maze : mazes) {
            allRooms.addAll(maze.rooms);
        }
        if (!allRooms.isEmpty() && allRooms.stream().noneMatch(room -> room.spawner)) {
            allRooms.get(random.nextInt(allRooms.size())).spawner = true;
        }

        boolean[][] scanColumns = new boolean[SIZE + 2][SIZE + 2];
        for (Maze maze : mazes) {
            for (int lx = 0; lx < SIZE; lx++) {
                for (int lz = 0; lz < SIZE; lz++) {
                    if (this.coraline$tileAt(maze, lx, lz) == TILE_NONE) {
                        continue;
                    }
                    for (int offsetX = 0; offsetX <= 2; offsetX++) {
                        for (int offsetZ = 0; offsetZ <= 2; offsetZ++) {
                            scanColumns[lx + offsetX][lz + offsetZ] = true;
                        }
                    }
                }
            }
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int airBlocks = 0;
        int scannedBlocks = 0;
        int slabAirBlocks = 0;
        int slabScannedBlocks = 0;
        for (int scanX = 0; scanX < SIZE + 2; scanX++) {
            for (int scanZ = 0; scanZ < SIZE + 2; scanZ++) {
                if (!scanColumns[scanX][scanZ]) {
                    continue;
                }
                for (int y = volumeBottom - 1; y <= volumeTop + 1; y++) {
                    BlockState state = level.getBlockState(pos.set(minX + scanX - 1, y, minZ + scanZ - 1));
                    if (!state.getFluidState().isEmpty()
                            || state.is(BlockTags.PLANKS)
                            || state.is(Blocks.SPAWNER)
                            || state.is(Blocks.CHEST)
                            || state.is(Blocks.RAIL)
                            || state.is(Blocks.COBBLESTONE)
                            || state.is(Blocks.MOSSY_COBBLESTONE)
                            || state.is(Blocks.STONE_BRICKS)
                            || state.is(Blocks.MOSSY_STONE_BRICKS)
                            || state.is(Blocks.DEEPSLATE_BRICKS)
                            || state.is(Blocks.SCULK)) {
                        return false;
                    }

                    boolean inSlab = y >= bottomSlabY && y <= topSlabY + SLAB_THICKNESS - 1
                            && Math.floorMod(topSlabY + SLAB_THICKNESS - 1 - y, LEVEL_PITCH) < SLAB_THICKNESS;
                    if (inSlab) {
                        slabScannedBlocks++;
                    }
                    if (state.isAir()) {
                        airBlocks++;
                        if (inSlab) {
                            slabAirBlocks++;
                        }
                    }
                    scannedBlocks++;
                }
            }
        }
        if (airBlocks > scannedBlocks * MAX_AIR_FRACTION || slabAirBlocks > slabScannedBlocks * MAX_SLAB_AIR_FRACTION) {
            return false;
        }

        Predicate<BlockState> replaceable = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        Block accentBlock = ForgeRegistries.BLOCKS.getValue(ACCENT_BRICKS_ID);
        BlockState accent;
        if (accentBlock == Blocks.AIR) {
            accent = Blocks.STONE_BRICKS.defaultBlockState();
        } else {
            assert accentBlock != null;
            accent = accentBlock.defaultBlockState();
        }

        for (int floor = 0; floor < floors; floor++) {
            Maze maze = mazes.get(floor);
            Maze above = floor == 0 ? null : mazes.get(floor - 1);
            int slabY = topSlabY - floor * LEVEL_PITCH;
            float mossyChance = BASE_MOSSY_CHANCE + MOSSY_PER_FLOOR * floor;

            this.coraline$buildFloor(level, random, maze, above, minX, minZ, slabY, mossyChance, accent, replaceable);
            this.coraline$carvePits(level, random, maze, minX, minZ, slabY, mossyChance, replaceable);
            this.coraline$placeTraps(level, random, maze, minX, minZ, slabY);
            for (Room room : maze.rooms) {
                this.coraline$furnishRoom(level, random, room, minX, minZ, slabY + SLAB_THICKNESS);
            }
        }

        return true;
    }

    private Maze coraline$planFloor(RandomSource random, Maze above) {
        Maze maze = new Maze();

        int[] seed = above == null
                ? new int[] {GRID / 2 + random.nextInt(-1, 2), GRID / 2 + random.nextInt(-1, 2)}
                : above.cells.get(random.nextInt(above.cells.size()));
        this.coraline$activate(maze, seed[0], seed[1]);

        int targetCells = random.nextInt(MIN_ACTIVE_CELLS, MAX_ACTIVE_CELLS + 1);
        while (maze.cells.size() < targetCells) {
            int[] from = maze.cells.get(random.nextInt(maze.cells.size()));
            int walkerX = from[0];
            int walkerZ = from[1];
            Direction heading = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int steps = random.nextInt(WALKER_MIN_STEPS, WALKER_MAX_STEPS + 1);
            for (int step = 0; step < steps; step++) {
                if (random.nextFloat() < WALKER_TURN_CHANCE) {
                    heading = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                }
                int nextX = walkerX + heading.getStepX();
                int nextZ = walkerZ + heading.getStepZ();
                if (nextX < 0 || nextX >= GRID || nextZ < 0 || nextZ >= GRID) {
                    heading = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    continue;
                }
                walkerX = nextX;
                walkerZ = nextZ;
                this.coraline$activate(maze, walkerX, walkerZ);
            }
        }

        int roomCount = random.nextInt(1, 3);
        for (int i = 0; i < roomCount; i++) {
            for (int attempt = 0; attempt < ROOM_PLACEMENT_ATTEMPTS; attempt++) {
                int[] anchor = maze.cells.get(random.nextInt(maze.cells.size()));
                int cellX = Mth.clamp(anchor[0] - random.nextInt(2), 0, GRID - 2);
                int cellZ = Mth.clamp(anchor[1] - random.nextInt(2), 0, GRID - 2);
                if (maze.roomCell[cellX][cellZ] || maze.roomCell[cellX + 1][cellZ]
                        || maze.roomCell[cellX][cellZ + 1] || maze.roomCell[cellX + 1][cellZ + 1]) {
                    continue;
                }
                for (int offsetX = 0; offsetX <= 1; offsetX++) {
                    for (int offsetZ = 0; offsetZ <= 1; offsetZ++) {
                        maze.roomCell[cellX + offsetX][cellZ + offsetZ] = true;
                        this.coraline$activate(maze, cellX + offsetX, cellZ + offsetZ);
                    }
                }
                this.coraline$link(maze, cellX, cellZ, Direction.EAST);
                this.coraline$link(maze, cellX, cellZ + 1, Direction.EAST);
                this.coraline$link(maze, cellX, cellZ, Direction.SOUTH);
                this.coraline$link(maze, cellX + 1, cellZ, Direction.SOUTH);
                maze.rooms.add(new Room(cellX, cellZ, random.nextFloat() < SPAWNER_ROOM_CHANCE));
                break;
            }
        }

        boolean[] visited = new boolean[GRID * GRID];
        List<Integer> frontier = new ArrayList<>();
        int[] start = maze.cells.get(random.nextInt(maze.cells.size()));
        int startCell = start[1] * GRID + start[0];
        visited[startCell] = true;
        frontier.add(startCell);
        while (!frontier.isEmpty()) {
            int index = random.nextFloat() < RANDOM_PICK_CHANCE ? random.nextInt(frontier.size()) : frontier.size() - 1;
            int cell = frontier.get(index);
            int cellX = cell % GRID;
            int cellZ = cell / GRID;

            List<Direction> options = new ArrayList<>(4);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                int neighborX = cellX + direction.getStepX();
                int neighborZ = cellZ + direction.getStepZ();
                if (this.coraline$isActive(maze, neighborX, neighborZ) && !visited[neighborZ * GRID + neighborX]) {
                    options.add(direction);
                }
            }
            if (options.isEmpty()) {
                frontier.remove(index);
                continue;
            }

            Direction next = options.get(random.nextInt(options.size()));
            this.coraline$link(maze, cellX, cellZ, next);
            int neighbor = (cellZ + next.getStepZ()) * GRID + cellX + next.getStepX();
            visited[neighbor] = true;
            frontier.add(neighbor);
        }

        for (int cellX = 0; cellX < GRID; cellX++) {
            for (int cellZ = 0; cellZ < GRID; cellZ++) {
                if (this.coraline$isActive(maze, cellX, cellZ) && this.coraline$isActive(maze, cellX + 1, cellZ)
                        && !maze.eastOpen[cellX][cellZ] && random.nextFloat() < BRAID_CHANCE) {
                    maze.eastOpen[cellX][cellZ] = true;
                }
                if (this.coraline$isActive(maze, cellX, cellZ) && this.coraline$isActive(maze, cellX, cellZ + 1)
                        && !maze.southOpen[cellX][cellZ] && random.nextFloat() < BRAID_CHANCE) {
                    maze.southOpen[cellX][cellZ] = true;
                }
            }
        }

        for (int px = 0; px <= GRID; px++) {
            for (int pz = 0; pz <= GRID; pz++) {
                if (!this.coraline$touchesActiveCell(maze, px, pz)) {
                    continue;
                }
                int[] segments = {
                        this.coraline$verticalSegment(maze, px, pz - 1),
                        this.coraline$verticalSegment(maze, px, pz),
                        this.coraline$horizontalSegment(maze, px - 1, pz),
                        this.coraline$horizontalSegment(maze, px, pz)
                };
                boolean touchesWall = false;
                for (int segment : segments) {
                    if (segment == TILE_WALL || segment == TILE_EDGE_WALL) {
                        touchesWall = true;
                    }
                }
                maze.pillar[px][pz] = touchesWall || random.nextFloat() < DECORATIVE_PILLAR_CHANCE;
            }
        }
        for (Room room : maze.rooms) {
            maze.pillar[room.cellX + 1][room.cellZ + 1] = false;
        }

        return maze;
    }

    private void coraline$planPits(RandomSource random, List<Maze> mazes) {
        for (int floor = 0; floor < mazes.size(); floor++) {
            Maze maze = mazes.get(floor);
            Maze above = floor == 0 ? null : mazes.get(floor - 1);
            Maze below = floor == mazes.size() - 1 ? null : mazes.get(floor + 1);

            List<Integer> dropCandidates = new ArrayList<>();
            List<Integer> spikeCandidates = new ArrayList<>();
            List<int[]> straightCells = new ArrayList<>();
            for (int cellX = 0; cellX < GRID; cellX++) {
                for (int cellZ = 0; cellZ < GRID; cellZ++) {
                    if (!maze.active[cellX][cellZ] || maze.roomCell[cellX][cellZ]) {
                        continue;
                    }
                    if (above != null && above.pit[cellX][cellZ] == PIT_DROP) {
                        continue;
                    }

                    int links = this.coraline$linkCount(maze, cellX, cellZ);
                    if (links == 1) {
                        if (below == null || !below.active[cellX][cellZ]) {
                            spikeCandidates.add(cellZ * GRID + cellX);
                        } else if (!below.roomCell[cellX][cellZ]) {
                            dropCandidates.add(cellZ * GRID + cellX);
                        }
                    } else if (links == 2) {
                        boolean eastWest = this.coraline$hasLink(maze, cellX, cellZ, Direction.EAST) && this.coraline$hasLink(maze, cellX, cellZ, Direction.WEST);
                        boolean northSouth = this.coraline$hasLink(maze, cellX, cellZ, Direction.NORTH) && this.coraline$hasLink(maze, cellX, cellZ, Direction.SOUTH);
                        if (eastWest || northSouth) {
                            straightCells.add(new int[] {cellX, cellZ, eastWest ? 1 : 0});
                        }
                    }
                }
            }

            if (below != null) {
                int drops = Math.min(dropCandidates.size(), random.nextInt(1, 3));
                for (int i = 0; i < drops; i++) {
                    int cell = dropCandidates.remove(random.nextInt(dropCandidates.size()));
                    maze.pit[cell % GRID][cell / GRID] = PIT_DROP;
                }
            }

            int spikes = Math.min(spikeCandidates.size(), random.nextInt(1, 4));
            for (int i = 0; i < spikes; i++) {
                int cell = spikeCandidates.remove(random.nextInt(spikeCandidates.size()));
                maze.pit[cell % GRID][cell / GRID] = PIT_SPIKED;
                maze.pitDepth[cell % GRID][cell / GRID] = random.nextInt(MIN_SPIKE_PIT_DEPTH, MAX_SPIKE_PIT_DEPTH + 1);
            }

            int traps = Math.min(straightCells.size(), random.nextInt(MAX_TRAPS_PER_FLOOR + 1));
            for (int i = 0; i < traps; i++) {
                int[] cell = straightCells.remove(random.nextInt(straightCells.size()));
                for (int offset = 0; offset < CELL_OPEN_WIDTH; offset++) {
                    if (cell[2] == 1) {
                        maze.trapTiles.add(new int[] {cell[0] * CELL_PITCH + 2, cell[1] * CELL_PITCH + 1 + offset});
                    } else {
                        maze.trapTiles.add(new int[] {cell[0] * CELL_PITCH + 1 + offset, cell[1] * CELL_PITCH + 2});
                    }
                }
            }
        }
    }

    private void coraline$buildFloor(WorldGenLevel level, RandomSource random, Maze maze, Maze above, int minX, int minZ, int slabY, float mossyChance, BlockState accent, Predicate<BlockState> replaceable) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int openStartY = slabY + SLAB_THICKNESS;
        int roofY = openStartY + OPEN_HEIGHT;

        for (int lx = 0; lx < SIZE; lx++) {
            for (int lz = 0; lz < SIZE; lz++) {
                int tile = this.coraline$tileAt(maze, lx, lz);
                if (tile == TILE_NONE) {
                    continue;
                }

                int x = minX + lx;
                int z = minZ + lz;

                for (int layer = 0; layer < SLAB_THICKNESS; layer++) {
                    this.coraline$placeShell(level, pos.set(x, slabY + layer, z), this.coraline$stone(random, mossyChance), replaceable);
                }

                if (above == null || this.coraline$tileAt(above, lx, lz) == TILE_NONE) {
                    this.coraline$placeShell(level, pos.set(x, roofY, z), this.coraline$stone(random, mossyChance), replaceable);
                }

                for (int dy = 0; dy < OPEN_HEIGHT; dy++) {
                    pos.set(x, openStartY + dy, z);
                    if (tile == TILE_PILLAR) {
                        this.coraline$placeShell(level, pos, accent, replaceable);
                    } else if (tile == TILE_EDGE_WALL || (tile == TILE_WALL && random.nextFloat() >= WALL_DECAY_CHANCE)) {
                        this.coraline$placeShell(level, pos, this.coraline$stone(random, mossyChance), replaceable);
                    } else if (tile == TILE_OPEN && dy == OPEN_HEIGHT - 1 && random.nextFloat() < COBWEB_CHANCE
                            && level.getBlockState(pos.above()).isSolid()) {
                        this.safeSetBlock(level, pos, COBWEB, replaceable);
                    } else {
                        this.safeSetBlock(level, pos, AIR, replaceable);
                    }
                }
            }
        }
    }

    private void coraline$carvePits(WorldGenLevel level, RandomSource random, Maze maze, int minX, int minZ, int slabY, float mossyChance, Predicate<BlockState> replaceable) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int cellX = 0; cellX < GRID; cellX++) {
            for (int cellZ = 0; cellZ < GRID; cellZ++) {
                int pit = maze.pit[cellX][cellZ];
                if (pit == PIT_NONE) {
                    continue;
                }

                int startX = minX + cellX * CELL_PITCH + 1;
                int startZ = minZ + cellZ * CELL_PITCH + 1;

                if (pit == PIT_DROP) {
                    for (int dx = 0; dx < CELL_OPEN_WIDTH; dx++) {
                        for (int dz = 0; dz < CELL_OPEN_WIDTH; dz++) {
                            for (int y = slabY; y < slabY + SLAB_THICKNESS; y++) {
                                this.safeSetBlock(level, pos.set(startX + dx, y, startZ + dz), AIR, replaceable);
                            }
                        }
                    }
                    continue;
                }

                int digBottomY = slabY - maze.pitDepth[cellX][cellZ];
                for (int dx = -1; dx <= CELL_OPEN_WIDTH; dx++) {
                    for (int dz = -1; dz <= CELL_OPEN_WIDTH; dz++) {
                        for (int y = digBottomY - 1; y < slabY; y++) {
                            this.safeSetBlock(level, pos.set(startX + dx, y, startZ + dz), this.coraline$stone(random, mossyChance), replaceable);
                        }
                    }
                }
                for (int dx = 0; dx < CELL_OPEN_WIDTH; dx++) {
                    for (int dz = 0; dz < CELL_OPEN_WIDTH; dz++) {
                        for (int y = digBottomY; y < slabY + SLAB_THICKNESS; y++) {
                            this.safeSetBlock(level, pos.set(startX + dx, y, startZ + dz), AIR, replaceable);
                        }
                    }
                }
                this.coraline$scatterSpikes(level, random, startX, digBottomY, startZ);
            }
        }
    }

    private void coraline$scatterSpikes(WorldGenLevel level, RandomSource random, int startX, int y, int startZ) {
        BlockState spikeState = RediscoveredBlocks.spikes.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < CELL_OPEN_WIDTH; dx++) {
            for (int dz = 0; dz < CELL_OPEN_WIDTH; dz++) {
                if (random.nextFloat() < SPIKE_COVERAGE) {
                    level.setBlock(pos.set(startX + dx, y, startZ + dz), spikeState, 2);
                }
            }
        }
    }

    private void coraline$placeTraps(WorldGenLevel level, RandomSource random, Maze maze, int minX, int minZ, int slabY) {
        for (int[] tile : maze.trapTiles) {
            BlockPos dispenserPos = new BlockPos(minX + tile[0], slabY + SLAB_THICKNESS - 1, minZ + tile[1]);
            if (!level.getBlockState(dispenserPos).isSolid() || !level.getBlockState(dispenserPos.below()).isSolid()) {
                continue;
            }
            level.setBlock(dispenserPos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP), 2);
            level.setBlock(dispenserPos.above(), Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 2);
            if (level.getBlockEntity(dispenserPos) instanceof DispenserBlockEntity dispenser) {
                ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_HARMING);
                int potionCount = random.nextIntBetweenInclusive(MIN_TRAP_POTIONS, MAX_TRAP_POTIONS);
                for (int i = 0; i < potionCount; i++) {
                    int slot;
                    do {
                        slot = random.nextInt(dispenser.getContainerSize());
                    } while (!dispenser.getItem(slot).isEmpty());
                    dispenser.setItem(slot, potion.copy());
                }
            }
        }
    }

    private void coraline$furnishRoom(WorldGenLevel level, RandomSource random, Room room, int minX, int minZ, int walkY) {
        int startX = minX + room.cellX * CELL_PITCH + 1;
        int startZ = minZ + room.cellZ * CELL_PITCH + 1;

        int chestCount = 2;
        if (room.spawner) {
            BlockPos center = new BlockPos(startX + ROOM_CENTER_OFFSET, walkY, startZ + ROOM_CENTER_OFFSET);
            if (level.getBlockState(center.below()).isSolid()) {
                level.setBlock(center, Blocks.SPAWNER.defaultBlockState(), 2);
                if (level.getBlockEntity(center) instanceof SpawnerBlockEntity spawner) {
                    spawner.setEntityId(DungeonHooks.getRandomDungeonMob(random), random);
                }
                chestCount = 1;
            }
        }

        List<ChestSpot> spots = new ArrayList<>();
        for (int i = 0; i <= ROOM_CORNER_OFFSET; i++) {
            for (int j = 0; j <= ROOM_CORNER_OFFSET; j++) {
                if (i != 0 && i != ROOM_CORNER_OFFSET && j != 0 && j != ROOM_CORNER_OFFSET) {
                    continue;
                }
                BlockPos spot = new BlockPos(startX + i, walkY, startZ + j);
                if (!level.isEmptyBlock(spot) || !level.getBlockState(spot.below()).isSolid()) {
                    continue;
                }
                for (Direction wallSide : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockState(spot.relative(wallSide)).isSolid()) {
                        spots.add(new ChestSpot(spot, wallSide.getOpposite()));
                    }
                }
            }
        }

        List<BlockPos> placed = new ArrayList<>();
        while (placed.size() < chestCount && !spots.isEmpty()) {
            ChestSpot spot = spots.remove(random.nextInt(spots.size()));
            if (placed.stream().anyMatch(other -> other.distManhattan(spot.pos()) < 2)) {
                continue;
            }
            level.setBlock(spot.pos(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, spot.facing()), 2);
            RandomizableContainerBlockEntity.setLootTable(level, random, spot.pos(), ROOM_LOOT);
            placed.add(spot.pos());
        }
    }

    private void coraline$placeShell(WorldGenLevel level, BlockPos pos, BlockState state, Predicate<BlockState> replaceable) {
        if (level.getBlockState(pos).isAir()) {
            return;
        }
        this.safeSetBlock(level, pos, state, replaceable);
    }

    private BlockState coraline$stone(RandomSource random, float mossyChance) {
        return random.nextFloat() < mossyChance ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
    }

    private void coraline$activate(Maze maze, int cellX, int cellZ) {
        if (!maze.active[cellX][cellZ]) {
            maze.active[cellX][cellZ] = true;
            maze.cells.add(new int[] {cellX, cellZ});
        }
    }

    private boolean coraline$isActive(Maze maze, int cellX, int cellZ) {
        return cellX >= 0 && cellX < GRID && cellZ >= 0 && cellZ < GRID && maze.active[cellX][cellZ];
    }

    private boolean coraline$touchesActiveCell(Maze maze, int px, int pz) {
        return this.coraline$isActive(maze, px - 1, pz - 1) || this.coraline$isActive(maze, px, pz - 1)
                || this.coraline$isActive(maze, px - 1, pz) || this.coraline$isActive(maze, px, pz);
    }

    private int coraline$tileAt(Maze maze, int lx, int lz) {
        int cellX = lx / CELL_PITCH;
        int cellZ = lz / CELL_PITCH;
        boolean onXLine = lx % CELL_PITCH == 0;
        boolean onZLine = lz % CELL_PITCH == 0;

        if (onXLine && onZLine) {
            if (!this.coraline$touchesActiveCell(maze, cellX, cellZ)) {
                return TILE_NONE;
            }
            return maze.pillar[cellX][cellZ] ? TILE_PILLAR : TILE_OPEN;
        }
        if (onXLine) {
            return this.coraline$verticalSegment(maze, cellX, cellZ);
        }
        if (onZLine) {
            return this.coraline$horizontalSegment(maze, cellX, cellZ);
        }
        return this.coraline$isActive(maze, cellX, cellZ) ? TILE_OPEN : TILE_NONE;
    }

    private int coraline$verticalSegment(Maze maze, int px, int cellZ) {
        boolean west = this.coraline$isActive(maze, px - 1, cellZ);
        boolean east = this.coraline$isActive(maze, px, cellZ);
        if (west && east) {
            return maze.eastOpen[px - 1][cellZ] ? TILE_OPEN : TILE_WALL;
        }
        return west || east ? TILE_EDGE_WALL : TILE_NONE;
    }

    private int coraline$horizontalSegment(Maze maze, int cellX, int pz) {
        boolean north = this.coraline$isActive(maze, cellX, pz - 1);
        boolean south = this.coraline$isActive(maze, cellX, pz);
        if (north && south) {
            return maze.southOpen[cellX][pz - 1] ? TILE_OPEN : TILE_WALL;
        }
        return north || south ? TILE_EDGE_WALL : TILE_NONE;
    }

    private void coraline$link(Maze maze, int cellX, int cellZ, Direction direction) {
        switch (direction) {
            case EAST -> maze.eastOpen[cellX][cellZ] = true;
            case WEST -> maze.eastOpen[cellX - 1][cellZ] = true;
            case SOUTH -> maze.southOpen[cellX][cellZ] = true;
            default -> maze.southOpen[cellX][cellZ - 1] = true;
        }
    }

    private boolean coraline$hasLink(Maze maze, int cellX, int cellZ, Direction direction) {
        return switch (direction) {
            case EAST -> maze.eastOpen[cellX][cellZ];
            case WEST -> cellX > 0 && maze.eastOpen[cellX - 1][cellZ];
            case SOUTH -> maze.southOpen[cellX][cellZ];
            default -> cellZ > 0 && maze.southOpen[cellX][cellZ - 1];
        };
    }

    private int coraline$linkCount(Maze maze, int cellX, int cellZ) {
        int links = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.coraline$hasLink(maze, cellX, cellZ, direction)) {
                links++;
            }
        }
        return links;
    }

    private record ChestSpot(BlockPos pos, Direction facing) {
    }

    private static final class Room {
        final int cellX;
        final int cellZ;
        boolean spawner;

        Room(int cellX, int cellZ, boolean spawner) {
            this.cellX = cellX;
            this.cellZ = cellZ;
            this.spawner = spawner;
        }
    }

    private static final class Maze {
        final boolean[][] active = new boolean[GRID][GRID];
        final boolean[][] eastOpen = new boolean[GRID][GRID];
        final boolean[][] southOpen = new boolean[GRID][GRID];
        final boolean[][] roomCell = new boolean[GRID][GRID];
        final boolean[][] pillar = new boolean[GRID + 1][GRID + 1];
        final int[][] pit = new int[GRID][GRID];
        final int[][] pitDepth = new int[GRID][GRID];
        final List<int[]> cells = new ArrayList<>();
        final List<Room> rooms = new ArrayList<>();
        final List<int[]> trapTiles = new ArrayList<>();
    }
}