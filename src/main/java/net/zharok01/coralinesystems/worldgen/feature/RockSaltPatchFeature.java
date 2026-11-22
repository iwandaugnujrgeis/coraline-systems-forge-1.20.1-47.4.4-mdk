package net.zharok01.coralinesystems.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.zharok01.coralinesystems.worldgen.feature.config.RockSaltPatchConfiguration;

import java.util.ArrayList;
import java.util.List;

public class RockSaltPatchFeature extends Feature<RockSaltPatchConfiguration> {

    private static final int SHORE_SCAN_RADIUS = 6;
    private static final int SHORE_SCAN_HEIGHT = 3;
    private static final int SEA_LEVEL_BAND = 3;
    private static final int SURFACE_RISE = 1;
    private static final int MIN_SOIL_DEPTH = 2;
    private static final float EDGE_WOBBLE = 0.3f;

    public RockSaltPatchFeature(Codec<RockSaltPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RockSaltPatchConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        RockSaltPatchConfiguration config = context.config();

        int seaLevel = level.getSeaLevel();

        if (Math.abs(origin.getY() - seaLevel) > SEA_LEVEL_BAND + SHORE_SCAN_HEIGHT) {
            return false;
        }

        List<BlockPos> shoreBlocks = new ArrayList<>();
        for (int dx = -SHORE_SCAN_RADIUS; dx <= SHORE_SCAN_RADIUS; dx++) {
            for (int dy = -SHORE_SCAN_HEIGHT; dy <= SHORE_SCAN_HEIGHT; dy++) {
                for (int dz = -SHORE_SCAN_RADIUS; dz <= SHORE_SCAN_RADIUS; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (this.coraline$isInSeaLevelBand(pos, seaLevel) && this.coraline$isShoreAnchor(level, pos)) {
                        shoreBlocks.add(pos);
                    }
                }
            }
        }

        if (shoreBlocks.isEmpty()) {
            return false;
        }

        BlockPos center = shoreBlocks.get(random.nextInt(shoreBlocks.size()));
        int radius = config.radius().sample(random);
        int soilDepth = Math.max(MIN_SOIL_DEPTH, radius - 1);

        boolean placedAny = false;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -soilDepth; dy <= SURFACE_RISE; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int verticalRadius = dy < 0 ? soilDepth : SURFACE_RISE;
                    float distance = (dx * dx) / (float) (radius * radius)
                            + (dy * dy) / (float) (verticalRadius * verticalRadius)
                            + (dz * dz) / (float) (radius * radius);
                    float edge = 1.0f + (random.nextFloat() - 0.5f) * 2.0f * EDGE_WOBBLE;
                    if (distance > edge) {
                        continue;
                    }

                    BlockPos pos = center.offset(dx, dy, dz);
                    if (this.coraline$isInSeaLevelBand(pos, seaLevel) && this.coraline$isReplaceable(level.getBlockState(pos))) {
                        level.setBlock(pos, config.block(), 2);
                        placedAny = true;
                    }
                }
            }
        }

        return placedAny;
    }

    private boolean coraline$isInSeaLevelBand(BlockPos pos, int seaLevel) {
        return Math.abs(pos.getY() - seaLevel) <= SEA_LEVEL_BAND;
    }

    private boolean coraline$isReplaceable(BlockState state) {
        return state.is(BlockTags.OVERWORLD_CARVER_REPLACEABLES) && state.getFluidState().isEmpty();
    }

    private boolean coraline$isShoreAnchor(WorldGenLevel level, BlockPos pos) {
        if (!this.coraline$isReplaceable(level.getBlockState(pos)) || !level.getFluidState(pos.above()).isEmpty()) {
            return false;
        }

        for (Direction face : Direction.values()) {
            if (level.getFluidState(pos.relative(face)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }
}