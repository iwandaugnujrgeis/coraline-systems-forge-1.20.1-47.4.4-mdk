package net.zharok01.coralinesystems.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import net.zharok01.coralinesystems.registry.CoralineStructures;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RuinsPieces {

    private static final ResourceLocation KUBEJS_BRICKS = new ResourceLocation("kubejs", "smooth_stone_bricks");
    private static final ResourceLocation RUINS_LOOT = new ResourceLocation("gamma", "chests/skylands_loot");

    private static final int MAX_WALL_HEIGHT = 8;
    private static final int NO_WINDOW = -1;

    public static void addPieces(BlockPos origin, Rotation rotation,
                                 StructurePiecesBuilder builder,
                                 WorldgenRandom rand) {
        BoundingBox bounds = BoundingBox.fromCorners(
                origin.offset(-20, -64 - origin.getY(), -20),
                origin.offset(20, 320 - origin.getY(), 20)
        );
        builder.addPiece(new Piece(bounds));
    }

    public static class Piece extends StructurePiece {

        protected Piece(BoundingBox boundingBox) {
            super((StructurePieceType) CoralineStructures.RUINS.getPieceType().get(), 0, boundingBox);
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super((StructurePieceType) CoralineStructures.RUINS.getPieceType().get(), tag);
        }

        @Override
        protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context, @NotNull CompoundTag tag) {
        }

        @Override
        public void postProcess(@NotNull WorldGenLevel level, @NotNull StructureManager structureManager,
                                @NotNull ChunkGenerator chunkGen, @NotNull RandomSource rand,
                                @NotNull BoundingBox chunkBounds, @NotNull ChunkPos chunkPos, @NotNull BlockPos pos) {

            BlockPos center = boundingBox.getCenter();

            RandomSource pieceRand = RandomSource.create(center.asLong() ^ 0x57021E57L);

            Map<BlockPos, Integer> blueprint = new LinkedHashMap<>();
            Map<BlockPos, Integer> windowGap = new HashMap<>();

            int style = pieceRand.nextInt(15);
            boolean canHaveChest = false;

            switch (style) {

                case 0 -> {
                    int len = pieceRand.nextIntBetweenInclusive(6, 12);
                    int wallH = pieceRand.nextIntBetweenInclusive(3, MAX_WALL_HEIGHT);
                    for (int dx = 0; dx <= len; dx++) {
                        blueprint.put(new BlockPos(dx - len / 2, 0, 0), wallH);
                    }
                    if (wallH >= 3) {
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand,
                                -len / 2, len / 2, 0, 0, wallH, true);
                    }
                }

                case 1 -> {
                    int armA = pieceRand.nextIntBetweenInclusive(5, 9);
                    int armB = pieceRand.nextIntBetweenInclusive(5, 9);
                    int h = pieceRand.nextIntBetweenInclusive(3, MAX_WALL_HEIGHT);
                    for (int dx = 0; dx <= armA; dx++) blueprint.put(new BlockPos(dx, 0, 0), h);
                    for (int dz = 1; dz <= armB; dz++) blueprint.put(new BlockPos(0, 0, dz), h);
                    if (h >= 3) {
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand, 1, armA, 0, 0, h, false);
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand, 0, 0, 1, armB, h, false);
                    }
                }

                case 2 -> {
                    int len = pieceRand.nextIntBetweenInclusive(6, 11);
                    int gap = pieceRand.nextIntBetweenInclusive(3, 6);
                    int h = pieceRand.nextIntBetweenInclusive(3, MAX_WALL_HEIGHT);
                    for (int dx = 0; dx <= len; dx++) {
                        blueprint.put(new BlockPos(dx - len / 2, 0, 0), h);
                        blueprint.put(new BlockPos(dx - len / 2, 0, gap), h);
                    }
                    if (h >= 3) {
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand,
                                -len / 2, len / 2, 0, 0, h, true);
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand,
                                -len / 2, len / 2, gap, gap, h, true);
                    }
                }

                case 3 -> {
                    int w = pieceRand.nextIntBetweenInclusive(5, 10);
                    int d = pieceRand.nextIntBetweenInclusive(8, 14);
                    int h = pieceRand.nextIntBetweenInclusive(3, MAX_WALL_HEIGHT);
                    coraline$traceRect(blueprint, -w / 2, -d / 2, w, d, h, true, false, true, true);
                    if (h >= 3) {
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand,
                                -w / 2, w / 2, -d / 2, -d / 2, h, false);
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand,
                                -w / 2, -w / 2, -d / 2, d / 2, h, false);
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand,
                                w / 2, w / 2, -d / 2, d / 2, h, false);
                    }
                    canHaveChest = true;
                }

                case 4 -> {
                    int h = pieceRand.nextIntBetweenInclusive(3, MAX_WALL_HEIGHT);
                    int armA1 = pieceRand.nextIntBetweenInclusive(5, 9);
                    int armA2 = pieceRand.nextIntBetweenInclusive(5, 9);
                    for (int dx = 0; dx <= armA1; dx++) blueprint.put(new BlockPos(dx, 0, 0), h);
                    for (int dz = 1; dz <= armA2; dz++) blueprint.put(new BlockPos(0, 0, dz), h);
                    int offX = pieceRand.nextIntBetweenInclusive(3, 6);
                    int offZ = pieceRand.nextIntBetweenInclusive(3, 6);
                    int armB1 = pieceRand.nextIntBetweenInclusive(4, 8);
                    int armB2 = pieceRand.nextIntBetweenInclusive(4, 8);
                    for (int dx = 0; dx <= armB1; dx++) blueprint.putIfAbsent(new BlockPos(offX + dx, 0, offZ), h);
                    for (int dz = 0; dz < armB2; dz++) blueprint.putIfAbsent(new BlockPos(offX, 0, offZ + dz), h);
                    if (h >= 3) {
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand, 1, armA1, 0, 0, h, false);
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand, 0, 0, 1, armA2, h, false);
                    }
                    canHaveChest = true;
                }

                case 5 -> {
                    coraline$traceZigzagWall(blueprint, pieceRand);
                }

                case 6 -> {
                    int w = pieceRand.nextIntBetweenInclusive(5, 10);
                    int d = pieceRand.nextIntBetweenInclusive(6, 11);
                    int hBase = pieceRand.nextIntBetweenInclusive(3, MAX_WALL_HEIGHT);
                    int hWest = Math.max(1, hBase - pieceRand.nextIntBetweenInclusive(0, 2));
                    coraline$traceRect(blueprint, -w / 2, -d / 2, w, d, hBase, true, false, false, false);
                    for (int dz = 0; dz <= d; dz++) {
                        blueprint.put(new BlockPos(-w / 2, 0, -d / 2 + dz), Math.max(1, hWest - dz / 3));
                    }
                    for (int dz = 0; dz <= d; dz++) {
                        blueprint.put(new BlockPos(-w / 2 + w, 0, -d / 2 + dz), Math.max(1, hBase - dz / 3));
                    }
                    if (hBase >= 3) {
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand,
                                -w / 2, w / 2, -d / 2, -d / 2, hBase, false);
                    }
                    canHaveChest = true;
                }

                case 7 -> {
                    int spanX = pieceRand.nextIntBetweenInclusive(6, 12);
                    int spanZ = pieceRand.nextIntBetweenInclusive(6, 12);
                    int stubH = pieceRand.nextIntBetweenInclusive(2, 5);
                    int[] cornersX = {-spanX / 2, spanX / 2 - 1, -spanX / 2, spanX / 2 - 1};
                    int[] cornersZ = {-spanZ / 2, -spanZ / 2, spanZ / 2 - 1, spanZ / 2 - 1};
                    for (int i = 0; i < 4; i++) {
                        for (int ox = 0; ox <= 1; ox++) {
                            for (int oz = 0; oz <= 1; oz++) {
                                blueprint.put(new BlockPos(cornersX[i] + ox, 0, cornersZ[i] + oz), stubH);
                            }
                        }
                    }
                    if (pieceRand.nextFloat() < 0.40f) {
                        int bridgeH = Math.max(1, stubH - 1);
                        for (int dx = cornersX[0] + 2; dx < cornersX[1]; dx++) {
                            blueprint.putIfAbsent(new BlockPos(dx, 0, cornersZ[0]), bridgeH);
                        }
                    }
                }

                case 8 -> {
                    int mainH = pieceRand.nextIntBetweenInclusive(6, 18);
                    for (int ox = 0; ox <= 1; ox++) {
                        for (int oz = 0; oz <= 1; oz++) {
                            blueprint.put(new BlockPos(ox, 0, oz), mainH);
                        }
                    }
                    if (pieceRand.nextFloat() < 0.60f) {
                        int offX = pieceRand.nextIntBetweenInclusive(4, 8) * (pieceRand.nextBoolean() ? 1 : -1);
                        int offZ = pieceRand.nextIntBetweenInclusive(0, 3) * (pieceRand.nextBoolean() ? 1 : -1);
                        int secondH = pieceRand.nextIntBetweenInclusive(4, mainH);
                        for (int ox = 0; ox <= 1; ox++) {
                            for (int oz = 0; oz <= 1; oz++) {
                                blueprint.putIfAbsent(new BlockPos(offX + ox, 0, offZ + oz), secondH);
                            }
                        }
                    }
                }

                case 9 -> {
                    int len = pieceRand.nextIntBetweenInclusive(7, 13);
                    int hHigh = pieceRand.nextIntBetweenInclusive(4, MAX_WALL_HEIGHT);
                    int hLow = Math.max(1, hHigh - pieceRand.nextIntBetweenInclusive(2, 3));
                    for (int dx = 0; dx <= len; dx++) {
                        int h = hHigh - (int) Math.round((float) (hHigh - hLow) * dx / len);
                        blueprint.put(new BlockPos(dx - len / 2, 0, 0), Math.max(1, h));
                    }
                    int thickEnd = pieceRand.nextIntBetweenInclusive(1, 3);
                    for (int dx = 0; dx <= thickEnd; dx++) {
                        blueprint.putIfAbsent(new BlockPos(dx - len / 2, 0, 1), hHigh);
                    }
                }

                case 10 -> {
                    int armH = pieceRand.nextIntBetweenInclusive(3, MAX_WALL_HEIGHT);
                    int armLenX = pieceRand.nextIntBetweenInclusive(4, 9);
                    int armLenZ = pieceRand.nextIntBetweenInclusive(4, 9);
                    for (int dx = -armLenX; dx <= armLenX; dx++) blueprint.put(new BlockPos(dx, 0, 0), armH);
                    for (int dz = -armLenZ; dz <= armLenZ; dz++) blueprint.putIfAbsent(new BlockPos(0, 0, dz), armH);
                    if (armH >= 3) {
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand,
                                -armLenX, armLenX, 0, 0, armH, true);
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand,
                                0, 0, -armLenZ, armLenZ, armH, true);
                    }
                    canHaveChest = true;
                }

                case 11 -> {
                    int spokeCount = pieceRand.nextIntBetweenInclusive(3, 4);
                    int hubH = pieceRand.nextIntBetweenInclusive(3, MAX_WALL_HEIGHT);
                    for (int ox = 0; ox <= 1; ox++) {
                        for (int oz = 0; oz <= 1; oz++) {
                            blueprint.put(new BlockPos(ox, 0, oz), hubH);
                        }
                    }
                    int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
                    List<int[]> chosen = new ArrayList<>(List.of(dirs));
                    while (chosen.size() > spokeCount) {
                        chosen.remove(pieceRand.nextInt(chosen.size()));
                    }
                    for (int[] d : chosen) {
                        int spokeLen = pieceRand.nextIntBetweenInclusive(3, 7);
                        int spokeH = Math.max(1, hubH - pieceRand.nextIntBetweenInclusive(0, 2));
                        for (int step = 1; step <= spokeLen; step++) {
                            int sx = d[0] == 0 ? 0 : (d[0] > 0 ? 2 + step - 1 : -(step));
                            int sz = d[1] == 0 ? 0 : (d[1] > 0 ? 2 + step - 1 : -(step));
                            blueprint.putIfAbsent(new BlockPos(sx, 0, sz), spokeH);
                        }
                    }
                }

                case 12 -> {
                    int stumpH = pieceRand.nextIntBetweenInclusive(4, 8);
                    int missingCorner = pieceRand.nextInt(4);
                    int mcx = (missingCorner == 1 || missingCorner == 2) ? 1 : -1;
                    int mcz = (missingCorner == 2 || missingCorner == 3) ? 1 : -1;
                    for (int ox = -1; ox <= 1; ox++) {
                        for (int oz = -1; oz <= 1; oz++) {
                            boolean inMissing = (Integer.signum(ox) == mcx || ox == 0)
                                    && (Integer.signum(oz) == mcz || oz == 0)
                                    && !(ox == 0 && oz == 0);
                            if (!inMissing) {
                                blueprint.put(new BlockPos(ox, 0, oz), stumpH);
                            }
                        }
                    }
                    if (pieceRand.nextFloat() < 0.50f) {
                        int stubLen = pieceRand.nextIntBetweenInclusive(2, 5);
                        int stubH = Math.max(1, stumpH - pieceRand.nextIntBetweenInclusive(1, 3));
                        int extDx = -mcx;
                        int extDz = 0;
                        for (int step = 2; step <= stubLen + 1; step++) {
                            blueprint.putIfAbsent(new BlockPos(extDx * step, 0, extDz), stubH);
                        }
                    }
                    canHaveChest = true;
                }

                case 13 -> {
                    int radius = pieceRand.nextIntBetweenInclusive(4, 8);
                    int hMid = pieceRand.nextIntBetweenInclusive(3, MAX_WALL_HEIGHT);
                    double sweep = Math.PI * (0.6 + pieceRand.nextFloat() * 0.4);
                    int steps = radius * 4;
                    for (int i = 0; i <= steps; i++) {
                        double angle = -sweep / 2.0 + sweep * i / steps;
                        int bx = (int) Math.round(radius * Math.sin(angle));
                        int bz = (int) Math.round(-radius * Math.cos(angle));
                        double t = (double) i / steps;
                        double envelope = 1.0 - Math.abs(2.0 * t - 1.0);
                        int h = Math.max(1, (int) Math.round(hMid * (0.5 + 0.5 * envelope)));
                        blueprint.putIfAbsent(new BlockPos(bx, 0, bz), h);
                    }
                    if (hMid >= 3) {
                        coraline$addWindowsToWall(blueprint, windowGap, pieceRand,
                                -radius, radius, -radius, radius, hMid, true);
                    }
                }

                case 14 -> {
                    int gateH = pieceRand.nextIntBetweenInclusive(4, 10);
                    int gateW = pieceRand.nextIntBetweenInclusive(3, 6);
                    int stubDepth = pieceRand.nextIntBetweenInclusive(1, 2);
                    for (int ox = 0; ox <= stubDepth; ox++) {
                        for (int oz = 0; oz <= stubDepth; oz++) {
                            blueprint.put(new BlockPos(-gateW / 2 - ox, 0, oz), gateH);
                            blueprint.put(new BlockPos(gateW / 2 + ox, 0, oz), gateH);
                        }
                    }
                    if (pieceRand.nextFloat() < 0.55f) {
                        int gap2 = pieceRand.nextIntBetweenInclusive(6, 10);
                        int gateH2 = Math.max(2, gateH - pieceRand.nextIntBetweenInclusive(1, 3));
                        for (int ox = 0; ox <= stubDepth; ox++) {
                            for (int oz = 0; oz <= stubDepth; oz++) {
                                blueprint.putIfAbsent(new BlockPos(-gateW / 2 - ox, 0, gap2 + oz), gateH2);
                                blueprint.putIfAbsent(new BlockPos(gateW / 2 + ox, 0, gap2 + oz), gateH2);
                            }
                        }
                    }
                    canHaveChest = true;
                }
            }

            for (Map.Entry<BlockPos, Integer> entry : blueprint.entrySet()) {
                BlockPos localPos = entry.getKey();
                int nominalHeight = entry.getValue();

                BlockPos worldXZ = center.offset(localPos.getX(), 0, localPos.getZ());
                boolean columnInThisChunk = chunkBounds.isInside(worldXZ.atY(64));

                BlockPos surface = columnInThisChunk
                        ? coraline$surfaceAt(level, worldXZ)
                        : worldXZ.atY(64);

                boolean validSurface = columnInThisChunk
                        && surface.getY() > level.getMinBuildHeight();

                float trimFraction = pieceRand.nextFloat();
                int trim = (int) (trimFraction * trimFraction * nominalHeight);
                int effectiveHeight = Math.max(1, nominalHeight - trim);

                int winDy = windowGap.getOrDefault(localPos, NO_WINDOW);

                for (int dy = 0; dy < effectiveHeight; dy++) {
                    if (winDy != NO_WINDOW && winDy + 1 < effectiveHeight
                            && (dy == winDy || dy == winDy + 1)) {
                        coraline$ruinState(pieceRand);
                        continue;
                    }

                    BlockState wallState = coraline$ruinState(pieceRand);
                    if (!validSurface) continue;
                    BlockPos target = surface.above(dy);
                    if (!chunkBounds.isInside(target) || !boundingBox.isInside(target)) continue;
                    coraline$placeBlock(level, target, wallState);
                }
            }

            coraline$scatterGroundDebris(level, pieceRand, chunkBounds, blueprint.keySet(), center);

            if (canHaveChest) {
                coraline$tryPlaceBuriedChest(level, pieceRand, chunkBounds, center, blueprint.keySet());
            }
        }

        private void coraline$addWindowsToWall(Map<BlockPos, Integer> blueprint,
                                               Map<BlockPos, Integer> windowGap,
                                               RandomSource rand,
                                               int minX, int maxX, int minZ, int maxZ,
                                               int wallH, boolean allowTwo) {
            List<BlockPos> candidates = new ArrayList<>();
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean isEndpoint = (x == minX && z == minZ) || (x == maxX && z == maxZ);
                    if (!isEndpoint) {
                        BlockPos key = new BlockPos(x, 0, z);
                        if (blueprint.containsKey(key) && !windowGap.containsKey(key)) {
                            candidates.add(key);
                        }
                    }
                }
            }
            if (candidates.isEmpty()) return;

            int gapDy = Math.max(1, wallH / 2 - 1);

            BlockPos first = candidates.get(rand.nextInt(candidates.size()));
            windowGap.put(first, gapDy);

            if (!allowTwo || candidates.size() < 4) return;
            if (rand.nextFloat() > 0.45f) return;

            for (int attempt = 0; attempt < 10; attempt++) {
                BlockPos second = candidates.get(rand.nextInt(candidates.size()));
                if (second.equals(first)) continue;
                int dist = Math.abs(second.getX() - first.getX()) + Math.abs(second.getZ() - first.getZ());
                if (dist >= 3) {
                    windowGap.put(second, gapDy);
                    break;
                }
            }
        }
        
        private void coraline$traceRect(Map<BlockPos, Integer> map,
                                        int startX, int startZ, int width, int depth, int height,
                                        boolean n, boolean s, boolean e, boolean w) {
            for (int dx = 0; dx <= width; dx++) {
                for (int dz = 0; dz <= depth; dz++) {
                    boolean onNorth = (dz == 0);
                    boolean onSouth = (dz == depth);
                    boolean onWest = (dx == 0);
                    boolean onEast = (dx == width);
                    if ((onNorth && n) || (onSouth && s) || (onWest && w) || (onEast && e)) {
                        map.put(new BlockPos(startX + dx, 0, startZ + dz), height);
                    }
                }
            }
        }

        private void coraline$traceZigzagWall(Map<BlockPos, Integer> map, RandomSource rand) {
            int[] dirX = {1, 0, -1, 0};
            int[] dirZ = {0, 1, 0, -1};
            int dir = rand.nextInt(4);
            int cx = 0;
            int cz = 0;
            int segments = rand.nextIntBetweenInclusive(3, 5);
            for (int seg = 0; seg < segments; seg++) {
                int segLen = rand.nextIntBetweenInclusive(3, 7);
                int segHeight = rand.nextIntBetweenInclusive(2, MAX_WALL_HEIGHT);
                for (int step = 0; step < segLen; step++) {
                    map.put(new BlockPos(cx, 0, cz), segHeight);
                    if (rand.nextFloat() < 0.30f) {
                        int perpX = cx + dirZ[dir];
                        int perpZ = cz - dirX[dir];
                        map.putIfAbsent(new BlockPos(perpX, 0, perpZ), segHeight);
                    }
                    cx += dirX[dir];
                    cz += dirZ[dir];
                }
                dir = (dir + (rand.nextBoolean() ? 1 : 3)) & 3;
            }
        }
        
        private void coraline$scatterGroundDebris(WorldGenLevel level, RandomSource rand,
                                                  BoundingBox chunkBounds,
                                                  Set<BlockPos> anchors, BlockPos center) {
            for (BlockPos localPos : anchors) {
                if (rand.nextFloat() > 0.35f) continue;

                int patchOffX = (rand.nextInt(7) - 3);
                int patchOffZ = (rand.nextInt(7) - 3);
                BlockPos patchWorld = center.offset(localPos.getX() + patchOffX, 0, localPos.getZ() + patchOffZ);

                int blobRadius = rand.nextIntBetweenInclusive(2, 4);
                float fillChance = 0.90f + rand.nextFloat() * 0.10f;

                for (int dx = -blobRadius; dx <= blobRadius; dx++) {
                    for (int dz = -blobRadius; dz <= blobRadius; dz++) {
                        if (dx * dx + dz * dz > blobRadius * blobRadius) continue;
                        if (rand.nextFloat() > fillChance) continue;

                        BlockPos ground = coraline$surfaceAt(level, patchWorld.offset(dx, 0, dz)).below();
                        if (!chunkBounds.isInside(ground) || !boundingBox.isInside(ground)) continue;

                        BlockState current = level.getBlockState(ground);
                        if (current.is(BlockTags.DIRT) || current.is(Blocks.GRASS_BLOCK)) {
                            level.setBlock(ground, Blocks.COARSE_DIRT.defaultBlockState(), 2);
                        } else if (current.is(Blocks.SAND) || current.is(Blocks.GRAVEL) || current.is(Blocks.STONE)) {
                            level.setBlock(ground, Blocks.GRAVEL.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
        
        private void coraline$tryPlaceBuriedChest(WorldGenLevel level, RandomSource rand,
                                                  BoundingBox chunkBounds, BlockPos center,
                                                  Set<BlockPos> blueprintKeys) {
            if (rand.nextFloat() > 0.60f) return;

            List<BlockPos> candidateXZ = new ArrayList<>(blueprintKeys);

            for (int i = candidateXZ.size() - 1; i > 0; i--) {
                int j = rand.nextInt(i + 1);
                BlockPos tmp = candidateXZ.get(i);
                candidateXZ.set(i, candidateXZ.get(j));
                candidateXZ.set(j, tmp);
            }

            for (BlockPos local : candidateXZ) {
                BlockPos worldXZ = center.offset(local.getX(), 0, local.getZ());
                if (!chunkBounds.isInside(worldXZ.atY(64))) continue;

                int startY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        worldXZ.getX(), worldXZ.getZ());

                int stopY = Math.max(level.getMinBuildHeight() + 5, startY - 12);

                for (int y = startY - 1; y >= stopY; y--) {
                    BlockPos candidate = worldXZ.atY(y);
                    if (!chunkBounds.isInside(candidate) || !boundingBox.isInside(candidate)) continue;

                    BlockState currentState = level.getBlockState(candidate);
                    if (!currentState.isSolid() || coraline$isRuinBlock(currentState)) continue;

                    if (!level.getBlockState(candidate.above()).isSolid()) continue;

                    BlockState floor = level.getBlockState(candidate.below());
                    if (!floor.isSolid() || coraline$isRuinBlock(floor)) continue;

                    boolean enclosed = true;
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockState neighbour = level.getBlockState(candidate.relative(dir));
                        if (!neighbour.isSolid() || coraline$isRuinBlock(neighbour)) {
                            enclosed = false;
                            break;
                        }
                    }
                    if (!enclosed) continue;

                    Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
                    level.setBlock(candidate, Blocks.CHEST.defaultBlockState()
                            .setValue(ChestBlock.FACING, facing), 2);
                    net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity.setLootTable(
                            level, rand, candidate, RUINS_LOOT
                    );
                    return;
                }
            }
        }

        private static BlockPos coraline$surfaceAt(WorldGenLevel level, BlockPos pos) {
            return level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, pos);
        }

        private static void coraline$placeBlock(WorldGenLevel level, BlockPos pos, BlockState state) {
            level.setBlock(pos, state, 2);
        }

        private static boolean coraline$isRuinBlock(BlockState state) {
            if (state.is(Blocks.SMOOTH_STONE)) return true;
            Block kubejsBricks = ForgeRegistries.BLOCKS.getValue(KUBEJS_BRICKS);
            return kubejsBricks != null && kubejsBricks != Blocks.AIR && state.is(kubejsBricks);
        }

        private static BlockState coraline$ruinState(RandomSource rand) {
            if (rand.nextFloat() < 0.55f) {
                return Blocks.SMOOTH_STONE.defaultBlockState();
            }
            Block kubejsBricks = ForgeRegistries.BLOCKS.getValue(KUBEJS_BRICKS);
            if (kubejsBricks != null && kubejsBricks != Blocks.AIR) {
                return kubejsBricks.defaultBlockState();
            }
            return Blocks.SMOOTH_STONE.defaultBlockState();
        }
    }
}