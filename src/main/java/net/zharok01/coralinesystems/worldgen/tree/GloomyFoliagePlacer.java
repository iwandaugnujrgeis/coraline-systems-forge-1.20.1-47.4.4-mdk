package net.zharok01.coralinesystems.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.zharok01.coralinesystems.registry.CoralineFoliagePlacers;

public class GloomyFoliagePlacer extends FoliagePlacer {
    public static final Codec<GloomyFoliagePlacer> CODEC = RecordCodecBuilder.create(instance ->
            foliagePlacerParts(instance).and(instance.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("lean_chance").orElse(0.0F).forGetter(placer -> placer.leanChance),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("droop_chance").orElse(0.0F).forGetter(placer -> placer.droopChance),
                    Codec.BOOL.fieldOf("oak_blob").orElse(false).forGetter(placer -> placer.oakBlob)
            )).apply(instance, GloomyFoliagePlacer::new)
    );

    private static final int BLOB_TOP_ROW = 2;

    private final float leanChance;
    private final float droopChance;
    private final boolean oakBlob;

    public GloomyFoliagePlacer(IntProvider radius, IntProvider offset, float leanChance, float droopChance, boolean oakBlob) {
        super(radius, offset);
        this.leanChance = leanChance;
        this.droopChance = droopChance;
        this.oakBlob = oakBlob;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return CoralineFoliagePlacers.GLOOMY_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, FoliageSetter blockSetter, RandomSource random, TreeConfiguration config, int maxFreeTreeHeight, FoliageAttachment attachment, int foliageHeight, int foliageRadius, int offset) {
        BlockPos center = attachment.pos().above(offset);
        int radius = foliageRadius + attachment.radiusOffset();

        BlockPos upperCenter = random.nextFloat() < this.leanChance
                ? center.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random))
                : center;

        if (this.oakBlob) {
            this.placeLeavesRow(level, blockSetter, random, config, center, radius, -1, false);
            this.placeLeavesRow(level, blockSetter, random, config, center, radius, 0, false);
            this.placeLeavesRow(level, blockSetter, random, config, upperCenter, radius - 1, 1, false);
            this.placeLeavesRow(level, blockSetter, random, config, upperCenter, radius - 1, BLOB_TOP_ROW, false);
            return;
        }

        int bottomRange = radius - 1;
        this.placeLeavesRow(level, blockSetter, random, config, center, bottomRange, -1, false);
        this.placeLeavesRow(level, blockSetter, random, config, center, radius, 0, false);
        this.placeLeavesRow(level, blockSetter, random, config, upperCenter, radius, 1, false);
        this.placeLeavesRow(level, blockSetter, random, config, upperCenter, radius - 2, 2, false);

        for (int localX = -bottomRange; localX <= bottomRange; localX++) {
            for (int localZ = -bottomRange; localZ <= bottomRange; localZ++) {
                boolean onRim = Math.max(Math.abs(localX), Math.abs(localZ)) == bottomRange;
                boolean isCorner = Math.abs(localX) == bottomRange && Math.abs(localZ) == bottomRange;
                if (onRim && !isCorner && random.nextFloat() < this.droopChance) {
                    tryPlaceLeaf(level, blockSetter, random, config, center.offset(localX, -2, localZ));
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
        if (range == 0 || localX != range || localZ != range) {
            return false;
        }
        if (!this.oakBlob) {
            return true;
        }
        return localY >= BLOB_TOP_ROW || random.nextInt(2) == 0;
    }
}