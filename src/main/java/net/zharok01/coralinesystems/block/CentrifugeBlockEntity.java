package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.event.ServerEventListener;
import net.zharok01.coralinesystems.item.OrbItem;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import org.jetbrains.annotations.NotNull;

/**
 * BlockEntity for the Centrifuge block.
 *
 * Interaction contract:
 *   - Player right-clicks holding an {@link OrbItem}.
 *   - If no session is active, the Orb is consumed and a session starts.
 *   - If a session is already active, the interaction is silently ignored.
 */
public class CentrifugeBlockEntity extends BlockEntity {

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    /**
     * Called from {@link net.zharok01.coralinesystems.block.CentrifugeBlock#use}.
     *
     * @param player the player who right-clicked
     * @param hand   the hand used
     * @return true if the interaction was handled (Orb consumed, session started)
     */
    public boolean tryActivate(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide()) {
            return false;
        }

        var manager = ServerEventListener.getManager();
        if (manager == null) {
            return false;
        }

        // Refuse silently if already running.
        if (manager.isActive()) {
            return false;
        }

        ItemStack heldStack = player.getItemInHand(hand);
        if (!(heldStack.getItem() instanceof OrbItem)) {
            return false;
        }

        // Consume exactly one Orb.
        heldStack.shrink(1);

        // Start the session.
        manager.startSession();

        // Placeholder activation sound — swap for your custom SoundEvent later.
        level.playSound(
                null,                          // null = play for all nearby players
                worldPosition,
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );

        return true;
    }

    // ── NBT (nothing to persist for now, skeleton for later) ──────────────

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
    }
}