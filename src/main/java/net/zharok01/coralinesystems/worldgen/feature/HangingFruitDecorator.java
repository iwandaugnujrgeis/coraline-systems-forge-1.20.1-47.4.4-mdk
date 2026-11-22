package net.zharok01.coralinesystems.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.zharok01.coralinesystems.block.HangingFruitBlock;
import net.zharok01.coralinesystems.registry.CoralineTreeDecoratorTypes;

public class HangingFruitDecorator extends TreeDecorator {

    public static final Codec<HangingFruitDecorator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.floatRange(0.0F, 1.0F)
                            .fieldOf("probability")
                            .forGetter(d -> d.probability),
                    BlockState.CODEC
                            .fieldOf("fruit_state")
                            .forGetter(d -> d.fruitState),
                    Codec.intRange(0, HangingFruitBlock.MAX_AGE)
                            .optionalFieldOf("max_spawn_age", 0)
                            .forGetter(d -> d.maxSpawnAge)
            ).apply(instance, HangingFruitDecorator::new)
    );

    private final float probability;
    private final BlockState fruitState;
    private final int maxSpawnAge;

    public HangingFruitDecorator(float probability, BlockState fruitState, int maxSpawnAge) {
        this.probability = probability;
        this.fruitState = fruitState;
        this.maxSpawnAge = maxSpawnAge;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return CoralineTreeDecoratorTypes.HANGING_FRUIT.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();

        for (BlockPos leafPos : context.leaves()) {
            if (random.nextFloat() >= probability) {
                continue;
            }

            BlockPos below = leafPos.below();

            if (!context.isAir(below)) {
                continue;
            }

            int age = maxSpawnAge == 0 ? 0 : random.nextInt(maxSpawnAge + 1);
            context.setBlock(below, fruitState.setValue(HangingFruitBlock.AGE, age));
        }
    }
}