// net/zharok01/coralinesystems/mixins/PancakeBlockMixin.java
package net.zharok01.coralinesystems.mixin;

import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.Topping;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PancakeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PancakeBlock.class, remap = false)
public class PancakeBlockMixin {

    /**
     * Fires PANCAKE_TOPPING when a player successfully applies a topping.
     * m_6227_ = use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)
     */
    @Inject(method = "m_6227_", at = @At("RETURN"))
    private void coraline$onUse(
            BlockState state,
            Level worldIn,
            BlockPos pos,
            Player player,
            InteractionHand handIn,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (worldIn.isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (cir.getReturnValue() != InteractionResult.CONSUME) return;

        // setTopping only fires when the old topping was NONE —
        // this guards against the eating-a-topped-pancake branch.
        if (!state.hasProperty(PancakeBlock.TOPPING)) return;
        if (state.getValue(PancakeBlock.TOPPING) != Topping.NONE) return;

        // Read the topping that was just committed to the world.
        BlockState currentState = worldIn.getBlockState(pos);
        if (!currentState.hasProperty(PancakeBlock.TOPPING)) return;

        Topping appliedTopping = currentState.getValue(PancakeBlock.TOPPING);
        if (appliedTopping == Topping.NONE) return;

        CoralineTriggers.PANCAKE_TOPPING.trigger(serverPlayer, appliedTopping);
    }

    /**
     * Fires FULL_PANCAKE_STACK when placement brings a stack to exactly 8.
     * m_5573_ = getStateForPlacement(BlockPlaceContext)
     */
    @Inject(method = "m_5573_", at = @At("RETURN"))
    private void coraline$onGetStateForPlacement(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir
    ) {
        BlockState resultState = cir.getReturnValue();
        if (resultState == null) return;
        if (!resultState.hasProperty(PancakeBlock.PANCAKES)) return;
        if (resultState.getValue(PancakeBlock.PANCAKES) != 8) return;

        if (!(context.getPlayer() instanceof ServerPlayer serverPlayer)) return;

        CoralineTriggers.FULL_PANCAKE_STACK.trigger(serverPlayer);
    }
}