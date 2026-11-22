package net.zharok01.coralinesystems.worldgen.tree;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
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

public class MegaOakTrunkPlacer extends TrunkPlacer {
    public static final Codec<MegaOakTrunkPlacer> CODEC = RecordCodecBuilder.create(instance ->
            trunkPlacerParts(instance)
                    .and(MegaOakBranchesConfig.CODEC.fieldOf("branch_config").forGetter(placer -> placer.branches))
                    .apply(instance, MegaOakTrunkPlacer::new)
    );

    private static final float BRANCH_TAPER = 0.35F;
    private static final int TOP_CLEARANCE = 3;
    private static final int MIN_STRAIGHT_BEFORE_JAG = 2;
    private static final int TOP_FOLIAGE_RADIUS_BONUS = 1;

    private final MegaOakBranchesConfig branches;

    public MegaOakTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, MegaOakBranchesConfig branches) {
        super(baseHeight, heightRandA, heightRandB);
        this.branches = branches;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return CoralineTrunkPlacers.MEGA_OAK.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, int freeTreeHeight, BlockPos pos, TreeConfiguration config) {
        BlockPos groundPos = pos.below();
        setDirtAt(level, blockSetter, random, groundPos, config);
        setDirtAt(level, blockSetter, random, groundPos.east(), config);
        setDirtAt(level, blockSetter, random, groundPos.south(), config);
        setDirtAt(level, blockSetter, random, groundPos.south().east(), config);

        for (int y = 0; y < freeTreeHeight; y++) {
            this.placeLog(level, blockSetter, random, pos.above(y), config);
            if (y < freeTreeHeight - 1) {
                this.placeLog(level, blockSetter, random, pos.offset(1, y, 0), config);
                this.placeLog(level, blockSetter, random, pos.offset(1, y, 1), config);
                this.placeLog(level, blockSetter, random, pos.offset(0, y, 1), config);
            }
        }

        List<FoliagePlacer.FoliageAttachment> attachments = Lists.newArrayList();
        attachments.add(new FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight), TOP_FOLIAGE_RADIUS_BONUS, true));

        int count = this.branches.count() + random.nextInt(this.branches.countRandom() + 1);
        int firstDirection = random.nextInt(4);

        for (int index = 0; index < count; index++) {
            float heightFraction = count > 1 ? (float) index / (count - 1) : 1.0F;
            int branchY = freeTreeHeight - this.branches.startDown() + Math.round((this.branches.startDown() - TOP_CLEARANCE) * heightFraction);
            int length = Math.round((this.branches.length() + random.nextInt(this.branches.lengthRandom() + 1)) * (1.0F - BRANCH_TAPER * heightFraction));

            Direction direction = Direction.from2DDataValue((firstDirection + index) % 4);
            Direction.Axis branchAxis = direction.getAxis();

            int edgeOffset = random.nextInt(2);
            BlockPos tip = switch (direction) {
                case EAST -> pos.offset(2, branchY, edgeOffset);
                case WEST -> pos.offset(-1, branchY, edgeOffset);
                case SOUTH -> pos.offset(edgeOffset, branchY, 2);
                default -> pos.offset(edgeOffset, branchY, -1);
            };
            this.placeLog(level, blockSetter, random, tip, config, state -> state.trySetValue(RotatedPillarBlock.AXIS, branchAxis));

            for (int step = 1; step < length; step++) {
                if (step >= MIN_STRAIGHT_BEFORE_JAG && random.nextFloat() < this.branches.jagChance()) {
                    tip = tip.above();
                    this.placeLog(level, blockSetter, random, tip, config, state -> state.trySetValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                }
                tip = tip.relative(direction);
                this.placeLog(level, blockSetter, random, tip, config, state -> state.trySetValue(RotatedPillarBlock.AXIS, branchAxis));
            }

            if (random.nextFloat() < this.branches.upturnChance()) {
                int rise = this.branches.upturnHeight().sample(random);
                for (int step = 0; step < rise; step++) {
                    tip = tip.above();
                    this.placeLog(level, blockSetter, random, tip, config, state -> state.trySetValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                }
            }

            attachments.add(new FoliagePlacer.FoliageAttachment(tip, 0, false));
        }

        return attachments;
    }
}