package net.zharok01.coralinesystems.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.zharok01.coralinesystems.registry.CoralineBlocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class CementHardeningFeature extends Feature<NoneFeatureConfiguration> {

    private static final int SCAN_RADIUS = 16;
    private static final int MAX_SPREAD_DISTANCE = 4;

    public CementHardeningFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block cementBlock = CoralineBlocks.CEMENT.get();
        BlockState concreteState = CoralineBlocks.CONCRETE.get().defaultBlockState();

        boolean placedAny = false;

        record SpreadNode(BlockPos pos, int distance) {}
        Queue<SpreadNode> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (level.getBlockState(pos).is(cementBlock) && isTouchingWater(level, pos)) {
                        queue.add(new SpreadNode(pos, 0));
                        visited.add(pos);
                    }
                }
            }
        }

        while (!queue.isEmpty()) {
            SpreadNode node = queue.poll();
            BlockPos currentPos = node.pos();
            int currentDist = node.distance();

            level.setBlock(currentPos, concreteState, 3);
            placedAny = true;

            if (currentDist >= MAX_SPREAD_DISTANCE) continue;

            for (Direction face : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(face);

                if (visited.contains(neighborPos)) continue;
                if (!level.getBlockState(neighborPos).is(cementBlock)) continue;

                float spreadChance = 1.0f - ((currentDist + 1) * 0.2f);

                if (random.nextFloat() < spreadChance) {
                    visited.add(neighborPos);
                    queue.add(new SpreadNode(neighborPos, currentDist + 1));
                }
            }
        }

        return placedAny;
    }

    private boolean isTouchingWater(WorldGenLevel level, BlockPos pos) {
        for (Direction face : Direction.values()) {
            if (level.getFluidState(pos.relative(face)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }
}