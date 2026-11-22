package net.zharok01.coralinesystems.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.zharok01.coralinesystems.worldgen.feature.config.FallenTreeConfiguration;

import java.util.ArrayList;
import java.util.List;

public class FallenTreeFeature extends Feature<FallenTreeConfiguration> {

    public FallenTreeFeature(Codec<FallenTreeConfiguration> codec) {
        super(codec);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean place(FeaturePlaceContext<FallenTreeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        FallenTreeConfiguration config = context.config();

        if (!level.getFluidState(origin).isEmpty()) {
            return false;
        }

        BlockPos groundOrigin = findGroundPosition(level, origin);
        if (groundOrigin == null) {
            return false;
        }

        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int targetLength = random.nextInt(3) + 4;

        List<BlockPos> validLogPositions = new ArrayList<>();

        for (int i = 0; i < targetLength; i++) {
            BlockPos currentPos = groundOrigin.relative(direction, i);

            if (!level.getFluidState(currentPos).isEmpty()) {
                break;
            }

            BlockState currentState = level.getBlockState(currentPos);
            if (!currentState.isAir() && !currentState.canBeReplaced()) {
                break;
            }

            BlockPos groundBelow = currentPos.below();
            BlockState stateBelow = level.getBlockState(groundBelow);

            if (!level.getFluidState(groundBelow).isEmpty() || !stateBelow.isSolid()) {
                break;
            }

            validLogPositions.add(currentPos);
        }

        if (validLogPositions.size() < 3) {
            return false;
        }

        BlockState logState = config.log().getState(random, groundOrigin)
                .setValue(RotatedPillarBlock.AXIS, direction.getAxis());

        for (BlockPos pos : validLogPositions) {
            level.setBlock(pos, logState, 3);
        }

        return true;
    }

    private BlockPos findGroundPosition(WorldGenLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        for (int i = 0; i < 16; i++) {
            BlockState stateAt = level.getBlockState(mutablePos);
            BlockState stateBelow = level.getBlockState(mutablePos.below());

            if ((stateAt.isAir() || stateAt.canBeReplaced()) &&
                    level.getFluidState(mutablePos).isEmpty() &&
                    level.getFluidState(mutablePos.below()).isEmpty() &&
                    stateBelow.isSolid()) {
                return mutablePos.immutable();
            }

            mutablePos.move(Direction.DOWN);
        }
        return null;
    }
}