package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CentrifugeBlock extends BaseEntityBlock {

    public CentrifugeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // ── BlockEntity wiring ────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CentrifugeBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        // Switch to ENTITYBLOCK_ANIMATED later when you add a BESR.
        return RenderShape.MODEL;
    }

    // ── Interaction ───────────────────────────────────────────────────────

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand,
                                          @NotNull BlockHitResult hit) {
        if (level.isClientSide()) {
            // Optimistic return so the client doesn't play the arm-swing animation twice.
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof CentrifugeBlockEntity centrifuge) {
            boolean activated = centrifuge.tryActivate(player, hand);
            return activated ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }
}