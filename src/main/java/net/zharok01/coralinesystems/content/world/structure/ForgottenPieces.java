package net.zharok01.coralinesystems.content.world.structure;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.zharok01.coralinesystems.CoralineSystems;

import javax.annotation.Nullable;
import java.util.List;

/**
 * All piece types for the Forgotten Dungeon structure.
 *
 * Piece catalogue (12 types):
 *   StartRoom       — 13×9×13  origin room, 4 exits + decorative pillars
 *   Corridor        —  5×5×14  basic hallway
 *   WideCorridor    —  7×6×16  wide hallway with pilasters and torches
 *   TurnLeft        —  9×5×9   left-turn connector
 *   TurnRight       —  9×5×9   right-turn connector
 *   CrossroadRoom   — 13×7×13  4-way junction with central pillar cluster
 *   SmallRoom       —  9×7×9   small room, 1–3 exits, rubble floor detail
 *   LargeHall       — 17×11×17 grand hall with four 2×2 pillars and vaulted detail
 *   SpawnerRoom     —  9×7×9   room with a random-mob spawner
 *   TreasureRoom    —  9×7×9   iron-door vault, 2–3 loot chests + guard spawner
 *   PrisonBlock     — 13×5×9   cell block with iron-bar walls and skeleton spawners
 *   StaircaseDown   —  5×13×10 descends ~8 blocks to a lower level
 *   StaircaseUp     —  5×13×10 ascends ~8 blocks — can breach the surface as a ruin
 *
 * Piece weights are tuned so the dungeon grows wide before it grows deep,
 * creating the sprawling horizontal layout typical of old-school megadungeons.
 */
public class ForgottenPieces {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Maximum recursive generation depth — higher than Stronghold (50) to
     *  encourage a much larger, more convoluted dungeon. */
    private static final int MAX_DEPTH = 80;

    /** How far from the start room (in blocks) pieces are still generated.
     *  Stronghold uses 112; we use 160 for a wider horizontal spread. */
    private static final int HORIZONTAL_RADIUS = 160;

    /** No piece's bounding box may have a minY below this world-space value.
     *  Mirrors the Stronghold's LOWEST_Y_POSITION check in isOkBox(). */
    private static final int MIN_Y = 10;

    /** Loot table used for all chests in the dungeon. */
    private static final ResourceLocation FORGOTTEN_LOOT =
            new ResourceLocation(CoralineSystems.MOD_ID, "chests/forgotten_dungeon.json");

    // -----------------------------------------------------------------------
    // Piece weight table
    // -----------------------------------------------------------------------

    private static final PieceWeight[] PIECE_WEIGHTS = {
        new PieceWeight(Corridor.class,        35,  0),   // unlimited corridors
        new PieceWeight(WideCorridor.class,    15,  0),
        new PieceWeight(TurnLeft.class,        20,  0),
        new PieceWeight(TurnRight.class,       20,  0),
        new PieceWeight(CrossroadRoom.class,   10, 12),
        new PieceWeight(SmallRoom.class,       15, 10),
        new PieceWeight(LargeHall.class,        8,  6),
        new PieceWeight(SpawnerRoom.class,     20, 15),   // high weight — spawners everywhere
        new PieceWeight(TreasureRoom.class,     8,  5),
        new PieceWeight(PrisonBlock.class,     10,  6),
        new PieceWeight(StaircaseDown.class,   12,  8),
        new PieceWeight(StaircaseUp.class,      5,  3) {
            @Override public boolean doPlace(int genDepth) {
                // Only place upward stairs once the dungeon is deep enough that
                // the piece actually ascends from underground rather than starting
                // above sea level.
                return super.doPlace(genDepth) && genDepth > 6;
            }
        },
    };

    private static List<PieceWeight> currentPieces;
    private static int totalWeight;

    /** Called by ForgottenStructure before each new dungeon is generated. */
    public static void resetPieces() {
        currentPieces = Lists.newArrayList();
        for (PieceWeight w : PIECE_WEIGHTS) {
            w.placeCount = 0;
            currentPieces.add(w);
        }
    }

    /** Recomputes totalWeight and returns true if any capped piece still has
     *  remaining quota — mirrors StrongholdPieces.updatePieceWeight(). */
    private static boolean updatePieceWeights() {
        boolean hasMore = false;
        totalWeight = 0;
        for (PieceWeight w : currentPieces) {
            if (w.maxPlaceCount > 0 && w.placeCount < w.maxPlaceCount) hasMore = true;
            totalWeight += w.weight;
        }
        return hasMore;
    }

    // -----------------------------------------------------------------------
    // Piece factory / spawning helpers
    // -----------------------------------------------------------------------

    @Nullable
    static StructurePiece generateAndAddPiece(
            StartRoom start, StructurePieceAccessor pieces, RandomSource random,
            int x, int y, int z, @Nullable Direction dir, int genDepth) {

        if (genDepth > MAX_DEPTH) return null;
        if (Math.abs(x - start.getBoundingBox().minX()) > HORIZONTAL_RADIUS) return null;
        if (Math.abs(z - start.getBoundingBox().minZ()) > HORIZONTAL_RADIUS) return null;

        StructurePiece piece = generatePieceFromDoor(start, pieces, random, x, y, z, dir, genDepth + 1);
        if (piece != null) {
            pieces.addPiece(piece);
            start.pendingChildren.add(piece);
        }
        return piece;
    }

