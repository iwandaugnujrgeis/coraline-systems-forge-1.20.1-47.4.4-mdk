package net.zharok01.coralinesystems.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.zharok01.coralinesystems.registry.CoralineTreeDecoratorTypes;

public class LeafLichenDecorator extends TreeDecorator {
    public static final Codec<LeafLichenDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
            .fieldOf("probability")
            .xmap(LeafLichenDecorator::new, decorator -> decorator.probability)
            .codec();

    private static final Direction[] LICHEN_SIDES = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private final float probability;

    public LeafLichenDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return CoralineTreeDecoratorTypes.LEAF_LICHEN.get();
    }

    @Override
    public void place(Context context) {
        for (BlockPos leafPos : context.leaves()) {
            if (context.random().nextFloat() >= this.probability) {
                continue;
            }

            Direction side = LICHEN_SIDES[context.random().nextInt(LICHEN_SIDES.length)];
            BlockPos lichenPos = leafPos.relative(side);

            if (context.isAir(lichenPos)) {
                context.setBlock(lichenPos, Blocks.GLOW_LICHEN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(side.getOpposite()), true));
            }
        }
    }
}