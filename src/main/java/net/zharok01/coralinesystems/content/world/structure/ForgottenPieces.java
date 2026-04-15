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
import net.zharok01.coralinesystems.registry.IsotopicEntities;

import javax.annotation.Nullable;
import java.util.List;

/**
 * CONNECTION RULES (mirrors vanilla StrongholdPieces exactly):
 *
 *   ENTRY face (z = 0):  draw this.entryDoor — the piece owns its own doorway.
 *   EXIT  face (z = max): always DoorType.OPENING — just carves 3 blocks of air.
 *   SIDE  exits (x = 0 or x = max): generateBox(CAVE_AIR) ONLY — no door call.
 *
 * The child piece connected to any exit draws its OWN entryDoor at its z = 0.
 * This guarantees exactly one door at every junction, eliminating double-doors
 * and the "buttons replacing the door half" corruption.
 */
public class ForgottenPieces {

    private static final int MAX_DEPTH         = 80;
    private static final int HORIZONTAL_RADIUS = 160;
    private static final int MIN_Y             = 10;  // restored — 56 is above ground

    // FIX: removed stale .json suffix — ResourceLocation paths never include it
    private static final ResourceLocation FORGOTTEN_LOOT =
            new ResourceLocation(CoralineSystems.MOD_ID, "chests/forgotten_dungeon");

    // -----------------------------------------------------------------------
    // Piece weight table
    // -----------------------------------------------------------------------

    private static final PieceWeight[] PIECE_WEIGHTS = {
            new PieceWeight(Corridor.class,        35,  0),
            new PieceWeight(WideCorridor.class,    15,  0),
            new PieceWeight(TurnLeft.class,        20,  0),
            new PieceWeight(TurnRight.class,       20,  0),
            new PieceWeight(CrossroadRoom.class,   10, 12),
            new PieceWeight(SmallRoom.class,       15, 10),
            new PieceWeight(LargeHall.class,        8,  6),
            new PieceWeight(SpawnerRoom.class,     20, 15),
            new PieceWeight(TreasureRoom.class,     8,  5),
            new PieceWeight(PrisonBlock.class,     10,  6),
            new PieceWeight(StaircaseDown.class,   12,  8)
            /*new PieceWeight(StaircaseUp.class,      5,  3) {
                @Override public boolean doPlace(int genDepth) {
                    return super.doPlace(genDepth) && genDepth > 6;
                }
            },
            */
    };

    private static List<PieceWeight> currentPieces;
    private static int totalWeight;

    public static void resetPieces() {
        currentPieces = Lists.newArrayList();
        for (PieceWeight w : PIECE_WEIGHTS) {
            w.placeCount = 0;
            currentPieces.add(w);
        }
    }

