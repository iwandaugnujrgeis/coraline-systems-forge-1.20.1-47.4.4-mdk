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
import net.zharok01.coralinesystems.worldgen.feature.config.MixedMushroomConfiguration;

public class MixedMushroomFeature extends Feature<MixedMushroomConfiguration> {

    private static final int CANOPY_SCAN_HEIGHT = 12;

    public MixedMushroomFeature(Codec<MixedMushroomConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MixedMushroomConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        MixedMushroomConfiguration config = context.config();

        int placedCount = 0;
        int xzSpread = config.xzSpread();
        int ySpread = config.ySpread();
        BlockPos.MutableBlockPos canopyCursor = new BlockPos.MutableBlockPos();

        for (int i = 0; i < config.tries(); ++i) {
            int x = origin.getX() + random.nextInt(xzSpread) - random.nextInt(xzSpread);
            int y = origin.getY() + random.nextInt(ySpread) - random.nextInt(ySpread);
            int z = origin.getZ() + random.nextInt(xzSpread) - random.nextInt(xzSpread);

            BlockPos targetPos = new BlockPos(x, y, z);
            BlockState stateToPlace = config.stateProvider().getState(random, targetPos);

            if (!level.isEmptyBlock(targetPos) || level.getMaxLocalRawBrightness(targetPos) > config.maxLight()) {
                continue;
            }

            if (config.requireCanopy()) {
                boolean underCanopy = false;
                for (int offset = 1; offset <= CANOPY_SCAN_HEIGHT; ++offset) {
                    canopyCursor.setWithOffset(targetPos, 0, offset, 0);
                    if (level.getBlockState(canopyCursor).is(BlockTags.LEAVES)) {
                        underCanopy = true;
                        break;
                    }
                }
                if (!underCanopy) {
                    continue;
                }
            }

            if (stateToPlace.canSurvive(level, targetPos)) {
                level.setBlock(targetPos, stateToPlace, Block.UPDATE_CLIENTS);
                placedCount++;
            }
        }

        return placedCount > 0;
    }
}