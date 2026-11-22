package net.zharok01.coralinesystems.worldgen.tree;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.zharok01.coralinesystems.registry.CoralineTrunkPlacers;

import java.util.List;
import java.util.function.BiConsumer;

public class RedwoodTrunkPlacer extends GiantTrunkPlacer {
    public static final Codec<RedwoodTrunkPlacer> CODEC = RecordCodecBuilder.create(instance ->
            trunkPlacerParts(instance).and(instance.group(
                    IntProvider.codec(0, 24).fieldOf("bare_trunk_height").forGetter(placer -> placer.bareTrunkHeight),
                    Codec.intRange(1, 16).fieldOf("foliage_radius").forGetter(placer -> placer.foliageRadius),
                    Codec.intRange(0, 16).fieldOf("foliage_offset").forGetter(placer -> placer.foliageOffset),
                    Codec.intRange(1, 8).fieldOf("branch_length").forGetter(placer -> placer.branchLength)
            )).apply(instance, RedwoodTrunkPlacer::new)
    );

    private static final int WIDE_ROW_RADIUS = 4;

    private final IntProvider bareTrunkHeight;
    private final int foliageRadius;
    private final int foliageOffset;
    private final int branchLength;

    public RedwoodTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, IntProvider bareTrunkHeight, int foliageRadius, int foliageOffset, int branchLength) {
        super(baseHeight, heightRandA, heightRandB);
        this.bareTrunkHeight = bareTrunkHeight;
        this.foliageRadius = foliageRadius;
        this.foliageOffset = foliageOffset;
        this.branchLength = branchLength;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return CoralineTrunkPlacers.REDWOOD.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, int freeTreeHeight, BlockPos pos, TreeConfiguration config) {
        List<FoliagePlacer.FoliageAttachment> attachments = Lists.newArrayList();
        attachments.addAll(super.placeTrunk(level, blockSetter, random, freeTreeHeight, pos, config));

        int canopyBottomY = RedwoodFoliagePlacer.coraline$bareTrunkHeight(freeTreeHeight, this.bareTrunkHeight);
        int canopyTopY = freeTreeHeight + this.foliageOffset;

        for (int rowY = canopyBottomY; rowY < canopyTopY; rowY++) {
            int rowRadius = RedwoodFoliagePlacer.coraline$calculateRowRadius(canopyTopY - rowY, this.foliageRadius);
            if (rowRadius < WIDE_ROW_RADIUS) {
                continue;
            }

            int branchY = rowY == canopyBottomY ? rowY + 1 : rowY;

            boolean mirrored = random.nextBoolean();

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos start = switch (direction) {
                    case EAST -> pos.offset(2, branchY, mirrored ? 0 : 1);
                    case SOUTH -> pos.offset(mirrored ? 1 : 0, branchY, 2);
                    case WEST -> pos.offset(-1, branchY, mirrored ? 1 : 0);
                    default -> pos.offset(mirrored ? 0 : 1, branchY, -1);
                };

                for (int step = 0; step < this.branchLength; step++) {
                    this.placeLog(level, blockSetter, random, start.relative(direction, step), config,
                            state -> state.trySetValue(RotatedPillarBlock.AXIS, direction.getAxis()));
                }
            }
        }

        return attachments;
    }
}