    private static boolean updatePieceWeights() {
        boolean hasMore = false;
        totalWeight = 0;
        for (PieceWeight w : currentPieces) {
            if (w.maxPlaceCount > 0 && w.placeCount < w.maxPlaceCount) hasMore = true;
            totalWeight += w.weight;
        }
        return hasMore;
    }

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
        if (cls == Corridor.class)      return Corridor.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == WideCorridor.class)  return WideCorridor.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == TurnLeft.class)      return TurnLeft.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == TurnRight.class)     return TurnRight.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == CrossroadRoom.class) return CrossroadRoom.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == SmallRoom.class)     return SmallRoom.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == LargeHall.class)     return LargeHall.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == SpawnerRoom.class)   return SpawnerRoom.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == TreasureRoom.class)  return TreasureRoom.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == PrisonBlock.class)   return PrisonBlock.createPiece(pieces, random, x, y, z, dir, genDepth);
        if (cls == StaircaseDown.class) return StaircaseDown.createPiece(pieces, random, x, y, z, dir, genDepth);
        //if (cls == StaircaseUp.class)   return StaircaseUp.createPiece(pieces, random, x, y, z, dir, genDepth);
        return null;
    }

    // -----------------------------------------------------------------------
    // Block selector
    // -----------------------------------------------------------------------

    static final ForgottenBlockSelector STONE_SELECTOR = new ForgottenBlockSelector();

    public static class ForgottenBlockSelector extends StructurePiece.BlockSelector {
        @Override
        public void next(RandomSource random, int x, int y, int z, boolean isWall) {
            float f = random.nextFloat();
            if      (f < 0.20F) this.next = Blocks.DEEPSLATE.defaultBlockState();
            //else if (f < 0.35F) this.next = Blocks.DEEPSLATE.defaultBlockState();
            else if (f < 0.42F) this.next = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
            else                this.next = Blocks.DEEPSLATE_TILES.defaultBlockState();
        }
    }

    public enum DoorType { OPENING, WOOD_DOOR, IRON_DOOR, GRATE }

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

        boolean doPlace(int genDepth) { return maxPlaceCount == 0 || placeCount < maxPlaceCount; }
        boolean isValid()             { return maxPlaceCount == 0 || placeCount < maxPlaceCount; }
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
         * Draws a doorway on the Z-face at local (x, y, z).
         * ONLY call this for the ENTRY face (z = 0). All exit faces must use
         * DoorType.OPENING, which is equivalent to a plain generateBox(CAVE_AIR).
         */
        protected void generateDoor(WorldGenLevel level, RandomSource random, BoundingBox box,
                                    DoorType type, int x, int y, int z) {
            switch (type) {
                case OPENING ->
                        this.generateBox(level, box, x, y, z, x + 2, y + 2, z, CAVE_AIR, CAVE_AIR, false);

                case WOOD_DOOR -> {
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x,     y,     z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x,     y + 1, z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x,     y + 2, z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x + 1, y + 2, z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x + 2, y + 2, z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x + 2, y + 1, z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x + 2, y,     z, box);
                    this.placeBlock(level, Blocks.OAK_DOOR.defaultBlockState(), x + 1, y, z, box);
                    this.placeBlock(level, Blocks.OAK_DOOR.defaultBlockState()
                            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, box);
                }

                case IRON_DOOR -> {
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x,     y,     z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x,     y + 1, z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x,     y + 2, z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x + 1, y + 2, z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x + 2, y + 2, z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x + 2, y + 1, z, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), x + 2, y,     z, box);
                    this.placeBlock(level, Blocks.IRON_DOOR.defaultBlockState(), x + 1, y, z, box);
                    this.placeBlock(level, Blocks.IRON_DOOR.defaultBlockState()
                            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, box);
                    // Button inside the room (z + 1) — can always be opened from within.
                    // Button at z - 1 goes outside the piece's bounding box, so the chunk
                    // box allows it to land in the adjacent corridor's air space, enabling
                    // opening from the other side — exactly as vanilla handles this.
                    this.placeBlock(level, Blocks.STONE_BUTTON.defaultBlockState()
                            .setValue(ButtonBlock.FACING, Direction.NORTH), x + 2, y + 1, z + 1, box);
                    this.placeBlock(level, Blocks.STONE_BUTTON.defaultBlockState()
                            .setValue(ButtonBlock.FACING, Direction.SOUTH), x + 2, y + 1, z - 1, box);
                }

                case GRATE -> {
                    this.placeBlock(level, CAVE_AIR, x + 1, y,     z, box);
                    this.placeBlock(level, CAVE_AIR, x + 1, y + 1, z, box);
                    for (int by = y; by <= y + 2; by++) {
                        this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), x,     by, z, box);
                        this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), x + 2, by, z, box);
                    }
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), x + 1, y + 2, z, box);
                }
            }
        }

        protected void placeTorch(WorldGenLevel level, BoundingBox box,
                                  int x, int y, int z, Direction facing) {
            this.placeBlock(level,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing),
                    x, y, z, box);
        }

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

        /** Returns the correct stair-facing for this piece's orientation (see stairFacing() in prior version). */
        protected Direction stairFacing() {
            return (this.getOrientation() == Direction.SOUTH) ? Direction.SOUTH : Direction.NORTH;
        }

        @Nullable
        protected StructurePiece generateChildForward(StartRoom start, StructurePieceAccessor pieces,
                                                      RandomSource random, int offsetX, int offsetY) {
            Direction dir = this.getOrientation();
            if (dir == null) return null;
            return switch (dir) {
                case NORTH -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() + offsetX, this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() - 1, dir, this.getGenDepth());
                case SOUTH -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() + offsetX, this.boundingBox.minY() + offsetY,
                        this.boundingBox.maxZ() + 1, dir, this.getGenDepth());
                case WEST  -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() - 1, this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() + offsetX, dir, this.getGenDepth());
                case EAST  -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.maxX() + 1, this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() + offsetX, dir, this.getGenDepth());
                default    -> null;
            };
        }

        @Nullable
        protected StructurePiece generateChildLeft(StartRoom start, StructurePieceAccessor pieces,
                                                   RandomSource random, int offsetY, int offsetX) {
            Direction dir = this.getOrientation();
            if (dir == null) return null;
            return switch (dir) {
                case NORTH, SOUTH -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() - 1, this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() + offsetX, Direction.WEST, this.getGenDepth());
                case WEST, EAST   -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() + offsetX, this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth());
                default           -> null;
            };
        }

        @Nullable
        protected StructurePiece generateChildRight(StartRoom start, StructurePieceAccessor pieces,
                                                    RandomSource random, int offsetY, int offsetX) {
            Direction dir = this.getOrientation();
            if (dir == null) return null;
            return switch (dir) {
                case NORTH, SOUTH -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.maxX() + 1, this.boundingBox.minY() + offsetY,
                        this.boundingBox.minZ() + offsetX, Direction.EAST, this.getGenDepth());
                case WEST, EAST   -> ForgottenPieces.generateAndAddPiece(start, pieces, random,
                        this.boundingBox.minX() + offsetX, this.boundingBox.minY() + offsetY,
                        this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth());
                default           -> null;
            };
        }
    }

    // =======================================================================
    // PIECE IMPLEMENTATIONS
    // =======================================================================

    // -----------------------------------------------------------------------
    // StartRoom — 13×9×13
    // No entry (it is the origin). All four exits are plain air carves.
    // Children draw their own entry doors.
    // -----------------------------------------------------------------------
    public static class StartRoom extends ForgottenPiece {

        public final List<StructurePiece> pendingChildren = Lists.newArrayList();

        public StartRoom(RandomSource random, int x, int z) {
            super(CoralineStructurePieceTypes.FORGOTTEN_START_ROOM, 0,
                    new BoundingBox(x, 64, z, x + 12, 72, z + 12));
            this.setOrientation(Direction.SOUTH);
        }

        public StartRoom(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_START_ROOM, tag);
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            this.generateChildForward(this, pieces, random, 5, 1);
            this.generateChildLeft(this,    pieces, random, 1, 5);
            this.generateChildRight(this,   pieces, random, 1, 5);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 12, 8, 12, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 1, 11, 7, 11, CAVE_AIR, CAVE_AIR, false);

            // Corner pillars
            for (int[] p : new int[][]{{2, 2}, {10, 2}, {2, 10}, {10, 10}}) {
                this.generateBox(level, box, p[0], 1, p[1], p[0], 6, p[1],
                        Blocks.DEEPSLATE_TILES.defaultBlockState(),
                        Blocks.DEEPSLATE_TILES.defaultBlockState(), false);
            }

            // FIX: all four exits are plain air carves — no door frames here.
            // The children connected to these openings draw their own entry doors.
            // Forward exit (z = 12): x:5-7, y:1-3
            this.generateBox(level, box,  5, 1, 12,  7, 3, 12, CAVE_AIR, CAVE_AIR, false);
            // Entry / south face (z = 0): x:5-7, y:1-3
            this.generateBox(level, box,  5, 1,  0,  7, 3,  0, CAVE_AIR, CAVE_AIR, false);
            // Left exit  (x = 0):  z:5-7, y:1-3
            this.generateBox(level, box,  0, 1,  5,  0, 3,  7, CAVE_AIR, CAVE_AIR, false);
            // Right exit (x = 12): z:5-7, y:1-3
            this.generateBox(level, box, 12, 1,  5, 12, 3,  7, CAVE_AIR, CAVE_AIR, false);

            this.placeTorch(level, box,  3, 4,  2, Direction.EAST);
            this.placeTorch(level, box,  9, 4,  2, Direction.WEST);
            this.placeTorch(level, box,  3, 4, 10, Direction.EAST);
            this.placeTorch(level, box,  9, 4, 10, Direction.WEST);
        }
    }

    // -----------------------------------------------------------------------
    // Corridor — 5×5×14
    // Entry door at z = 0.  Exit at z = 13: OPENING only (child draws its door).
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

            // Entry door — this piece owns its own entry doorway
            this.generateDoor(level, random, box, this.entryDoor, 1, 1, 0);
            // FIX: exit is always OPENING — child draws the door on its side
            this.generateDoor(level, random, box, DoorType.OPENING, 1, 1, 13);

            /*
            this.maybeGenerateBlock(level, box, random, 0.4F, 0, 2,  3,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
            this.maybeGenerateBlock(level, box, random, 0.4F, 4, 2,  3,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
            this.maybeGenerateBlock(level, box, random, 0.4F, 0, 2, 10,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
            this.maybeGenerateBlock(level, box, random, 0.4F, 4, 2, 10,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
            */
        }
    }

    // -----------------------------------------------------------------------
    // WideCorridor — 7×6×16
    // Entry at z = 0.  Exit at z = 15: OPENING.
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

            for (int pz : new int[]{3, 7, 11}) {
                this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), 1, 3, pz, box);
                this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), 5, 3, pz, box);
                this.placeTorch(level, box, 1, 3, pz, Direction.EAST);
                this.placeTorch(level, box, 5, 3, pz, Direction.WEST);
            }

            this.generateDoor(level, random, box, this.entryDoor,   2, 1,  0);
            // FIX: exit is OPENING
            this.generateDoor(level, random, box, DoorType.OPENING, 2, 1, 15);
        }
    }

    // -----------------------------------------------------------------------
    // TurnLeft — 9×5×9
    //
    // Entry at z = 0.
    // Side exit at x = 0: the interior air carve (0,1,1 → 7,3,8) already opens
    // the left wall.  No door call needed — the child draws its own entry door.
    // FIX: removed the bogus generateDoor(random, 0, 1, 1) that was placing a
    // Z-face door frame on the wrong axis.
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
            // Forward arm of the L (entry corridor, z direction)
            this.generateBox(level, box, 1, 1, 0, 8, 3, 7, CAVE_AIR, CAVE_AIR, false);
            // Left arm of the L (exit corridor, x direction) — also opens x = 0 face
            this.generateBox(level, box, 0, 1, 1, 7, 3, 8, CAVE_AIR, CAVE_AIR, false);

            // Entry door only — the x = 0 opening is already carved above
            this.generateDoor(level, random, box, this.entryDoor, 1, 1, 0);
            // FIX: NO door call for the side exit. Child draws its own door.
        }
    }

    // -----------------------------------------------------------------------
    // TurnRight — 9×5×9
    //
    // Entry at z = 0.
    // Side exit at x = 8: the interior air carve (1,1,0 → 8,3,7) opens x = 8.
    // FIX: removed the bogus generateDoor(random, 8, 1, 1) call.
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
            // Right arm of the L (exit corridor, x direction) — opens x = 8 face
            this.generateBox(level, box, 0, 1, 1, 7, 3, 8, CAVE_AIR, CAVE_AIR, false);
            // Forward arm of the L (entry corridor, z direction)
            this.generateBox(level, box, 1, 1, 0, 8, 3, 7, CAVE_AIR, CAVE_AIR, false);

            // Entry door only
            this.generateDoor(level, random, box, this.entryDoor, 1, 1, 0);
            // FIX: NO door call for the side exit. Child draws its own door.
        }
    }

    // -----------------------------------------------------------------------
    // CrossroadRoom — 13×7×13
    //
    // Entry at z = 0 (x:5-7).
    // Forward exit at z = 12: OPENING.
    // Left  exit at x = 0  (z:5-7): plain generateBox(CAVE_AIR) — no door frame.
    // Right exit at x = 12 (z:5-7): plain generateBox(CAVE_AIR) — no door frame.
    //
    // FIX: the old generateDoor(random, 0, 1, 5) and generateDoor(random, 12, 1, 5)
    // calls were trying to draw a Z-face door on the X face — completely wrong axis.
    // They produced stone-brick frames that extended outside the bounding box and
    // blocked the corridors connecting to those exits.
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

            this.generateBox(level, box, 5, 1, 5, 7, 5, 7,
                    Blocks.DEEPSLATE_TILES.defaultBlockState(),
                    Blocks.DEEPSLATE_TILES.defaultBlockState(), false);
            this.generateBox(level, box, 6, 2, 6, 6, 5, 6, CAVE_AIR, CAVE_AIR, false);

            // Entry door at z = 0
            this.generateDoor(level, random, box, this.entryDoor, 5, 1, 0);
            // FIX: forward exit — OPENING, not a random door
            this.generateDoor(level, random, box, DoorType.OPENING, 5, 1, 12);
            // FIX: side exits — plain air carves on the X faces (3 wide in Z, 3 tall)
            // Left  (x = 0):  z:5–7
            this.generateBox(level, box,  0, 1, 5,  0, 3, 7, CAVE_AIR, CAVE_AIR, false);
            // Right (x = 12): z:5–7
            this.generateBox(level, box, 12, 1, 5, 12, 3, 7, CAVE_AIR, CAVE_AIR, false);

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
    // SmallRoom — 9×7×9
    //
    // Entry at z = 0 (x:3-5).
    // Forward exit at z = 8 (x:3-5): OPENING.
    // Optional side exits: tracked with leftChild / rightChild booleans so
    // postProcess knows which X-face openings to carve. Serialised to NBT.
    //
    // FIX: addChildren no longer generates exits without recording them;
    // postProcess was previously never carving the forward or side openings
    // at all (the interior box stops at z=7/x=1, not the wall faces).
    // -----------------------------------------------------------------------
    public static class SmallRoom extends ForgottenPiece {

        private boolean leftChild;
        private boolean rightChild;

        public SmallRoom(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_SMALL_ROOM, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor  = this.randomDoor(random);
            this.leftChild  = random.nextBoolean();
            this.rightChild = random.nextBoolean();
        }

        public SmallRoom(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_SMALL_ROOM, tag);
            this.leftChild  = tag.getBoolean("L");
            this.rightChild = tag.getBoolean("R");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
            super.addAdditionalSaveData(ctx, tag);
            tag.putBoolean("L", this.leftChild);
            tag.putBoolean("R", this.rightChild);
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
            if (this.leftChild)  this.generateChildLeft(start,  pieces, random, 1, 3);
            if (this.rightChild) this.generateChildRight(start, pieces, random, 1, 3);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 8, 6, 8, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 1, 7, 5, 7, CAVE_AIR, CAVE_AIR, false);

            // Rubble floor
            /*
            for (int fx = 1; fx <= 7; fx++)
                for (int fz = 1; fz <= 7; fz++)
                    this.maybeGenerateBlock(level, box, random, 0.15F, fx, 0, fz,
                            Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            */

            // Entry door at z = 0
            this.generateDoor(level, random, box, this.entryDoor, 3, 1, 0);
            // FIX: forward exit at z = 8 — OPENING (was missing entirely before)
            this.generateDoor(level, random, box, DoorType.OPENING, 3, 1, 8);
            // FIX: optional side exits — plain air carves at x-faces (only if child was generated)
            if (this.leftChild)
                this.generateBox(level, box, 0, 1, 3, 0, 3, 5, CAVE_AIR, CAVE_AIR, false);
            if (this.rightChild)
                this.generateBox(level, box, 8, 1, 3, 8, 3, 5, CAVE_AIR, CAVE_AIR, false);

            this.maybeGenerateBlock(level, box, random, 0.7F, 1, 3, 4,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
            this.maybeGenerateBlock(level, box, random, 0.7F, 7, 3, 4,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
        }
    }

    // -----------------------------------------------------------------------
    // LargeHall — 17×11×17
    //
    // Entry at z = 0 (x:7-9).
    // Forward exit at z = 16 (x:7-9): OPENING.
    // Left  exit at x = 0  (z:7-9): plain air carve.
    // Right exit at x = 16 (z:7-9): plain air carve.
    //
    // FIX: forward and side exits were previously random doors; side exits
    // were also drawn on the wrong axis.
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

            for (int[] p : new int[][]{{3, 3}, {13, 3}, {3, 13}, {13, 13}}) {
                int px = p[0], pz = p[1];
                this.generateBox(level, box, px, 1, pz, px + 1, 8, pz + 1,
                        Blocks.DEEPSLATE_TILES.defaultBlockState(),
                        Blocks.DEEPSLATE_TILES.defaultBlockState(), false);
                this.placeBlock(level, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.TOP), px,     9, pz,     box);
                this.placeBlock(level, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.TOP), px + 1, 9, pz,     box);
                this.placeBlock(level, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.TOP), px,     9, pz + 1, box);
                this.placeBlock(level, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.TOP), px + 1, 9, pz + 1, box);
                this.placeTorch(level, box, px - 1, 5, pz,     Direction.WEST);
                this.placeTorch(level, box, px + 2, 5, pz,     Direction.EAST);
                this.placeTorch(level, box, px,     5, pz - 1, Direction.NORTH);
                this.placeTorch(level, box, px,     5, pz + 2, Direction.SOUTH);
            }

            // Entry door
            this.generateDoor(level, random, box, this.entryDoor, 7, 1, 0);
            // FIX: forward exit — OPENING only
            this.generateDoor(level, random, box, DoorType.OPENING, 7, 1, 16);
            // FIX: side exits — plain air carves (x-faces), 3 wide in Z, 3 tall
            this.generateBox(level, box,  0, 1, 7,  0, 3, 9, CAVE_AIR, CAVE_AIR, false);
            this.generateBox(level, box, 16, 1, 7, 16, 3, 9, CAVE_AIR, CAVE_AIR, false);
        }
    }

    // -----------------------------------------------------------------------
    // SpawnerRoom — 9×7×9
    //
    // Entry at z = 0 (x:3-5).
    // Forward exit at z = 8 (x:3-5): OPENING — was missing entirely before.
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

            // Entry door
            this.generateDoor(level, random, box, this.entryDoor, 3, 1, 0);
            // FIX: forward exit was missing — add OPENING at z = 8
            this.generateDoor(level, random, box, DoorType.OPENING, 3, 1, 8);

            EntityType<?>[] mobs = {
                    EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
                    IsotopicEntities.HELPER.get(), EntityType.CREEPER
            };
            this.placeSpawner(level, box, random, 4, 1, 4, mobs[random.nextInt(mobs.length)]);

            /*
            this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), 3, 1, 4, box);
            this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), 5, 1, 4, box);
            */
        }
    }

    // -----------------------------------------------------------------------
    // TreasureRoom — 9×7×9  (dead end — entry only, no exit)
    // -----------------------------------------------------------------------
    public static class TreasureRoom extends ForgottenPiece {

        private boolean hasPlacedChests;

        public TreasureRoom(int genDepth, RandomSource random, BoundingBox box, Direction dir) {
            super(CoralineStructurePieceTypes.FORGOTTEN_TREASURE_ROOM, genDepth, box);
            this.setOrientation(dir);
            this.entryDoor = DoorType.IRON_DOOR;
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
            // Dead end — no exits
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 8, 6, 8, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 1, 7, 5, 7, CAVE_AIR, CAVE_AIR, false);

            // Mossy floor at y = 1
            this.generateBox(level, box, 1, 0, 1, 7, 0, 7,
                    Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
                    Blocks.MOSSY_COBBLESTONE.defaultBlockState(), false);

            this.generateDoor(level, random, box, DoorType.IRON_DOOR, 3, 1, 0);

            if (!this.hasPlacedChests) {
                this.hasPlacedChests = true;
                this.createChest(level, box, random, 2, 1, 2, FORGOTTEN_LOOT);
                this.createChest(level, box, random, 6, 1, 6, FORGOTTEN_LOOT);
                if (random.nextBoolean())
                    this.createChest(level, box, random, 2, 1, 6, FORGOTTEN_LOOT);
            }

            // FIX: spawner at y = 2 — sits on top of the decorative floor, not inside it
            //this.placeSpawner(level, box, random, 4, 1, 4, EntityType.ZOMBIE);

            //TODO: TEST! A randomized Spawner:
            EntityType<?>[] mobs = {
                    EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
                    IsotopicEntities.HELPER.get(), EntityType.CREEPER
            };
            this.placeSpawner(level, box, random, 4, 1, 4, mobs[random.nextInt(mobs.length)]);

            this.placeTorch(level, box, 1, 4, 4, Direction.EAST);
            this.placeTorch(level, box, 7, 4, 4, Direction.WEST);
        }
    }

    // -----------------------------------------------------------------------
    // PrisonBlock — 13×5×9
    // Entry at z = 0.  Forward exit at z = 8: OPENING.
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
            this.generateBox(level, box, 0, 1, 4, 12, 3, 4, CAVE_AIR, CAVE_AIR, false);

            for (int cx = 1; cx <= 10; cx += 4) {
                /*
                for (int by = 1; by <= 3; by++) {
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx,     by, 3, box);
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx + 1, by, 3, box);
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx + 2, by, 3, box);
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx,     by, 5, box);
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx + 1, by, 5, box);
                    this.placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), cx + 2, by, 5, box);
                }
                */
                if (random.nextBoolean())
                    this.placeSpawner(level, box, random, cx + 1, 1, 1, EntityType.ZOMBIE);
                if (random.nextBoolean())
                    this.placeSpawner(level, box, random, cx + 1, 1, 7, EntityType.SKELETON);
            }

            this.generateDoor(level, random, box, this.entryDoor,   5, 1, 0);
            // FIX: exit is OPENING
            this.generateDoor(level, random, box, DoorType.OPENING,  5, 1, 8);

            this.placeTorch(level, box,  0, 2, 4, Direction.EAST);
            this.placeTorch(level, box, 12, 2, 4, Direction.WEST);
            this.placeTorch(level, box,  6, 2, 4, Direction.EAST);
        }
    }

    // -----------------------------------------------------------------------
    // StaircaseDown — 5×11×8, descends 7 blocks to a lower level
    // -----------------------------------------------------------------------
    public static class StaircaseDown extends ForgottenPiece {

        public StaircaseDown(int genDepth, RandomSource random, BoundingBox box, Direction orientation) {
            super(CoralineStructurePieceTypes.FORGOTTEN_STAIRCASE_DOWN, genDepth, box);
            this.setOrientation(orientation);
            this.entryDoor = this.randomDoor(random);
        }

        public StaircaseDown(CompoundTag tag) {
            super(CoralineStructurePieceTypes.FORGOTTEN_STAIRCASE_DOWN, tag);
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            this.generateChildForward((StartRoom)piece, pieces, random, 1, 1);
        }

        public static StaircaseDown createPiece(
                StructurePieceAccessor pieces, RandomSource random, int x, int y, int z, Direction orientation, int genDepth
        ) {
            BoundingBox boundingBox = BoundingBox.orientBox(x, y, z, -1, -7, 0, 5, 11, 8, orientation);
            return isOkBox(boundingBox) && pieces.findCollisionPiece(boundingBox) == null
                    ? new StaircaseDown(genDepth, random, boundingBox, orientation)
                    : null;
        }

        @Override
        public void postProcess(
                WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos
        ) {
            // 1. Generate the outer solid shell
            this.generateBox(level, box, 0, 0, 0, 4, 10, 7, true, random, STONE_SELECTOR);

            // 2. MISSING LINE ADDED: Hollow out the inside with air!
            this.generateBox(level, box, 1, 1, 0, 3, 9, 7, CAVE_AIR, CAVE_AIR, false);

            // 3. Generate the top entry door and bottom exit opening
            this.generateDoor(level, random, box, this.entryDoor, 1, 7, 0);
            this.generateDoor(level, random, box, DoorType.OPENING, 1, 1, 7);

            BlockState blockState = Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);

            // 4. Place the 6 stairs and the solid blocks beneath them
            for (int i = 0; i < 6; i++) {
                this.placeBlock(level, blockState, 1, 6 - i, 1 + i, box);
                this.placeBlock(level, blockState, 2, 6 - i, 1 + i, box);
                this.placeBlock(level, blockState, 3, 6 - i, 1 + i, box);
                if (i < 5) {
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), 1, 5 - i, 1 + i, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2, 5 - i, 1 + i, box);
                    this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), 3, 5 - i, 1 + i, box);
                }
            }

            // Optional: Add some lighting so it isn't pitch black
            this.maybeGenerateBlock(level, box, random, 0.6F, 0, 6, 3,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
            this.maybeGenerateBlock(level, box, random, 0.6F, 4, 4, 5,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
        }
    }

    // -----------------------------------------------------------------------
    // StaircaseUp — 5×13×10  (dead end at top — breaches surface as a ruin)
    // Entry at z = 0 (bottom).  No exit at top.
    // -----------------------------------------------------------------------
    /*
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
            BoundingBox box = BoundingBox.orientBox(x, y, z, -2, 0, 0, 5, 13, 10, dir);
            return isOkBox(box) && pieces.findCollisionPiece(box) == null
                    ? new StaircaseUp(genDepth, random, box, dir) : null;
        }

        @Override
        public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
            // Dead end — top opens to surface / sky
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen,
                                RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            this.generateBox(level, box, 0, 0, 0, 4, 12, 9, true, random, STONE_SELECTOR);
            this.generateBox(level, box, 1, 1, 0, 3, 12, 9, CAVE_AIR, CAVE_AIR, false);

            // Ruined roof
            for (int rx = 0; rx <= 4; rx++) {
                for (int rz = 0; rz <= 9; rz++) {
                    if (random.nextFloat() < 0.45F) this.placeBlock(level, CAVE_AIR, rx, 12, rz, box);
                    if (random.nextFloat() < 0.30F) this.placeBlock(level, CAVE_AIR, rx, 11, rz, box);
                    if (random.nextFloat() < 0.20F)
                        this.placeBlock(level, Blocks.DEEPSLATE.defaultBlockState(), rx, 10, rz, box);
                }
            }

            this.generateDoor(level, random, box, this.entryDoor, 1, 1, 0);

            BlockState tread = Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, this.stairFacing());
            for (int i = 0; i < 8; i++) {
                int stepY = 2 + i;
                int stepZ = 8 - i;
                this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), 1, stepY - 1, stepZ, box);
                this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2, stepY - 1, stepZ, box);
                this.placeBlock(level, Blocks.DEEPSLATE_TILES.defaultBlockState(), 3, stepY - 1, stepZ, box);
                this.placeBlock(level, tread, 1, stepY, stepZ, box);
                this.placeBlock(level, tread, 2, stepY, stepZ, box);
                this.placeBlock(level, tread, 3, stepY, stepZ, box);
            }

            this.maybeGenerateBlock(level, box, random, 0.6F, 0, 3, 6,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
            this.maybeGenerateBlock(level, box, random, 0.6F, 4, 6, 4,
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
        }
    }
    */
}