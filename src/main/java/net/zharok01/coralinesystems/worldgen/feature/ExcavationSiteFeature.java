package net.zharok01.coralinesystems.worldgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ExcavationSiteFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_HALF_EXTENT = 2;
    private static final int MAX_HALF_EXTENT = 3;
    private static final int MIN_DEPTH = 5;
    private static final int MAX_DEPTH = 10;
    private static final int BOTTOM_SAFETY_MARGIN = 5;
    private static final int FLUID_CHECK_RING = 1;

    private static final int MAX_HORIZONTAL_REACH = 15;
    private static final int DRIFT_MIN_COVER = 2;
    private static final int PIT_NIBBLE_MIN_COVER = 1;

    private static final int MIN_TUNNEL_LENGTH = 3;
    private static final int MAX_TUNNEL_LENGTH = 6;
    private static final int MIN_CHAMBER_HALF = 1;
    private static final int MAX_CHAMBER_HALF = 3;
    private static final int MIN_CHAMBER_HEIGHT = 3;
    private static final int MAX_CHAMBER_HEIGHT = 4;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public ExcavationSiteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int halfExtent = random.nextInt(MIN_HALF_EXTENT, MAX_HALF_EXTENT + 1);
        int depth = random.nextInt(MIN_DEPTH, MAX_DEPTH + 1);
        int floorY = Math.max(origin.getY() - 1 - depth, level.getMinBuildHeight() + BOTTOM_SAFETY_MARGIN);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int checkExtent = halfExtent + FLUID_CHECK_RING;

        for (int dx = -checkExtent; dx <= checkExtent; dx++) {
            for (int dz = -checkExtent; dz <= checkExtent; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int surfaceTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

                if (surfaceTop != level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z)) {
                    return false;
                }

                for (int y = surfaceTop - 1; y >= floorY; y--) {
                    pos.set(x, y, z);
                    if (!level.getBlockState(pos).getFluidState().isEmpty()) {
                        return false;
                    }
                }
            }
        }

        Predicate<BlockState> replaceable = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);

        for (int dx = -(halfExtent + 1); dx <= halfExtent + 1; dx++) {
            for (int dz = -(halfExtent + 1); dz <= halfExtent + 1; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int columnTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                int ring = Math.max(Math.abs(dx), Math.abs(dz));
                int columnFloor;

                if (ring <= halfExtent) {
                    boolean corner = Math.abs(dx) == halfExtent && Math.abs(dz) == halfExtent;
                    if (corner && random.nextInt(5) < 2) {
                        continue;
                    }
                    columnFloor = ring == halfExtent ? floorY + random.nextInt(depth / 2 + 1) : floorY;
                } else {
                    if (random.nextInt(10) >= 3) {
                        continue;
                    }
                    columnFloor = columnTop - random.nextInt(1, 3);
                }

                for (int y = columnTop - 1; y >= columnFloor; y--) {
                    pos.set(x, y, z);
                    this.safeSetBlock(level, pos, AIR, replaceable);
                }
            }
        }

        int pitNibbles = random.nextInt(3, 7);
        for (int i = 0; i < pitNibbles; i++) {
            BlockPos start = new BlockPos(
                    origin.getX() + random.nextInt(-halfExtent, halfExtent + 1),
                    random.nextInt(floorY, floorY + depth - 1),
                    origin.getZ() + random.nextInt(-halfExtent, halfExtent + 1));
            this.coraline$nibble(level, random, start, PIT_NIBBLE_MIN_COVER, replaceable);
        }

        int roll = random.nextInt(100);
        int chainLength = roll < 25 ? 0 : roll < 70 ? 1 : random.nextInt(2, 4);

        Direction heading = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction side = heading.getClockWise();
        int lateral = random.nextInt(-1, 2);
        int cursorX = origin.getX() + heading.getStepX() * halfExtent + side.getStepX() * lateral;
        int cursorZ = origin.getZ() + heading.getStepZ() * halfExtent + side.getStepZ() * lateral;
        int cursorY = floorY + (random.nextInt(4) == 0 ? 1 : 0);

        for (int link = 0; link < chainLength; link++) {
            int length = random.nextInt(MIN_TUNNEL_LENGTH, MAX_TUNNEL_LENGTH + 1);
            int rise = random.nextInt(4) == 0 ? (random.nextBoolean() ? 1 : -1) : 0;
            int chamberHalfX = random.nextInt(MIN_CHAMBER_HALF, MAX_CHAMBER_HALF + 1);
            int chamberHalfZ = random.nextInt(MIN_CHAMBER_HALF, MAX_CHAMBER_HALF + 1);
            int chamberHeight = random.nextInt(MIN_CHAMBER_HEIGHT, MAX_CHAMBER_HEIGHT + 1);
            int halfAlong = heading.getAxis() == Direction.Axis.X ? chamberHalfX : chamberHalfZ;

            side = heading.getClockWise();
            lateral = random.nextInt(-1, 2);
            int centerX = cursorX + heading.getStepX() * (length + halfAlong) + side.getStepX() * lateral;
            int centerZ = cursorZ + heading.getStepZ() * (length + halfAlong) + side.getStepZ() * lateral;
            int chamberFloorY = cursorY + rise;

            boolean outOfReach = Math.abs(centerX - origin.getX()) + chamberHalfX > MAX_HORIZONTAL_REACH
                    || Math.abs(centerZ - origin.getZ()) + chamberHalfZ > MAX_HORIZONTAL_REACH;
            int surfaceAtCenter = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, centerX, centerZ);
            if (outOfReach || chamberFloorY + chamberHeight + DRIFT_MIN_COVER > surfaceAtCenter) {
                break;
            }

            for (int k = 0; k <= length; k++) {
                int tunnelX = cursorX + heading.getStepX() * k;
                int tunnelZ = cursorZ + heading.getStepZ() * k;
                int tunnelY = k > length / 2 ? cursorY + rise : cursorY;
                int bottom = tunnelY;
                int top = tunnelY + 1;
                if (rise != 0 && k == length / 2 + 1) {
                    bottom = Math.min(cursorY, tunnelY);
                    top = Math.max(cursorY, tunnelY) + 1;
                }
                for (int y = bottom; y <= top; y++) {
                    this.coraline$carve(level, pos.set(tunnelX, y, tunnelZ), DRIFT_MIN_COVER, replaceable);
                }
            }

            for (int dx = -chamberHalfX; dx <= chamberHalfX; dx++) {
                for (int dz = -chamberHalfZ; dz <= chamberHalfZ; dz++) {
                    for (int dy = 0; dy < chamberHeight; dy++) {
                        int boundaries = (Math.abs(dx) == chamberHalfX ? 1 : 0)
                                + (Math.abs(dz) == chamberHalfZ ? 1 : 0)
                                + (dy == chamberHeight - 1 ? 1 : 0);
                        if (boundaries >= 2 && random.nextInt(2) == 0) {
                            continue;
                        }
                        this.coraline$carve(level, pos.set(centerX + dx, chamberFloorY + dy, centerZ + dz), DRIFT_MIN_COVER, replaceable);
                    }
                }
            }

            int chamberNibbles = random.nextInt(2, 5);
            for (int i = 0; i < chamberNibbles; i++) {
                BlockPos start = new BlockPos(
                        centerX + random.nextInt(-chamberHalfX, chamberHalfX + 1),
                        chamberFloorY + random.nextInt(chamberHeight),
                        centerZ + random.nextInt(-chamberHalfZ, chamberHalfZ + 1));
                this.coraline$nibble(level, random, start, DRIFT_MIN_COVER, replaceable);
            }

            heading = random.nextInt(4) == 0 ? heading : (random.nextBoolean() ? heading.getClockWise() : heading.getCounterClockWise());
            halfAlong = heading.getAxis() == Direction.Axis.X ? chamberHalfX : chamberHalfZ;
            side = heading.getClockWise();
            lateral = random.nextInt(-1, 2);
            cursorX = centerX + heading.getStepX() * halfAlong + side.getStepX() * lateral;
            cursorZ = centerZ + heading.getStepZ() * halfAlong + side.getStepZ() * lateral;
            cursorY = chamberFloorY;
        }

        return true;
    }

    private void coraline$carve(WorldGenLevel level, BlockPos pos, int minCover, Predicate<BlockState> replaceable) {
        if (pos.getY() < level.getMinBuildHeight() + BOTTOM_SAFETY_MARGIN) {
            return;
        }
        if (pos.getY() + minCover > level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ()) - 1) {
            return;
        }

        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            neighbor.setWithOffset(pos, direction);
            if (!level.getFluidState(neighbor).isEmpty()) {
                return;
            }
        }

        this.safeSetBlock(level, pos, AIR, replaceable);
    }

    private void coraline$nibble(WorldGenLevel level, RandomSource random, BlockPos start, int minCover, Predicate<BlockState> replaceable) {
        if (!level.isEmptyBlock(start)) {
            return;
        }

        Direction direction = Direction.getRandom(random);
        BlockPos.MutableBlockPos pos = start.mutable();
        for (int step = 0; step < 4; step++) {
            pos.move(direction);
            if (!level.isEmptyBlock(pos)) {
                int pocketDepth = random.nextInt(1, 3);
                for (int i = 0; i < pocketDepth; i++) {
                    this.coraline$carve(level, pos, minCover, replaceable);
                    pos.move(direction);
                }
                return;
            }
        }
    }
}