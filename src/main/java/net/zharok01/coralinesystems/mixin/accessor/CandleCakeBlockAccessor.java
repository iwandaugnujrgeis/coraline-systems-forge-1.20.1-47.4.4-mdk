package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleCakeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(CandleCakeBlock.class)
public interface CandleCakeBlockAccessor {
    @Accessor("BY_CANDLE")
    static Map<Block, CandleCakeBlock> getByCandle() {
        throw new AssertionError("Mixin failed to apply Accessor!");
    }
}