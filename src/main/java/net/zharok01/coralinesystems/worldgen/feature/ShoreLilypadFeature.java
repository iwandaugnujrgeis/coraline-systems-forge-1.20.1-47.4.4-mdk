package net.zharok01.coralinesystems.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.zharok01.coralinesystems.worldgen.feature.config.ShoreLilypadConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ShoreLilypadFeature extends Feature<ShoreLilypadConfiguration> {

    private static final int SCAN_RADIUS = 10;
    private static final int CLUSTER_RADIUS = 4;
    private static final int MAX_SHORE_DISTANCE = 4;

    public ShoreLilypadFeature(Codec<ShoreLilypadConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ShoreLilypadConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        ShoreLilypadConfiguration config = context.config();

        List<BlockPos> shoreSurfaces = collectShoreSurfaces(level, origin);
        if (shoreSurfaces.isEmpty()) {
            return false;
        }

        BlockPos anchor = shoreSurfaces.get(random.nextInt(shoreSurfaces.size()));

        List<BlockPos> clusterCandidates = new ArrayList<>();
        for (BlockPos candidate : shoreSurfaces) {
            if (chebyshevDistance(candidate, anchor) <= CLUSTER_RADIUS) {
                clusterCandidates.add(candidate);
            }
        }

        int count = config.count().sample(random);
        boolean placedAny = false;

        for (int i = 0; i < count && !clusterCandidates.isEmpty(); i++) {
            int idx = random.nextInt(clusterCandidates.size());
            BlockPos target = clusterCandidates.get(idx);
            clusterCandidates.remove(idx);

            if (isValidLilypadSurface(level, target)) {
                level.setBlock(target, Blocks.LILY_PAD.defaultBlockState(), 3);
                placedAny = true;
            }
        }

        return placedAny;
    }

    private List<BlockPos> collectShoreSurfaces(WorldGenLevel level, BlockPos origin) {
        List<BlockPos> result = new ArrayList<>();
        int y = origin.getY();

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                BlockPos candidate = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                if (isValidLilypadSurface(level, candidate) && isNearShore(level, candidate)) {
                    result.add(candidate);
                }
            }
        }

        return result;
    }

    private boolean isValidLilypadSurface(WorldGenLevel level, BlockPos pos) {
        BlockState above = level.getBlockState(pos);
        BlockState below = level.getBlockState(pos.below());
        return above.isAir()
                && below.getFluidState().is(FluidTags.WATER)
                && below.getFluidState().isSource();
    }

    private boolean isNearShore(WorldGenLevel level, BlockPos surface) {
        int y = surface.getY();
        for (int dx = -MAX_SHORE_DISTANCE; dx <= MAX_SHORE_DISTANCE; dx++) {
            for (int dz = -MAX_SHORE_DISTANCE; dz <= MAX_SHORE_DISTANCE; dz++) {
                BlockPos neighbor = new BlockPos(surface.getX() + dx, y, surface.getZ() + dz);
                BlockState state = level.getBlockState(neighbor);
                if (state.isSolid() && level.getFluidState(neighbor).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int chebyshevDistance(BlockPos a, BlockPos b) {
        return Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getZ() - b.getZ()));
    }
}