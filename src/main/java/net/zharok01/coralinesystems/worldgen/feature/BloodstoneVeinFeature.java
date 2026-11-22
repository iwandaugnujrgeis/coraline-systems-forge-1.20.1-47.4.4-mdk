package net.zharok01.coralinesystems.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.block.BloodstoneBlock;
import net.zharok01.coralinesystems.registry.CoralineBlocks;

public class BloodstoneVeinFeature extends Feature<NoneFeatureConfiguration> {
    private static final int TRUNK_LENGTH_MIN = 10;
    private static final int TRUNK_LENGTH_MAX = 18;
    private static final int MAX_BRANCH_DEPTH = 3;
    private static final float BRANCH_CHANCE = 0.12F;
    private static final double DIRECTION_JITTER = 0.35;
    private static final int MAX_REACH = 8;

    public BloodstoneVeinFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int trunkLength = TRUNK_LENGTH_MIN + random.nextInt(TRUNK_LENGTH_MAX - TRUNK_LENGTH_MIN + 1);
        return coraline$growVein(level, random, origin, Vec3.atCenterOf(origin), coraline$randomDirection(random), trunkLength, 0) > 0;
    }

    private static int coraline$growVein(WorldGenLevel level, RandomSource random, BlockPos origin, Vec3 start, Vec3 startDirection, int length, int depth) {
        BlockState bloodstone = CoralineBlocks.BLOODSTONE.get().defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        Vec3 position = start;
        Vec3 direction = startDirection;
        int placed = 0;
        for (int step = 0; step < length; step++) {
            cursor.set(position.x, position.y, position.z);
            if (level.isOutsideBuildHeight(cursor)
                    || Math.abs(cursor.getX() - origin.getX()) > MAX_REACH
                    || Math.abs(cursor.getZ() - origin.getZ()) > MAX_REACH) {
                break;
            }
            if (level.getBlockState(cursor).is(BlockTags.BASE_STONE_NETHER)) {
                level.setBlock(cursor, bloodstone, Block.UPDATE_CLIENTS);
                BloodstoneBlock.coraline$scheduleAligned(level, cursor.immutable(), bloodstone.getBlock(), BloodstoneBlock.SETTLED_SCAN_TICKS);
                placed++;
            }
            if (depth < MAX_BRANCH_DEPTH && random.nextFloat() < BRANCH_CHANCE) {
                placed += coraline$growVein(level, random, origin, position, coraline$randomDirection(random), (length - step) / 2, depth + 1);
            }
            Vec3 jitter = coraline$randomDirection(random).scale(DIRECTION_JITTER);
            direction = direction.add(jitter).normalize();
            position = position.add(direction);
        }
        return placed;
    }

    private static Vec3 coraline$randomDirection(RandomSource random) {
        return new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).normalize();
    }
}
