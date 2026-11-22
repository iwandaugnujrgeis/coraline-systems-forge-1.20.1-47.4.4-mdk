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
import org.jetbrains.annotations.NotNull;

public class RedwoodFoliagePlacer extends FoliagePlacer {
    public static final Codec<RedwoodFoliagePlacer> CODEC = RecordCodecBuilder.create(instance ->
            foliagePlacerParts(instance)
                    .and(IntProvider.codec(0, 24).fieldOf("trunk_height").forGetter(placer -> placer.trunkHeight))
                    .apply(instance, RedwoodFoliagePlacer::new)
    );

    private final IntProvider trunkHeight;

    public RedwoodFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider trunkHeight) {
        super(radius, offset);
        this.trunkHeight = trunkHeight;
    }

    @Override
    protected @NotNull FoliagePlacerType<?> type() {
        return CoralineFoliagePlacers.REDWOOD_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(@NotNull LevelSimulatedReader level, @NotNull FoliageSetter blockSetter, @NotNull RandomSource random, @NotNull TreeConfiguration config, int maxFreeTreeHeight, FoliageAttachment attachment, int foliageHeight, int foliageRadius, int offset) {
        BlockPos blockPos = attachment.pos();
        int maxAllowedRadius = foliageRadius + attachment.radiusOffset();

        for (int localY = offset; localY >= -foliageHeight; --localY) {
            int step = offset - localY;
            int rowRadius = coraline$calculateRowRadius(step, maxAllowedRadius);

            this.placeLeavesRow(level, blockSetter, random, config, blockPos, rowRadius, localY, attachment.doubleTrunk());
        }
    }

    public static int coraline$calculateRowRadius(int step, int maxAllowedRadius) {
        if (step == 0) {
            return 0;
        }
        int cap = Math.min((step + 1) / 2, maxAllowedRadius);
        return (step % 2 == 0) ? Math.max(0, cap - 1) : cap;
    }

    public static int coraline$bareTrunkHeight(int treeHeight, IntProvider bareTrunkHeight) {
        int minHeight = bareTrunkHeight.getMinValue();
        return minHeight + Math.floorMod(treeHeight, bareTrunkHeight.getMaxValue() - minHeight + 1);
    }

    @Override
    public int foliageHeight(@NotNull RandomSource random, int height, @NotNull TreeConfiguration config) {
        return Math.max(4, height - coraline$bareTrunkHeight(height, this.trunkHeight));
    }

    @Override
    protected boolean shouldSkipLocation(@NotNull RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        if (localX == range && localZ == range && range > 0) {
            return true;
        }
        if (range > 1 && (localX == range || localZ == range)) {
            return random.nextFloat() < 0.12F;
        }
        return false;
    }
}