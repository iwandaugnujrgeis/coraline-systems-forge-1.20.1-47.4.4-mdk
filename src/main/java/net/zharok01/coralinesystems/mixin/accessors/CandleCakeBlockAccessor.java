package net.zharok01.coralinesystems.mixin.accessors;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleCakeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(CandleCakeBlock.class)
public interface CandleCakeBlockAccessor {
    // This allows us to read the private static BY_CANDLE map from CandleCakeBlock
    @Accessor("BY_CANDLE")
    static Map<Block, CandleCakeBlock> getByCandle() {
        throw new AssertionError("Mixin failed to apply Accessor!");
    }
}