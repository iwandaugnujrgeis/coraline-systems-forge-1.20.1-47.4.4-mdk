package net.zharok01.coralinesystems.worldgen.tree;

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
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.zharok01.coralinesystems.registry.CoralineTrunkPlacers;

import java.util.List;
import java.util.function.BiConsumer;

public class TwistedTrunkPlacer extends TrunkPlacer {
    public static final Codec<TwistedTrunkPlacer> CODEC = RecordCodecBuilder.create(instance ->
            trunkPlacerParts(instance).and(instance.group(
                    IntProvider.codec(2, 16).fieldOf("bend_spacing").forGetter(placer -> placer.bendSpacing),
                    IntProvider.codec(1, 4).fieldOf("bend_length").forGetter(placer -> placer.bendLength),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("twist_chance").orElse(0.0F).forGetter(placer -> placer.twistChance),
                    Codec.intRange(0, 16).fieldOf("straight_base").orElse(3).forGetter(placer -> placer.straightBase),
                    Codec.intRange(0, 16).fieldOf("straight_top").orElse(3).forGetter(placer -> placer.straightTop)
            )).apply(instance, TwistedTrunkPlacer::new)
    );

    private final IntProvider bendSpacing;
    private final IntProvider bendLength;
    private final float twistChance;
    private final int straightBase;
    private final int straightTop;

    public TwistedTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, IntProvider bendSpacing, IntProvider bendLength, float twistChance, int straightBase, int straightTop) {
        super(baseHeight, heightRandA, heightRandB);
        this.bendSpacing = bendSpacing;
        this.bendLength = bendLength;
        this.twistChance = twistChance;
        this.straightBase = straightBase;
        this.straightTop = straightTop;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return CoralineTrunkPlacers.TWISTED.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, int freeTreeHeight, BlockPos pos, TreeConfiguration config) {
        setDirtAt(level, blockSetter, random, pos.below(), config);

        BlockPos.MutableBlockPos cursor = pos.mutable();
        Direction lastDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int blocksSinceBend = 0;
        int nextBendAt = this.bendSpacing.sample(random);

        for (int y = 0; y < freeTreeHeight; y++) {
            this.placeLog(level, blockSetter, random, cursor, config);

            boolean canBend = y >= this.straightBase && y < freeTreeHeight - 1 - this.straightTop;
            int sidewaysBlocks = 0;
            if (canBend) {
                blocksSinceBend++;
                if (blocksSinceBend >= nextBendAt) {
                    sidewaysBlocks = this.bendLength.sample(random);
                    blocksSinceBend = 0;
                    nextBendAt = this.bendSpacing.sample(random);
                } else if (random.nextFloat() < this.twistChance) {
                    sidewaysBlocks = 1;
                }
            }

            if (sidewaysBlocks > 0) {
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                if (direction == lastDirection.getOpposite()) {
                    direction = lastDirection;
                }
                Direction.Axis sidewaysAxis = direction.getAxis();
                for (int step = 0; step < sidewaysBlocks; step++) {
                    cursor.move(direction);
                    this.placeLog(level, blockSetter, random, cursor, config, state -> state.trySetValue(RotatedPillarBlock.AXIS, sidewaysAxis));
                }
                lastDirection = direction;
            }

            if (y < freeTreeHeight - 1) {
                cursor.move(Direction.UP);
            }
        }

        return List.of(new FoliagePlacer.FoliageAttachment(cursor.immutable().above(), 0, false));
    }
}