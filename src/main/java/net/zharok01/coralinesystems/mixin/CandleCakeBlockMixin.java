package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.mixin.accessors.CandleCakeBlockAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(CandleCakeBlock.class)
public class CandleCakeBlockMixin {

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/CandleCakeBlock;dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private void coraline$handleCandleCakeEat(BlockState state, Level level, BlockPos pos) {
        // Find which candle belongs to this specific CandleCakeBlock
        Block candle = null;
        for (Map.Entry<Block, CandleCakeBlock> entry : CandleCakeBlockAccessor.getByCandle().entrySet()) {
            if (entry.getValue() == state.getBlock()) {
                candle = entry.getKey();
                break;
            }
        }

        // Drop ONLY the candle item if we found it
        if (candle != null) {
            Block.popResource(level, pos, new ItemStack(candle));
        }

        // By NOT calling dropResources(), we prevent the 7 slices from dropping!
    }
}