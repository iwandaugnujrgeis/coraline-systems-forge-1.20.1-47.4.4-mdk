package net.zharok01.coralinesystems.mixin.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GlowstoneFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GlowstoneFeature.class)
public abstract class GlowstoneFeatureMixin extends Feature<NoneFeatureConfiguration> {

    public GlowstoneFeatureMixin(com.mojang.serialization.Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @SuppressWarnings("OverwriteAuthorRequired")
    @Overwrite
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldGenLevel = context.level();
        BlockPos blockPos = context.origin();
        RandomSource randomSource = context.random();

        if (!worldGenLevel.isEmptyBlock(blockPos)) {
            return false;
        } else {
            boolean hasSolidSurface = false;
            Direction[] surfaceDirections = {
                    Direction.UP,
                    Direction.NORTH,
                    Direction.SOUTH,
                    Direction.EAST,
                    Direction.WEST
            };

            for (Direction direction : surfaceDirections) {
                BlockState neighborState = worldGenLevel.getBlockState(blockPos.relative(direction));
                if (coraline$isValidSurface(neighborState)) {
                    hasSolidSurface = true;
                    break;
                }
            }

            if (!hasSolidSurface) {
                return false;
            } else {
                worldGenLevel.setBlock(blockPos, Blocks.GLOWSTONE.defaultBlockState(), 2);

                for (int i = 0; i < 1500; i++) {
                    BlockPos blockPos2 = blockPos.offset(
                            randomSource.nextInt(8) - randomSource.nextInt(8), -randomSource.nextInt(12), randomSource.nextInt(8) - randomSource.nextInt(8)
                    );
                    if (worldGenLevel.getBlockState(blockPos2).isAir()) {
                        int j = 0;

                        for (Direction direction : Direction.values()) {
                            if (worldGenLevel.getBlockState(blockPos2.relative(direction)).is(Blocks.GLOWSTONE)) {
                                j++;
                            }

                            if (j > 1) {
                                break;
                            }
                        }

                        if (j == 1) {
                            worldGenLevel.setBlock(blockPos2, Blocks.GLOWSTONE.defaultBlockState(), 2);
                        }
                    }
                }

                return true;
            }
        }
    }

    private boolean coraline$isValidSurface(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD);
    }
}