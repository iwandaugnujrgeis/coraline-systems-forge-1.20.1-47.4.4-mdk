package net.zharok01.coralinesystems.worldgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.zharok01.coralinesystems.block.LimitedLightBlock;
import net.zharok01.coralinesystems.registry.CoralineBlocks;

public class PhantomTunnelFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_DEPTH_BELOW_SURFACE = 8;
    private static final int MAX_DEPTH_BELOW_SURFACE = 24;
    private static final int BOTTOM_SAFETY_MARGIN = 5;
    private static final int TUNNEL_MIN_COVER = 2;

    private static final int MAX_BACK_OFF = 8;
    private static final int MAX_TUNNEL_REACH = 12;

    private static final int VEIN_CHANCE = 20;
    private static final int STRAIGHT_CUTOFF = 55;
    private static final int ZIGZAG_CUTOFF = 85;
    private static final int SURFACE_PINHOLE_CHANCE = 30;
    private static final int END_TORCH_CHANCE = 15;
    private static final int STUB_ODDS = 9;

    private static final int MIN_SEGMENT_LENGTH = 2;
    private static final int MAX_SEGMENT_LENGTH = 12;
    private static final int MIN_STUB_LENGTH = 2;
    private static final int MAX_STUB_LENGTH = 3;
    private static final int MIN_VEIN_CELLS = 3;
    private static final int MAX_VEIN_CELLS = 8;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public PhantomTunnelFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ());
        if (surfaceY != level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ())) {
            return false;
        }

        int y = surfaceY - random.nextInt(MIN_DEPTH_BELOW_SURFACE, MAX_DEPTH_BELOW_SURFACE + 1);
        if (y < level.getMinBuildHeight() + BOTTOM_SAFETY_MARGIN) {
            return false;
        }

        Predicate<BlockState> replaceable = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos end = new BlockPos.MutableBlockPos();

        Direction heading = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int backOff = random.nextInt(0, MAX_BACK_OFF + 1);
        int x = origin.getX() - heading.getStepX() * backOff;
        int z = origin.getZ() - heading.getStepZ() * backOff;

        if (!this.coraline$carveWalkway(level, pos.set(x, y, z), replaceable)) {
            return false;
        }
        end.set(x, y, z);

        if (random.nextInt(100) < SURFACE_PINHOLE_CHANCE) {
            int shaftTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            for (int shaftY = y + 2; shaftY < shaftTop; shaftY++) {
                if (!this.coraline$carve(level, pos.set(x, shaftY, z), 0, replaceable)) {
                    break;
                }
            }
        }

        int roll = random.nextInt(100);
        if (roll < VEIN_CHANCE) {
            this.coraline$digVein(level, random, end, replaceable);
            return true;
        }

        boolean zigzag = false;
        int stairSteps = 0;
        int length;
        if (roll < STRAIGHT_CUTOFF) {
            length = random.nextInt(8, 25);
        } else if (roll < ZIGZAG_CUTOFF) {
            zigzag = true;
            length = random.nextInt(12, 29);
        } else {
            stairSteps = random.nextInt(6, 15);
            length = stairSteps + random.nextInt(0, 9);
        }

        int stepsUntilTurn = random.nextInt(MIN_SEGMENT_LENGTH, MAX_SEGMENT_LENGTH + 1);
        for (int step = 0; step < length; step++) {
            if (zigzag) {
                if (stepsUntilTurn == 0) {
                    heading = random.nextBoolean() ? heading.getClockWise() : heading.getCounterClockWise();
                    stepsUntilTurn = random.nextInt(MIN_SEGMENT_LENGTH, MAX_SEGMENT_LENGTH + 1);
                }
                stepsUntilTurn--;
            }

            if (step < stairSteps) {
                y--;
            }
            x += heading.getStepX();
            z += heading.getStepZ();

            if (Math.abs(x - origin.getX()) > MAX_TUNNEL_REACH || Math.abs(z - origin.getZ()) > MAX_TUNNEL_REACH) {
                break;
            }
            if (!this.coraline$carveWalkway(level, pos.set(x, y, z), replaceable)) {
                break;
            }
            end.set(x, y, z);

            if (step > 2 && random.nextInt(STUB_ODDS) == 0) {
                this.coraline$digStub(level, random, end, heading, replaceable);
            }
        }

        if (random.nextInt(100) < END_TORCH_CHANCE) {
            BlockPos floor = end.below();
            if (level.isEmptyBlock(end) && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
                BlockState burntOutTorch = CoralineBlocks.TORCH.get().defaultBlockState().setValue(LimitedLightBlock.BURN, 15);
                level.setBlock(end, burntOutTorch, Block.UPDATE_CLIENTS);
            }
        }

        return true;
    }

    private void coraline$digVein(WorldGenLevel level, RandomSource random, BlockPos start, Predicate<BlockState> replaceable) {
        Direction direction = Direction.getRandom(random);
        BlockPos.MutableBlockPos cursor = start.mutable();
        int cells = random.nextInt(MIN_VEIN_CELLS, MAX_VEIN_CELLS + 1);
        for (int i = 0; i < cells; i++) {
            if (!this.coraline$carve(level, cursor, TUNNEL_MIN_COVER, replaceable)) {
                return;
            }
            if (random.nextInt(3) == 0) {
                direction = Direction.getRandom(random);
            }
            cursor.move(direction);
        }
    }

    private void coraline$digStub(WorldGenLevel level, RandomSource random, BlockPos from, Direction heading, Predicate<BlockState> replaceable) {
        Direction side = random.nextBoolean() ? heading.getClockWise() : heading.getCounterClockWise();
        BlockPos.MutableBlockPos stub = from.mutable();
        int length = random.nextInt(MIN_STUB_LENGTH, MAX_STUB_LENGTH + 1);
        for (int i = 0; i < length; i++) {
            stub.move(side);
            if (!this.coraline$carveWalkway(level, stub, replaceable)) {
                return;
            }
        }
    }

    private boolean coraline$carveWalkway(WorldGenLevel level, BlockPos pos, Predicate<BlockState> replaceable) {
        return this.coraline$carve(level, pos, TUNNEL_MIN_COVER, replaceable)
                && this.coraline$carve(level, pos.above(), TUNNEL_MIN_COVER, replaceable);
    }

    private boolean coraline$carve(WorldGenLevel level, BlockPos pos, int minCover, Predicate<BlockState> replaceable) {
        if (pos.getY() < level.getMinBuildHeight() + BOTTOM_SAFETY_MARGIN) {
            return false;
        }
        if (pos.getY() + minCover > level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ()) - 1) {
            return false;
        }

        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            neighbor.setWithOffset(pos, direction);
            if (!level.getFluidState(neighbor).isEmpty()) {
                return false;
            }
        }

        this.safeSetBlock(level, pos, AIR, replaceable);
        return true;
    }
}