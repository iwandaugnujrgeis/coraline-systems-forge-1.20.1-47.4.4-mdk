package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.zharok01.coralinesystems.event.CentrifugeEvent;
import net.zharok01.coralinesystems.item.OrbItem;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
import net.zharok01.coralinesystems.time.TimeAccelerationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CentrifugeBlockEntity extends BlockEntity {

    /**
     * 1-slot, Orb-only inventory exposed as an IItemHandler capability.
     * This is what lets Hoppers and Droppers feed the Centrifuge
     * automatically (Forge bridges vanilla hopper/dropper insertion to
     * IItemHandler capabilities on adjacent block entities). Also sets us
     * up cleanly for future pipe-block compatibility, since pipes will
     * just target this same capability.
     *
     * Dispensers deliberately do NOT feed this — that mirrors Vanilla,
     * where a dispenser cannot push items into an adjacent container
     * (only droppers/hoppers can); a dispenser's job is to fire/use items.
     *
     * isItemValid is the FIRST line of defense: it gates insertion itself
     * (not just activation), so a Hopper/Dropper can no longer stuff Orbs
     * into the slot while a session is active or the block is Redstone-
     * locked. Without this, Orbs were being silently swallowed into the
     * slot (never extracted, since attemptStart() would fail) — an
     * invisible item sink.
     */
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (!(stack.getItem() instanceof OrbItem)) {
                return false;
            }

            if (canAcceptOrb()) {
                return true;
            }

            // Automation (Hopper/Dropper/future pipes) tried to feed an Orb
            // into a Redstone-locked Centrifuge — same "blank shot" cue as
            // the hand-driven rejection path, so the feedback is consistent
            // regardless of how the insertion was attempted.
            if (isRedstoneLocked()) {
                playBlankShotEffect();
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
            tryAutoActivate();
        }
    };

    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    // ── Player-driven activation (hand-held Orb) ────────────────────────

    public boolean tryActivate(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide()) {
            return false;
        }

        ItemStack heldStack = player.getItemInHand(hand);
        if (!(heldStack.getItem() instanceof OrbItem)) {
            return false;
        }

        // Same gate the ItemStackHandler uses for Hopper/Dropper insertion —
        // refuse the interaction entirely (no shrink, no start attempt) if
        // a session is already running/stopping here, or the block is
        // currently Redstone-locked.
        if (!canAcceptOrb()) {
            // Only the LOCKED case gets the "blank shot" effect — a refusal
            // because a session is already active/stopping isn't a
            // rejection in the same sense (there's nothing wrong with the
            // attempt, the machine's just busy) and already has its own
            // silence-on-no-op behavior via attemptStart().
            if (isRedstoneLocked()) {
                playBlankShotEffect();
            }
            return false;
        }

        boolean started = attemptStart();
        if (!started) {
            return false;
        }

        // Creative players keep their Orb — only Survival/Adventure pay the cost.
        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        // Hand-driven insertion only — Hopper/Dropper/pipe feeding does NOT
        // fire this, per design (tryAutoActivate never calls this trigger).
        if (player instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.CENTRIFUGE_ORB_INSERT.trigger(serverPlayer);
        }

        return true;
    }

    // ── Automated activation (Hopper/Dropper feeding an Orb into the slot) ──

    /**
     * Called whenever the internal item handler's contents change. If a
     * valid Orb has been inserted (by a Hopper, Dropper, or future pipe)
     * and no session is currently running at this Centrifuge, consumes the
     * Orb and starts a session automatically — same activation path as
     * manual player use.
     */
    private void tryAutoActivate() {
        if (level == null || level.isClientSide()) {
            return;
        }

        ItemStack slotStack = itemHandler.getStackInSlot(0);
        if (slotStack.isEmpty() || !(slotStack.getItem() instanceof OrbItem)) {
            return;
        }

        // Belt-and-suspenders: isItemValid should already have refused this
        // Orb if we couldn't accept it, but re-check here too in case the
        // slot was populated some other way (e.g. NBT load edge cases).
        if (!canAcceptOrb()) {
            return;
        }

        boolean started = attemptStart();
        if (started) {
            itemHandler.extractItem(0, 1, false);
        }
    }

    /**
     * Central insertion gate, shared by both the ItemStackHandler
     * (Hopper/Dropper path) and the hand-activation path. An Orb may only
     * be accepted if:
     *   - this Centrifuge does not already have an active/stopping session, and
     *   - this Centrifuge is not currently receiving a Redstone signal.
     * Both Hopper/Dropper insertion and hand-clicking are refused entirely
     * (not silently absorbed) when either condition fails.
     */
    private boolean canAcceptOrb() {
        if (level == null) {
            return false;
        }

        if (level.hasNeighborSignal(worldPosition)) {
            return false;
        }

        TimeAccelerationManager manager = CentrifugeEvent.getManager();
        if (manager != null && manager.isActive(worldPosition)) {
            return false;
        }

        return true;
    }

    /** @return true if this Centrifuge is currently refusing Orbs specifically due to Redstone power. */
    private boolean isRedstoneLocked() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    /**
     * "Blank shot" feedback — fires when a player or automation attempts to
     * insert an Orb into a Redstone-locked Centrifuge. No item is lost;
     * this is purely a rejection cue so the attempt doesn't read as silently
     * swallowed. Placeholder vanilla sound/particles until custom assets
     * are registered.
     */
    void playBlankShotEffect() {
        if (level == null || level.isClientSide()) {
            return;
        }

        level.playSound(
                null,
                worldPosition,
                SoundEvents.ITEM_BREAK, // TODO: CoralineSounds.CENTRIFUGE_BLANK_SHOT
                SoundSource.BLOCKS,
                0.8f,
                1.4f
        );

        ((ServerLevel) level).sendParticles(
                net.minecraft.core.particles.ParticleTypes.SMOKE,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5,
                8,
                0.2, 0.1, 0.2,
                0.02
        );
    }

    /**
     * "Locked" feedback — fires when this Centrifuge transitions into the
     * LOCKED state (Redstone-powered) while it was idle/deactivated.
     * Deliberately distinct from the ABORT effect (played by
     * TimeAccelerationManager.requestStop) which only fires when a
     * Centrifuge gets locked mid-session. Placeholder vanilla sound/
     * particles until custom assets are registered.
     */
    void playLockedEffect() {
        if (level == null || level.isClientSide()) {
            return;
        }

        level.playSound(
                null,
                worldPosition,
                SoundEvents.BEACON_DEACTIVATE, // TODO: CoralineSounds.CENTRIFUGE_LOCKED
                SoundSource.BLOCKS,
                0.6f,
                0.6f
        );

        ((ServerLevel) level).sendParticles(
                net.minecraft.core.particles.ParticleTypes.SMOKE,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5,
                12,
                0.3, 0.15, 0.3,
                0.01
        );
    }

    /**
     * Shared activation logic for both the manual and automated paths.
     * Fires the activation sound/particles only when a session actually
     * starts, so a rejected activation (already running, or coasting to a
     * stop) stays silent.
     */
    private boolean attemptStart() {
        if (level == null || level.isClientSide()) {
            return false;
        }

        TimeAccelerationManager manager = CentrifugeEvent.getManager();
        if (manager == null) {
            return false;
        }

        boolean started = manager.startSession((ServerLevel) level, worldPosition);
        if (!started) {
            return false;
        }

        // Activation sound — one-shot, plays immediately on insertion.
        // Swap SoundEvents.BEACON_ACTIVATE for your custom SoundEvent later.
        level.playSound(
                null,
                worldPosition,
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );

        // Activation particle burst — FLASH gives a sharp "ignition" pop.
        ((ServerLevel) level).sendParticles(
                net.minecraft.core.particles.ParticleTypes.FLASH,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5,
                1,
                0.0, 0.0, 0.0,
                0.0
        );

        return true;
    }

    // ── Capability exposure ─────────────────────────────────────────────

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOptional.invalidate();
    }

    // ── Persistence ──────────────────────────────────────────────────────

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
    }
}