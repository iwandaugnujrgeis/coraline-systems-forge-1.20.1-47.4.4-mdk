package net.zharok01.coralinesystems.mixin.compat.supplementaries;

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

    @Inject(method = "m_6227_", at = @At("RETURN"))
    private void coraline$onUse(
            BlockState state, Level worldIn, BlockPos pos, Player player,
            InteractionHand handIn, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (worldIn.isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (cir.getReturnValue() != InteractionResult.CONSUME) return;
        if (!state.hasProperty(PancakeBlock.TOPPING)) return;
        if (state.getValue(PancakeBlock.TOPPING) != Topping.NONE) return;

        BlockState currentState = worldIn.getBlockState(pos);
        if (!currentState.hasProperty(PancakeBlock.TOPPING)) return;

        Topping appliedTopping = currentState.getValue(PancakeBlock.TOPPING);
        if (appliedTopping == Topping.NONE) return;

        CoralineTriggers.PANCAKE_TOPPING.trigger(serverPlayer, appliedTopping);
    }

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