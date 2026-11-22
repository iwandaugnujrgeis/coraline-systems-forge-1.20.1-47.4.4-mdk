package net.zharok01.coralinesystems.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.zharok01.coralinesystems.registry.CoralineFoliagePlacers;

public class MegaOakFoliagePlacer extends FoliagePlacer {
    public static final Codec<MegaOakFoliagePlacer> CODEC = RecordCodecBuilder.create(instance ->
            foliagePlacerParts(instance).and(instance.group(
                    Codec.intRange(0, 8).fieldOf("vertical_radius").forGetter(placer -> placer.verticalRadius),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("rim_skip_chance").orElse(0.0F).forGetter(placer -> placer.rimSkipChance)
            )).apply(instance, MegaOakFoliagePlacer::new)
    );

    private static final float RIM_THRESHOLD = 0.7F;

    private final int verticalRadius;
    private final float rimSkipChance;

    public MegaOakFoliagePlacer(IntProvider radius, IntProvider offset, int verticalRadius, float rimSkipChance) {
        super(radius, offset);
        this.verticalRadius = verticalRadius;
        this.rimSkipChance = rimSkipChance;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return CoralineFoliagePlacers.MEGA_OAK_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, FoliageSetter blockSetter, RandomSource random, TreeConfiguration config, int maxFreeTreeHeight, FoliageAttachment attachment, int foliageHeight, int foliageRadius, int offset) {
        BlockPos center = attachment.pos().above(offset);
        int horizontalRadius = foliageRadius + attachment.radiusOffset();
        int doubleTrunkExtra = attachment.doubleTrunk() ? 1 : 0;
        float centerShift = doubleTrunkExtra * 0.5F;
        float horizontalReach = horizontalRadius + 0.5F;
        float verticalReach = this.verticalRadius + 0.5F;

        for (int localY = 0; localY <= this.verticalRadius; localY++) {
            for (int localX = -horizontalRadius; localX <= horizontalRadius + doubleTrunkExtra; localX++) {
                for (int localZ = -horizontalRadius; localZ <= horizontalRadius + doubleTrunkExtra; localZ++) {
                    float shiftedX = localX - centerShift;
                    float shiftedZ = localZ - centerShift;
                    float distance = (shiftedX * shiftedX + shiftedZ * shiftedZ) / (horizontalReach * horizontalReach) + (localY * localY) / (verticalReach * verticalReach);

                    if (distance > 1.0F) {
                        continue;
                    }
                    if (distance > RIM_THRESHOLD && random.nextFloat() < this.rimSkipChance) {
                        continue;
                    }

                    tryPlaceLeaf(level, blockSetter, random, config, center.offset(localX, localY, localZ));
                }
            }
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int height, TreeConfiguration config) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        return false;
    }
}