    @Nullable
    private static StructurePiece generatePieceFromDoor(
            StartRoom start, StructurePieceAccessor pieces, RandomSource random,
            int x, int y, int z, Direction dir, int genDepth) {

        if (!updatePieceWeights()) return null;

        for (int attempt = 0; attempt < 5; attempt++) {
            int roll = random.nextInt(totalWeight);
            for (PieceWeight w : currentPieces) {
                roll -= w.weight;
                if (roll < 0) {
                    if (!w.doPlace(genDepth)) break;
                    StructurePiece piece = instantiatePiece(w.pieceClass, pieces, random, x, y, z, dir, genDepth);
                    if (piece != null) {
                        w.placeCount++;
                        if (!w.isValid()) currentPieces.remove(w);
                        return piece;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static StructurePiece instantiatePiece(
            Class<? extends ForgottenPiece> cls, StructurePieceAccessor pieces,
            RandomSource random, int x, int y, int z, Direction dir, int genDepth) {
        if (cls == Corridor.class)       return Corridor.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == WideCorridor.class)   return WideCorridor.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == TurnLeft.class)       return TurnLeft.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == TurnRight.class)      return TurnRight.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == CrossroadRoom.class)  return CrossroadRoom.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == SmallRoom.class)      return SmallRoom.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == LargeHall.class)      return LargeHall.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == SpawnerRoom.class)    return SpawnerRoom.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == TreasureRoom.class)   return TreasureRoom.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == PrisonBlock.class)    return PrisonBlock.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == StaircaseDown.class)  return StaircaseDown.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == StaircaseUp.class)    return StaircaseUp.createPiece(pieces, random, x, y, z, dir, genDepth);
        return null;
    }

    // -----------------------------------------------------------------------
    // Block selector — randomises stone-brick variants for a ruined aesthetic
    // -----------------------------------------------------------------------

    static final ForgottenBlockSelector STONE_SELECTOR = new ForgottenBlockSelector();

    public static class ForgottenBlockSelector extends StructurePiece.BlockSelector {
        @Override
        public void next(RandomSource random, int x, int y, int z, boolean isWall) {
            float f = random.nextFloat();
            if      (f < 0.20F) this.next = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            else if (f < 0.35F) this.next = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            else if (f < 0.42F) this.next = Blocks.COBBLESTONE.defaultBlockState();
            else                 this.next = Blocks.STONE_BRICKS.defaultBlockState();
        }
    }

    // -----------------------------------------------------------------------
    // Door type enum
    // -----------------------------------------------------------------------

    public enum DoorType { OPENING, WOOD_DOOR, IRON_DOOR, GRATE }

    // -----------------------------------------------------------------------
    // PieceWeight — mirrors StrongholdPieces.PieceWeight
    // -----------------------------------------------------------------------

    static class PieceWeight {
        final Class<? extends ForgottenPiece> pieceClass;
        final int weight;
        final int maxPlaceCount;
        int placeCount = 0;

        PieceWeight(Class<? extends ForgottenPiece> cls, int weight, int maxPlaceCount) {
            this.pieceClass = cls;
            this.weight = weight;
            this.maxPlaceCount = maxPlaceCount;
        }

        boolean doPlace(int genDepth) {
            return maxPlaceCount == 0 || placeCount < maxPlaceCount;
        }

        boolean isValid() {
            return maxPlaceCount == 0 || placeCount < maxPlaceCount;
        }
    }

    // -----------------------------------------------------------------------
    // Abstract base piece
    // -----------------------------------------------------------------------

    public abstract static class ForgottenPiece extends StructurePiece {

        protected DoorType entryDoor = DoorType.OPENING;

        protected ForgottenPiece(StructurePieceType type, int genDepth, BoundingBox box) {
            super(type, genDepth, box);
        }

        protected ForgottenPiece(StructurePieceType type, CompoundTag tag) {
            super(type, tag);
            this.entryDoor = DoorType.valueOf(tag.getString("ED"));
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
            tag.putString("ED", this.entryDoor.name());
        }

        protected static boolean isOkBox(BoundingBox box) {
            return box != null && box.minY() > MIN_Y;
        }

        protected DoorType randomDoor(RandomSource random) {
            return switch (random.nextInt(5)) {
                case 2  -> DoorType.WOOD_DOOR;
                case 3  -> DoorType.GRATE;
                case 4  -> DoorType.IRON_DOOR;
                default -> DoorType.OPENING;
            };
        }

        /**
         * Carves a 3-wide × 3-tall doorway at local position (x, y, z) on the Z face.
         * All coordinates are in LOCAL space — the orientation transform in placeBlock
         * handles mapping to world space automatically.
         */
        protected void generateDoor(WorldGenLevel level, RandomSource random, BoundingBox box,
                                    DoorType type, int x, int y, int z) {
            switch (type) {
                case OPENING -> this.generateBox(level, box, x, y, z, x + 2, y + 2, z, CAVE_AIR, CAVE_AIR, false);

                case WOOD_DOOR -> {
                    // Stone brick frame
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x,     y,     z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x,     y + 1, z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x,     y + 2, z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x + 1, y + 2, z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x + 2, y + 2, z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x + 2, y + 1, z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x + 2, y,     z, box);
                    // Door (rotation applied automatically by placeBlock)
                    this.placeBlock(level, Blocks.OAK_DOOR.defaultBlockState(), x + 1, y, z, box);
                    this.placeBlock(level, Blocks.OAK_DOOR.defaultBlockState()
                            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, box);
                }

                case IRON_DOOR -> {
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x,     y,     z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x,     y + 1, z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x,     y + 2, z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x + 1, y + 2, z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x + 2, y + 2, z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x + 2, y + 1, z, box);
                    this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), x + 2, y,     z, box);
                    this.placeBlock(level, Blocks.IRON_DOOR.defaultBlockState(), x + 1, y, z, box);
                    this.placeBlock(level, Blocks.IRON_DOOR.defaultBlockState()
                            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, box);
                    // Buttons on both sides so the door can always be opened
                    this.placeBlock(level, Blocks.STONE_BUTTON.defaultBlockState()
                            .setValue(ButtonBlock.FACING, Direction.NORTH), x + 2, y + 1, z + 1, box);
                    this.placeBlock(level, Blocks.STONE_BUTTON.defaultBlockState()
                            .setValue(ButtonBlock.FACING, Direction.SOUTH), x + 2, y + 1, z - 1, box);
                }

                case GRATE -> {
                    // Clear the central opening
                    this.placeBlock(level, CAVE_AIR, x + 1, y,     z, box);
                    this.placeBlock(level, CAVE_AIR, x + 1, y + 1, z, box);
                    // Iron bar frame (full 3×3 surround + horizontal bar at top)
                    for (int by = y; by <= y + 2; by++) {
                        this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), x,     by, z, box);
                        this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), x + 2, by, z, box);
                    }
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), x + 1, y + 2, z, box);
                }
            }
        }

        /** Places a wall torch facing `facing` at local (x, y, z). */
        protected void placeTorch(WorldGenLevel level, BoundingBox box,
                                  int x, int y, int z, Direction facing) {
            this.placeBlock(level,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing),
                    x, y, z, box);
        }

        /** Places a monster spawner and sets its entity type. */
        protected void placeSpawner(WorldGenLevel level, BoundingBox box, RandomSource random,
                                    int x, int y, int z, EntityType<?> entityType) {
            BlockPos worldPos = this.getWorldPos(x, y, z);
            if (box.isInside(worldPos)) {
                level.setBlock(worldPos, Blocks.SPAWNER.defaultBlockState(), 2);
                if (level.getBlockEntity(worldPos) instanceof SpawnerBlockEntity spawner) {
                    spawner.setEntityId(entityType, random);
                }
            }
        }

        // ---- Child-spawning helpers (mirrors StrongholdPiece) ---------------

        @Nullable
        protected StructurePiece generateChildForward(StartRoom start, StructurePieceAccessor pieces,
                                                       RandomSource random, int offsetX, int offsetY) {
            Direction dir = this.getOrientation();
            if (dir == null) return null;
            return switch (dir) {
                case NORTH -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() + offsetX,
                        this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() - 1, dir, this.getGenDepth());
                case SOUTH -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() + offsetX,
                        this.boundingBox.minY() + offsetY,
                        this.boundingBox.maxZ() + 1, dir, this.getGenDepth());
                case WEST  -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() - 1,
                        this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() + offsetX, dir, this.getGenDepth());
                case EAST  -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.maxX() + 1,
                        this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() + offsetX, dir, this.getGenDepth());
                default    -> null;
            };
        }

        /** Mirrors vanilla generateSmallDoorChildLeft exactly. */
        @Nullable
        protected StructurePiece generateChildLeft(StartRoom start, StructurePieceAccessor pieces,
                                                    RandomSource random, int offsetY, int offsetX) {
            Direction dir = this.getOrientation();
            if (dir == null) return null;
            return switch (dir) {
                case NORTH, SOUTH -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() - 1,
                        this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() + offsetX,
                        Direction.WEST, this.getGenDepth());
                case WEST, EAST   -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() + offsetX,
                        this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() - 1,
                        Direction.NORTH, this.getGenDepth());
                default           -> null;
            };
        }

        /** Mirrors vanilla generateSmallDoorChildRight exactly. */
        @Nullable
        protected StructurePiece generateChildRight(StartRoom start, StructurePieceAccessor pieces,
                                                     RandomSource random, int offsetY, int offsetX) {
            Direction dir = this.getOrientation();
            if (dir == null) return null;
            return switch (dir) {
                case NORTH, SOUTH -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.maxX() + 1,
                        this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() + offsetX,
                        Direction.EAST, this.getGenDepth());
                case WEST, EAST   -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() + offsetX,
                        this.boundingBox.minY() + offsetY,
                        this.boundingBox.maxZ() + 1,
                        Direction.SOUTH, this.getGenDepth());
                default           -> null;
            };
        }
    }

    // =======================================================================
    //  PIECE IMPLEMENTATIONS
    // =======================================================================

    // -----------------------------------------------------------------------
    // StartRoom — 13×9×13, origin of the entire dungeon
    // -----------------------------------------------------------------------

    public static class StartRoom extends ForgottenPiece {

        /** Pieces waiting to have addChildren() called (same pattern as
         *  StrongholdPieces.StartPiece.pendingChildren). */
        public final List<StructurePiece> pendingChildren = Lists.newArrayList();

        public StartRoom(RandomSource random, int x, int z) {
            super(CoralineStructurePieceTypes.FORGOTTEN_START_ROOM, 0,
                    new BoundingBox(x, 64, z, x + 12, 72, z + 12));
            this.setOrientation(Direction.SOUTH);
            this.entryDoor = this.randomDoor(random);
        }

        public StartRoom(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_START_ROOM, tag);
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            // Spawn corridors on all four sides immediately
            this.generateChildForward(this, pieces, random, 5, 1);
            this.generateChildLeft(this, pieces, random, 1, 5);
            this.generateChildRight(this, pieces, random, 1, 5);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            // Outer shell (hollow=true → only fills the 1-block outer skin)
            this.generateBox(level, box, 0, 0, 0, 12, 8, 12, true, random, STONE_SELECTOR);
            // Carve the interior
            this.generateBox(level, box, 1, 1, 1, 11, 7, 11, CAVE_AIR, CAVE_AIR, false);

            // Four corner pillars
            for (int[] p : new int[][]{{2, 2}, {10, 2}, {2, 10}, {10, 10}}) {
                this.generateBox(level, box, p[0], 1, p[1], p[0], 6, p[1],
                        Blocks.STONE_BRICKS.defaultBlockState(),
                        Blocks.STONE_BRICKS.defaultBlockState(), false);
            }

            // Mossy-brick floor pattern — central diamond
            for (int fx = 4; fx <= 8; fx++) {
                for (int fz = 4; fz <= 8; fz++) {
                    this.placeBlock(level, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), fx, 1, fz, box);
                }
            }

            // Doors on all four faces (south = entry, others random)
            this.generateDoor(level, random, box, this.entryDoor, 5, 1,  0);
            this.generateDoor(level, random, box, this.randomDoor(random), 5, 1, 12);
            this.generateDoor(level, random, box, this.randomDoor(random), 0, 1,  5);
            this.generateDoor(level, random, box, this.randomDoor(random), 12, 1, 5);

            // Torches on the pillars
            this.placeTorch(level, box,  3, 4,  2, Direction.EAST);
            this.placeTorch(level, box,  9, 4,  2, Direction.WEST);
            this.placeTorch(level, box,  3, 4, 10, Direction.EAST);
            this.placeTorch(level, box,  9, 4, 10, Direction.WEST);
        }
    }

    // -----------------------------------------------------------------------
    // Corridor — 5×5×14
    // -----------------------------------------------------------------------

    public static class Corridor extends ForgottenPiece {

        public Corridor(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_CORRIDOR, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public Corridor(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_CORRIDOR, tag);
        }

        public static Corridor createPiece(StructurePieceAccessor pieces, RandomSource random,
                                           int x, int y, int z, Direction dir, int genDepth) {
            BoundingBox box = BoundingBox.orientBox(x, y, z, -2, -1, 0, 5, 5, 14, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new Corridor(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            this.generateChildForward((StartRoom) piece, pieces, random, 2, 1);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 4, 4, 13, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 0, 3, 3, 13, CAVE_AIR, CAVE_AIR, false);

            this.generateDoor(level, random, box, this.entryDoor,        1, 1,  0);
            this.generateDoor(level, random, box, this.randomDoor(random), 1, 1, 13);

            // Wall torches — 40 % chance on each sconce position
            this.maybeGenerateBlock(level, box, random, 0.4F, 0, 2,  3,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
            this.maybeGenerateBlock(level, box, random, 0.4F, 4, 2,  3,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
            this.maybeGenerateBlock(level, box, random, 0.4F, 0, 2, 10,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
            this.maybeGenerateBlock(level, box, random, 0.4F, 4, 2, 10,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
        }
    }

    // -----------------------------------------------------------------------
    // WideCorridor — 7×6×16 with pilasters
    // -----------------------------------------------------------------------

    public static class WideCorridor extends ForgottenPiece {

        public WideCorridor(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_WIDE_CORRIDOR, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public WideCorridor(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_WIDE_CORRIDOR, tag);
        }

        public static WideCorridor createPiece(StructurePieceAccessor pieces, RandomSource random,
                                               int x, int y, int z, Direction dir, int genDepth) {
            BoundingBox box = BoundingBox.orientBox(x, y, z, -3, -1, 0, 7, 6, 16, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new WideCorridor(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            this.generateChildForward((StartRoom) piece, pieces, random, 3, 1);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 6, 5, 15, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 0, 5, 4, 15, CAVE_AIR, CAVE_AIR, false);

            // Pilasters along the walls every 4 blocks, with torches
            for (int pz : new int[]{3, 7, 11}) {
                this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), 1, 3, pz, box);
                this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, pz, box);
                this.placeTorch(level, box, 1, 3, pz, Direction.EAST);
                this.placeTorch(level, box, 5, 3, pz, Direction.WEST);
            }

            this.generateDoor(level, random, box, this.entryDoor,          2, 1,  0);
            this.generateDoor(level, random, box, this.randomDoor(random),  2, 1, 15);
        }
    }

    // -----------------------------------------------------------------------
    // TurnLeft — 9×5×9
    // -----------------------------------------------------------------------

    public static class TurnLeft extends ForgottenPiece {

        public TurnLeft(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_TURN_LEFT, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public TurnLeft(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_TURN_LEFT, tag);
        }

        public static TurnLeft createPiece(StructurePieceAccessor pieces, RandomSource random,
                                           int x, int y, int z, Direction dir, int genDepth) {
            BoundingBox box = BoundingBox.orientBox(x, y, z, -1, -1, 0, 9, 5, 9, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new TurnLeft(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            this.generateChildLeft((StartRoom) piece, pieces, random, 1, 1);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 8, 4, 8, true, random, STONE_SELECTOR);
            // L-shaped interior: the corridor enters from z=0 and exits to the left (x=0)
            this.generateBox(level, box, 1, 1, 0, 8, 3, 7, CAVE_AIR, CAVE_AIR, false);
            this.generateBox(level, box, 0, 1, 1, 7, 3, 8, CAVE_AIR, CAVE_AIR, false);

            this.generateDoor(level, random, box, this.entryDoor,          1, 1, 0);
            this.generateDoor(level, random, box, this.randomDoor(random), 0, 1, 1);
        }
    }

    // -----------------------------------------------------------------------
    // TurnRight — 9×5×9
    // -----------------------------------------------------------------------

    public static class TurnRight extends ForgottenPiece {

        public TurnRight(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_TURN_RIGHT, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public TurnRight(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_TURN_RIGHT, tag);
        }

        public static TurnRight createPiece(StructurePieceAccessor pieces, RandomSource random,
                                            int x, int y, int z, Direction dir, int genDepth) {
            BoundingBox box = BoundingBox.orientBox(x, y, z, -1, -1, 0, 9, 5, 9, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new TurnRight(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            this.generateChildRight((StartRoom) piece, pieces, random, 1, 1);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 8, 4, 8, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 0, 1, 1, 7, 3, 8, CAVE_AIR, CAVE_AIR, false);
            this.generateBox(level, box, 1, 1, 0, 8, 3, 7, CAVE_AIR, CAVE_AIR, false);

            this.generateDoor(level, random, box, this.entryDoor,          1, 1, 0);
            this.generateDoor(level, random, box, this.randomDoor(random), 8, 1, 1);
        }
    }

    // -----------------------------------------------------------------------
    // CrossroadRoom — 13×7×13, 4-way junction with central pillar cluster
    // -----------------------------------------------------------------------

    public static class CrossroadRoom extends ForgottenPiece {

        public CrossroadRoom(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_CROSSROAD, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public CrossroadRoom(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_CROSSROAD, tag);
        }

        public static CrossroadRoom createPiece(StructurePieceAccessor pieces, RandomSource random,
                                                int x, int y, int z, Direction dir, int genDepth) {
            BoundingBox box = BoundingBox.orientBox(x, y, z, -6, -1, 0, 13, 7, 13, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new CrossroadRoom(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            StartRoom start = (StartRoom) piece;
            this.generateChildForward(start, pieces, random, 5, 1);
            this.generateChildLeft(start,    pieces, random, 1, 5);
            this.generateChildRight(start,   pieces, random, 1, 5);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 12, 6, 12, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 1, 11, 5, 11, CAVE_AIR, CAVE_AIR, false);

            // 3×3 pillar cluster in the centre — hollow inside
            this.generateBox(level, box, 5, 1, 5, 7, 5, 7,
                    Blocks.STONE_BRICKS.defaultBlockState(),
                    Blocks.STONE_BRICKS.defaultBlockState(), false);
            this.generateBox(level, box, 6, 2, 6, 6, 5, 6, CAVE_AIR, CAVE_AIR, false);

            // Doors on all four faces
            this.generateDoor(level, random, box, this.entryDoor,          5, 1,  0);
            this.generateDoor(level, random, box, this.randomDoor(random),  5, 1, 12);
            this.generateDoor(level, random, box, this.randomDoor(random),  0, 1,  5);
            this.generateDoor(level, random, box, this.randomDoor(random), 12, 1,  5);

            // Torches around the central pillar
            this.placeTorch(level, box, 5, 3,  4, Direction.NORTH);
            this.placeTorch(level, box, 7, 3,  4, Direction.NORTH);
            this.placeTorch(level, box, 5, 3,  8, Direction.SOUTH);
            this.placeTorch(level, box, 7, 3,  8, Direction.SOUTH);
            this.placeTorch(level, box, 4, 3,  5, Direction.WEST);
            this.placeTorch(level, box, 4, 3,  7, Direction.WEST);
            this.placeTorch(level, box, 8, 3,  5, Direction.EAST);
            this.placeTorch(level, box, 8, 3,  7, Direction.EAST);
        }
    }

    // -----------------------------------------------------------------------
    // SmallRoom — 9×7×9, 1–3 exits, rubble floor
    // -----------------------------------------------------------------------

    public static class SmallRoom extends ForgottenPiece {

        public SmallRoom(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_SMALL_ROOM, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public SmallRoom(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_SMALL_ROOM, tag);
        }

        public static SmallRoom createPiece(StructurePieceAccessor pieces, RandomSource random,
                                            int x, int y, int z, Direction dir, int genDepth) {
            BoundingBox box = BoundingBox.orientBox(x, y, z, -4, -1, 0, 9, 7, 9, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new SmallRoom(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            StartRoom start = (StartRoom) piece;
            this.generateChildForward(start, pieces, random, 3, 1);
            if (random.nextBoolean()) this.generateChildLeft(start,  pieces, random, 1, 3);
            if (random.nextBoolean()) this.generateChildRight(start, pieces, random, 1, 3);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 8, 6, 8, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 1, 7, 5, 7, CAVE_AIR, CAVE_AIR, false);

            // Rubble: 15 % chance per floor tile of a cracked brick
            for (int fx = 1; fx <= 7; fx++) {
                for (int fz = 1; fz <= 7; fz++) {
                    this.maybeGenerateBlock(level, box, random, 0.15F, fx, 1, fz,
                            Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                }
            }

            this.generateDoor(level, random, box, this.entryDoor, 3, 1, 0);

            this.maybeGenerateBlock(level, box, random, 0.7F, 1, 3, 4,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
            this.maybeGenerateBlock(level, box, random, 0.7F, 7, 3, 4,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
        }
    }

    // -----------------------------------------------------------------------
    // LargeHall — 17×11×17, four 2×2 pillars, exits on all four faces
    // -----------------------------------------------------------------------

    public static class LargeHall extends ForgottenPiece {

        public LargeHall(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_LARGE_HALL, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public LargeHall(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_LARGE_HALL, tag);
        }

        public static LargeHall createPiece(StructurePieceAccessor pieces, RandomSource random,
                                            int x, int y, int z, Direction dir, int genDepth) {
            BoundingBox box = BoundingBox.orientBox(x, y, z, -8, -1, 0, 17, 11, 17, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new LargeHall(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            StartRoom start = (StartRoom) piece;
            this.generateChildForward(start, pieces, random, 7, 1);
            this.generateChildLeft(start,    pieces, random, 1, 7);
            this.generateChildRight(start,   pieces, random, 1, 7);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box,  0,  0,  0, 16, 10, 16, true, random, STONE_SELECTOR);
            this.generateBox(level, box,  1,  1,  1, 15,  9, 15, CAVE_AIR, CAVE_AIR, false);

            // Four 2×2 pillars — placed at corners of the central area
            for (int[] p : new int[][]{{3, 3}, {13, 3}, {3, 13}, {13, 13}}) {
                int px = p[0], pz = p[1];
                this.generateBox(level, box, px, 1, pz, px + 1, 8, pz + 1,
                        Blocks.STONE_BRICKS.defaultBlockState(),
                        Blocks.STONE_BRICKS.defaultBlockState(), false);
                // Slab caps on pillars
                this.placeBlock(level, Blocks.STONE_BRICK_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.TOP), px,     9, pz,     box);
                this.placeBlock(level, Blocks.STONE_BRICK_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.TOP), px + 1, 9, pz,     box);
                this.placeBlock(level, Blocks.STONE_BRICK_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.TOP), px,     9, pz + 1, box);
                this.placeBlock(level, Blocks.STONE_BRICK_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.TOP), px + 1, 9, pz + 1, box);
                // Torches on two faces of each pillar
                this.placeTorch(level, box, px - 1, 5, pz,     Direction.WEST);
                this.placeTorch(level, box, px + 2, 5, pz,     Direction.EAST);
                this.placeTorch(level, box, px,     5, pz - 1, Direction.NORTH);
                this.placeTorch(level, box, px,     5, pz + 2, Direction.SOUTH);
            }

            // Doors on all four faces — centred on the wall spans
            this.generateDoor(level, random, box, this.entryDoor,           7, 1,  0);
            this.generateDoor(level, random, box, this.randomDoor(random),   7, 1, 16);
            this.generateDoor(level, random, box, this.randomDoor(random),   0, 1,  7);
            this.generateDoor(level, random, box, this.randomDoor(random),  16, 1,  7);
        }
    }

    // -----------------------------------------------------------------------
    // SpawnerRoom — 9×7×9, random-mob spawner with iron-bar cage
    // -----------------------------------------------------------------------

    public static class SpawnerRoom extends ForgottenPiece {

        public SpawnerRoom(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_SPAWNER_ROOM, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public SpawnerRoom(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_SPAWNER_ROOM, tag);
        }

        public static SpawnerRoom createPiece(StructurePieceAccessor pieces, RandomSource random,
                                              int x, int y, int z, Direction dir, int genDepth) {
            BoundingBox box = BoundingBox.orientBox(x, y, z, -4, -1, 0, 9, 7, 9, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new SpawnerRoom(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            this.generateChildForward((StartRoom) piece, pieces, random, 3, 1);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 8, 6, 8, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 1, 7, 5, 7, CAVE_AIR, CAVE_AIR, false);

            this.generateDoor(level, random, box, this.entryDoor, 3, 1, 0);

            // Randomise the mob type — cave-appropriate enemies
            EntityType<?>[] mobs = {
                EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.CAVE_SPIDER, EntityType.CREEPER
            };
            this.placeSpawner(level, box, random, 4, 1, 4, mobs[random.nextInt(mobs.length)]);

            // Partial iron-bar cage surrounding the spawner at floor level
            for (int bx = 3; bx <= 5; bx++) {
                this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), bx, 1, 3, box);
                this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), bx, 1, 5, box);
            }
            this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), 3, 1, 4, box);
            this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), 5, 1, 4, box);
        }
    }

    // -----------------------------------------------------------------------
    // TreasureRoom — 9×7×9, iron-door vault, 2–3 loot chests
    // -----------------------------------------------------------------------

    public static class TreasureRoom extends ForgottenPiece {

        private boolean hasPlacedChests;

        public TreasureRoom(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_TREASURE_ROOM, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = DoorType.IRON_DOOR; // always iron
        }

        public TreasureRoom(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_TREASURE_ROOM, tag);
            this.hasPlacedChests = tag.getBoolean("C");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
            super.addAdditionalSaveData(ctx, tag);
            tag.putBoolean("C", this.hasPlacedChests);
        }

        public static TreasureRoom createPiece(StructurePieceAccessor pieces, RandomSource random,
                                               int x, int y, int z, Direction dir, int genDepth) {
            BoundingBox box = BoundingBox.orientBox(x, y, z, -4, -1, 0, 9, 7, 9, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new TreasureRoom(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            // Dead end — no exits beyond the entry door
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 8, 6, 8, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 1, 7, 5, 7, CAVE_AIR, CAVE_AIR, false);

            // Mossy floor — treasure rooms look more ancient
            this.generateBox(level, box, 1, 1, 1, 7, 1, 7,
                    Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                    Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), false);

            this.generateDoor(level, random, box, DoorType.IRON_DOOR, 3, 1, 0);

            if (!this.hasPlacedChests) {
                this.hasPlacedChests = true;
                this.createChest(level, box, random, 2, 2, 2, FORGOTTEN_LOOT);
                this.createChest(level, box, random, 6, 2, 6, FORGOTTEN_LOOT);
                if (random.nextBoolean()) {
                    this.createChest(level, box, random, 2, 2, 6, FORGOTTEN_LOOT);
                }
            }

            // Guard spawner
            this.placeSpawner(level, box, random, 4, 1, 4, EntityType.ZOMBIE);

            this.placeTorch(level, box, 1, 4, 4, Direction.EAST);
            this.placeTorch(level, box, 7, 4, 4, Direction.WEST);
        }
    }

    // -----------------------------------------------------------------------
    // PrisonBlock — 13×5×9, cells separated by iron bars with skeleton spawners
    // -----------------------------------------------------------------------

    public static class PrisonBlock extends ForgottenPiece {

        public PrisonBlock(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_PRISON_BLOCK, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public PrisonBlock(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_PRISON_BLOCK, tag);
        }

        public static PrisonBlock createPiece(StructurePieceAccessor pieces, RandomSource random,
                                              int x, int y, int z, Direction dir, int genDepth) {
            BoundingBox box = BoundingBox.orientBox(x, y, z, -6, -1, 0, 13, 5, 9, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new PrisonBlock(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            this.generateChildForward((StartRoom) piece, pieces, random, 5, 1);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 12, 4, 8, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 1, 11, 3, 7, CAVE_AIR, CAVE_AIR, false);

            // Central corridor carved through the full width
            this.generateBox(level, box, 0, 1, 4, 12, 3, 4, CAVE_AIR, CAVE_AIR, false);

            // Two rows of cells: z=1–3 (left cells) and z=5–7 (right cells),
            // separated from the corridor (z=4) by iron-bar walls.
            for (int cx = 1; cx <= 10; cx += 4) {
                for (int by = 1; by <= 3; by++) {
                    // Bar wall at z=3 (facing corridor from left side)
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx,     by, 3, box);
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx + 1, by, 3, box);
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx + 2, by, 3, box);
                    // Bar wall at z=5 (facing corridor from right side)
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx,     by, 5, box);
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx + 1, by, 5, box);
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx + 2, by, 5, box);
                }
                // Skeleton spawner in every other cell — creates ambush potential
                if (random.nextBoolean()) {
                    this.placeSpawner(level, box, random, cx + 1, 1, 1, EntityType.SKELETON);
                }
                if (random.nextBoolean()) {
                    this.placeSpawner(level, box, random, cx + 1, 1, 7, EntityType.SKELETON);
                }
            }

            this.generateDoor(level, random, box, this.entryDoor,          5, 1, 0);
            this.generateDoor(level, random, box, this.randomDoor(random),  5, 1, 8);

            // Corridor torches
            this.placeTorch(level, box,  0, 2, 4, Direction.EAST);
            this.placeTorch(level, box, 12, 2, 4, Direction.WEST);
            this.placeTorch(level, box,  6, 2, 4, Direction.EAST);
        }
    }

    // -----------------------------------------------------------------------
    // StaircaseDown — 5×13×10, descends 8 blocks to a lower level
    // -----------------------------------------------------------------------

    public static class StaircaseDown extends ForgottenPiece {

        public StaircaseDown(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_STAIRCASE_DOWN, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public StaircaseDown(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_STAIRCASE_DOWN, tag);
        }

        public static StaircaseDown createPiece(StructurePieceAccessor pieces, RandomSource random,
                                                int x, int y, int z, Direction dir, int genDepth) {
            // offsetY=-8 sinks the box 8 blocks — exit is at world y-8 (lower level)
            BoundingBox box = BoundingBox.orientBox(x, y, z, -2, -8, 0, 5, 13, 10, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new StaircaseDown(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            // Child spawns at the bottom exit: local y=1, z=9
            this.generateChildForward((StartRoom) piece, pieces, random, 2, 1);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 4, 12, 9, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 0, 3, 12, 9, CAVE_AIR, CAVE_AIR, false);

            // Entry door at the top of the staircase (local y=8, z=0)
            this.generateDoor(level, random, box, this.entryDoor, 1, 8, 0);

            // 8 descending steps: z=1..8, each dropping 1 block in y.
            // The stair tread is at (y = 8-i), solid fill below it.
            BlockState tread = Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH);
            for (int i = 0; i < 8; i++) {
                int stepY = 7 - i;
                int stepZ = 1 + i;
                // Solid backing
                this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), 1, stepY,     stepZ, box);
                this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), 2, stepY,     stepZ, box);
                this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), 3, stepY,     stepZ, box);
                // Step tread on top
                this.placeBlock(level, tread, 1, stepY + 1, stepZ, box);
                this.placeBlock(level, tread, 2, stepY + 1, stepZ, box);
                this.placeBlock(level, tread, 3, stepY + 1, stepZ, box);
            }

            // Exit door at the bottom (local y=1, z=9)
            this.generateDoor(level, random, box, this.randomDoor(random), 1, 1, 9);

            // Occasional torches on staircase walls
            this.maybeGenerateBlock(level, box, random, 0.6F, 0, 6, 3,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
            this.maybeGenerateBlock(level, box, random, 0.6F, 4, 4, 6,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
        }
    }

    // -----------------------------------------------------------------------
    // StaircaseUp — 5×13×10, ascends 8 blocks — creates surface ruin effect
    // -----------------------------------------------------------------------

    public static class StaircaseUp extends ForgottenPiece {

        public StaircaseUp(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_STAIRCASE_UP, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = this.randomDoor(random);
        }

        public StaircaseUp(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_STAIRCASE_UP, tag);
        }

        public static StaircaseUp createPiece(StructurePieceAccessor pieces, RandomSource random,
                                              int x, int y, int z, Direction dir, int genDepth) {
            // No Y offset — the box extends upward from the connection point,
            // which, after moveBelowSeaLevel() shifts the whole structure down,
            // means the top naturally breaches or approaches the surface.
            BoundingBox box = BoundingBox.orientBox(x, y, z, -2, 0, 0, 5, 13, 10, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new StaircaseUp(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            // Dead end — the top opens to surface rubble / sky
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            // Full shell first, then carve
            this.generateBox(level, box, 0, 0, 0, 4, 12, 9, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 0, 3, 12, 9, CAVE_AIR, CAVE_AIR, false);

            // Ruin the roof — randomly remove blocks from the upper 2 layers
            // so the tower looks collapsed from the surface
            for (int rx = 0; rx <= 4; rx++) {
                for (int rz = 0; rz <= 9; rz++) {
                    if (random.nextFloat() < 0.45F) this.placeBlock(level, CAVE_AIR, rx, 12, rz, box);
                    if (random.nextFloat() < 0.30F) this.placeBlock(level, CAVE_AIR, rx, 11, rz, box);
                    // Occasional cracked bricks on the upper walls
                    if (random.nextFloat() < 0.20F)
                        this.placeBlock(level, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), rx, 10, rz, box);
                }
            }

            // Entry door at the base
            this.generateDoor(level, random, box, this.entryDoor, 1, 1, 0);

            // 8 ascending steps — player climbs from z=8 (bottom) to z=1 (top)
            BlockState tread = Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH);
            for (int i = 0; i < 8; i++) {
                int stepY = 2 + i;
                int stepZ = 8 - i;
                this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), 1, stepY - 1, stepZ, box);
                this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), 2, stepY - 1, stepZ, box);
                this.placeBlock(level, Blocks.STONE_BRICKS.defaultBlockState(), 3, stepY - 1, stepZ, box);
                this.placeBlock(level, tread, 1, stepY, stepZ, box);
                this.placeBlock(level, tread, 2, stepY, stepZ, box);
                this.placeBlock(level, tread, 3, stepY, stepZ, box);
            }

            // Torches on the way up
            this.maybeGenerateBlock(level, box, random, 0.6F, 0, 3, 6,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
            this.maybeGenerateBlock(level, box, random, 0.6F, 4, 6, 4,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
        }
    }
}
