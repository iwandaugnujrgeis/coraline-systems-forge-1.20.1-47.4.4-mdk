package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.zharok01.coralinesystems.event.CentrifugeEvent;
import net.zharok01.coralinesystems.time.TimeAccelerationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CentrifugeBlock extends BaseEntityBlock {

    /** Drives the idle/activated blockstate and texture swap. */
    public static final BooleanProperty ACTIVATED =
            BooleanProperty.create("activated");

    /**
     * Drives the third "blocked" blockstate/texture — true whenever the
     * Centrifuge is Redstone-locked (i.e. currently refusing Orb insertion
     * because it's receiving a neighbor signal). Independent of ACTIVATED:
     * a Centrifuge can be LOCKED while idle (never started because it was
     * powered) or LOCKED while still ACTIVATED (powered mid-session, now
     * coasting through its abort inertia). The blockstate/model layer
     * should treat LOCKED as taking visual priority over plain idle, but
     * players can still see ACTIVATED's running effects layered if both
     * happen to be true at once during the abort coast-down.
     */
    public static final BooleanProperty LOCKED =
            BooleanProperty.create("locked");

    public CentrifugeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        // Default state: not activated, not locked.
        registerDefaultState(stateDefinition.any()
                .setValue(ACTIVATED, false)
                .setValue(LOCKED, false));
    }

    // ── Blockstate ────────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED, LOCKED);
    }

    // ── BlockEntity wiring ────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CentrifugeBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Interaction ───────────────────────────────────────────────────────

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand,
                                          @NotNull BlockHitResult hit) {
        // Only claim the interaction if the player is actually holding an
        // Orb — otherwise we must PASS on both sides so vanilla's normal
        // item-use logic (e.g. placing a held block like a Dropper against
        // us) runs uninterrupted, including its client-predicted sound.
        // Previously the client branch unconditionally returned SUCCESS
        // regardless of held item, which suppressed the client-side place
        // swing/sound even though the server correctly fell through.
        if (!(player.getItemInHand(hand).getItem() instanceof net.zharok01.coralinesystems.item.OrbItem)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof CentrifugeBlockEntity centrifuge) {
            boolean activated = centrifuge.tryActivate(player, hand);
            return activated ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    // ── Redstone abort / lock ────────────────────────────────────────────

    /**
     * Vanilla hook fired whenever a neighboring block change could affect
     * this block's Redstone signal. We check if we're now powered and, if
     * so, abort any running session at this position with the "aborted"
     * (alarming) sound/particle variant rather than the calm natural-stop one.
     *
     * Also drives the LOCKED blockstate, and — when the Centrifuge becomes
     * LOCKED while it was NOT running (ACTIVATED == false) — fires a
     * distinct "locked" sound/particle effect. This is deliberately
     * separate from the ABORT effect below: ABORT means "mid-use, now
     * being forcibly stopped"; LOCKED-while-idle means "you can't use this
     * right now," a much calmer/administrative cue.
     */
    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                @NotNull Block neighborBlock, @NotNull BlockPos neighborPos,
                                boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (level.isClientSide()) {
            return;
        }

        boolean powered = level.hasNeighborSignal(pos);
        boolean wasLocked = state.hasProperty(LOCKED) && state.getValue(LOCKED);
        boolean activated = state.hasProperty(ACTIVATED) && state.getValue(ACTIVATED);

        // Keep the LOCKED blockstate (blocked texture + dim glow) in sync
        // with the actual Redstone-power condition that CentrifugeBlockEntity
        // .canAcceptOrb() checks — every neighbor change re-evaluates this,
        // so the visual never drifts from the real insertion-lock state.
        if (state.hasProperty(LOCKED) && wasLocked != powered) {
            level.setBlock(pos, state.setValue(LOCKED, powered), 3);
        }

        if (!powered) {
            return;
        }

        // Newly locked (wasn't locked a moment ago) while sitting idle:
        // this is the calm "locked" cue, not the alarming abort one.
        if (!wasLocked && !activated && level instanceof ServerLevel
                && level.getBlockEntity(pos) instanceof CentrifugeBlockEntity centrifuge) {
            centrifuge.playLockedEffect();
        }

        if (!activated) {
            return;
        }

        TimeAccelerationManager manager = CentrifugeEvent.getManager();
        if (manager == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        manager.requestStop(serverLevel, pos, true);
    }

    // ── Explosion on breaking an active, unpowered Centrifuge ───────────

    /**
     * Fired whenever this block's BlockState is replaced (broken, or
     * silently swapped for another CentrifugeBlock state via setBlock).
     * We only care about a genuine removal — i.e. the new block is no
     * longer a CentrifugeBlock — and only explode if the Centrifuge was
     * actively speeding up time and NOT Redstone-locked (a locked
     * Centrifuge can't have an active session in the first place, but we
     * check explicitly for clarity/future-proofing).
     *
     * The running session is explicitly stopped BEFORE the explosion
     * fires, per design: requestStop plays the abort sound/particles as
     * the "moment it's being killed," and the explosion follows as the
     * physical consequence of breaking it while under load.
     */
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         @NotNull BlockState newState, boolean isMoving) {
        boolean genuinelyRemoved = !state.is(newState.getBlock());

        if (genuinelyRemoved && !level.isClientSide()
                && level instanceof ServerLevel serverLevel
                && state.hasProperty(ACTIVATED) && state.getValue(ACTIVATED)
                && state.hasProperty(LOCKED) && !state.getValue(LOCKED)) {

            TimeAccelerationManager manager = CentrifugeEvent.getManager();
            if (manager != null) {
                manager.requestStop(serverLevel, pos, true);
            }

            explodeCentrifuge(serverLevel, pos);
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    /**
     * TNT-style block-damaging explosion, contained (power 2.25 — between
     * a fireball and a full TNT charge), with mob-griefing fire enabled
     * exactly like TNTBlock's own explosion. Uses SoundEvents.GENERIC_EXPLODE
     * as a placeholder until CoralineSounds.CENTRIFUGE_EXPLODE is registered.
     */
    private static void explodeCentrifuge(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        // TODO: swap for CoralineSounds.CENTRIFUGE_EXPLODE.get() once registered.
        level.playSound(
                null,
                pos,
                SoundEvents.GENERIC_EXPLODE, // TODO: CoralineSounds.CENTRIFUGE_EXPLODE
                SoundSource.BLOCKS,
                4.0f,
                (level.random.nextFloat() - level.random.nextFloat()) * 0.2f + 1.0f
        );

        level.sendParticles(
                ParticleTypes.EXPLOSION,
                cx, cy, cz,
                1, 0.0, 0.0, 0.0, 0.0
        );
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                cx, cy, cz,
                30, 0.4, 0.4, 0.4, 0.05
        );

        level.explode(
                null,               // no entity source
                cx, cy, cz,
                2.25f,               // contained blast radius (between 2.0-2.5)
                true,                // setFire — mob-griefing fire like TNT
                Level.ExplosionInteraction.TNT
        );
    }

    // ── Client-side ambient effects (Furnace pattern) ─────────────────────

    /**
     * Called on the client only, roughly once per frame while the chunk is
     * loaded. We gate everything behind the ACTIVATED property so effects
     * only appear when the Centrifuge is running (including while it's
     * coasting through its stop-inertia phase, since ACTIVATED stays true
     * until the session actually ends).
     */
    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull RandomSource random) {
        if (!state.getValue(ACTIVATED)) {
            return;
        }

        double cx = pos.getX() + 0.5;
        double cy = pos.getY();
        double cz = pos.getZ() + 0.5;

        // Looping hum: ~10% chance per tick, same pattern as the Furnace crackle.
        // Swap SoundEvents.BEACON_AMBIENT for your custom SoundEvent later.
        if (random.nextDouble() < 0.1) {
            level.playLocalSound(
                    cx, cy, cz,
                    SoundEvents.BEACON_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5f,   // volume — quiet background hum
                    1.0f,
                    false
            );
        }

        // Ambient particles: a gentle scatter of enchantment glints above the block.
        for (int i = 0; i < 2; i++) {
            double ox = (random.nextDouble() - 0.5) * 0.8;
            double oz = (random.nextDouble() - 0.5) * 0.8;
            double oy = random.nextDouble() * 0.4 + 0.5;

            level.addParticle(
                    ParticleTypes.ENCHANT,
                    cx + ox, cy + oy, cz + oz,
                    0.0, 0.05, 0.0
            );
        }

        // Occasional END_ROD particle for extra visual punch.
        if (random.nextDouble() < 0.3) {
            level.addParticle(
                    ParticleTypes.END_ROD,
                    cx + (random.nextDouble() - 0.5) * 0.6,
                    cy + random.nextDouble() * 0.6 + 0.8,
                    cz + (random.nextDouble() - 0.5) * 0.6,
                    0.0, 0.02, 0.0
            );
        }
    }
}