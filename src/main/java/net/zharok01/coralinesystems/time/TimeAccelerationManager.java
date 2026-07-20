package net.zharok01.coralinesystems.time;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.block.CentrifugeBlock;
import net.zharok01.coralinesystems.event.CentrifugeEvent;
import net.zharok01.coralinesystems.util.data.CentrifugeAccelerationData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages ALL concurrently-active Centrifuge time-acceleration sessions in
 * the overworld. Multiple Centrifuges may run at once — there is no cap.
 * Their individual speed contributions are combined with a diminishing-
 * returns falloff (see {@link #computeExtraTicks()}), so stacking
 * Centrifuges helps but with ever-shrinking marginal benefit.
 */
public class TimeAccelerationManager {

    /** Geometric falloff rate applied per additional concurrent session, ranked by speed. */
    private static final double DIMINISHING_RETURNS_FACTOR = 0.7;

    /** One entry per currently-active Centrifuge, keyed by its BlockPos. */
    private final Map<BlockPos, TimeAccelerationSession> activeSessions = new LinkedHashMap<>();

    /** The SavedData instance for this world — fetched once on construction. */
    private final CentrifugeAccelerationData savedData;

    /**
     * Created by {@link CentrifugeEvent} on world load. Immediately restores
     * any sessions that were active when the world was last saved.
     *
     * @param level the overworld ServerLevel
     */
    public TimeAccelerationManager(ServerLevel level) {
        this.savedData = CentrifugeAccelerationData.get(level);
        tryRestoreSessions(level);
    }

    /**
     * Reconstructs all in-progress sessions from saved data.
     * Called once during construction — no sound or particles on restore,
     * since those are first-activation / first-stop events only.
     */
    private void tryRestoreSessions(ServerLevel level) {
        for (CentrifugeAccelerationData.SavedSession saved : savedData.getSessions()) {
            if (saved.ticksRemaining() <= 0 && !saved.stopping()) {
                continue; // invalid / stale entry, drop it silently
            }

            TimeAccelerationSession session = new TimeAccelerationSession(
                    saved.ticksRemaining(),
                    saved.stopping(),
                    saved.stopTicksRemaining(),
                    saved.frozenSpeed()
            );

            activeSessions.put(saved.source(), session);

            // Re-apply the activated blockstate in case the chunk had loaded
            // but the blockstate wasn't persisted correctly (e.g. mid-crash).
            setActivatedState(level, saved.source(), true);
        }
    }

    /**
     * Starts a new acceleration session at the given Centrifuge. Unlike the
     * old single-session model, this does NOT fail if other Centrifuges are
     * already running elsewhere — only if THIS specific Centrifuge already
     * has a session (running or coasting to a stop).
     *
     * @param level     the overworld ServerLevel
     * @param sourcePos the BlockPos of the Centrifuge that triggered activation
     * @return true if a new session was started
     */
    public boolean startSession(ServerLevel level, BlockPos sourcePos) {
        BlockPos key = sourcePos.immutable();

        if (activeSessions.containsKey(key)) {
            // Already running, or locked while coasting to a stop.
            return false;
        }

        TimeAccelerationSession session = new TimeAccelerationSession();
        activeSessions.put(key, session);

        setActivatedState(level, key, true);

        persistAll();

        return true;
    }

    /**
     * @param sourcePos the Centrifuge to check
     * @return true if that specific Centrifuge has a running or stopping session
     */
    public boolean isActive(BlockPos sourcePos) {
        return activeSessions.containsKey(sourcePos.immutable());
    }

    /** @return true if ANY Centrifuge session is currently active anywhere */
    public boolean isAnyActive() {
        return !activeSessions.isEmpty();
    }

    /**
     * Requests that a specific Centrifuge's session begin its inertia coast-
     * down instead of stopping instantly. Used both for manual player
     * deactivation and for Redstone abort.
     *
     * Fires the appropriate sound/particle burst immediately (abort effects
     * differ from a natural stop), then lets {@link #tick} carry the session
     * through its {@link TimeAccelerationSession#STOP_INERTIA_TICKS} coast.
     *
     * Safe to call on a Centrifuge with no active session (no-op), or one
     * already stopping (no-op, avoids double-firing effects).
     *
     * @param level   the overworld ServerLevel
     * @param source  the Centrifuge being stopped
     * @param aborted true if this is a Redstone-triggered abort (different
     *                sound/particles), false for a natural/manual stop
     */
    public void requestStop(ServerLevel level, BlockPos source, boolean aborted) {
        BlockPos key = source.immutable();
        TimeAccelerationSession session = activeSessions.get(key);
        if (session == null || session.isStopping()) {
            return;
        }

        session.beginStopping();
        playStopEffects(level, key, aborted);
        persistAll();
    }

    /**
     * Called once per server tick for the overworld. Advances every active
     * session, combines their speed contributions with diminishing returns,
     * applies the resulting time skip once, and cleans up any sessions that
     * finished this tick.
     */
    public void tick(ServerLevel level) {
        if (activeSessions.isEmpty()) {
            return;
        }

        long extraTicks = computeExtraTicks();
        if (extraTicks > 0) {
            level.setDayTime(level.getDayTime() + extraTicks);
        }

        broadcastTime(level);

        List<BlockPos> finished = new ArrayList<>();

        for (Map.Entry<BlockPos, TimeAccelerationSession> entry : activeSessions.entrySet()) {
            boolean stillRunning = entry.getValue().tick();
            if (!stillRunning) {
                finished.add(entry.getKey());
            }
        }

        for (BlockPos pos : finished) {
            endSession(level, pos);
        }

        // Only persist per-tick if something finished; otherwise rely on
        // Vanilla's auto-save cadence for mid-session progress, same as before.
        if (!finished.isEmpty()) {
            persistAll();
        } else {
            persistAllQuiet();
        }
    }

    /**
     * Combines every active session's current speed into a single extra-
     * ticks-this-tick value, using geometric diminishing returns: the
     * fastest-contributing session counts fully, the next counts at
     * {@link #DIMINISHING_RETURNS_FACTOR}x, the next at that squared, etc.
     * This means adding more Centrifuges always helps, but with rapidly
     * shrinking marginal benefit — no hard cap needed.
     */
    private long computeExtraTicks() {
        List<Double> speeds = new ArrayList<>(activeSessions.size());
        for (TimeAccelerationSession session : activeSessions.values()) {
            speeds.add(session.getCurrentSpeed());
        }
        speeds.sort(Comparator.reverseOrder());

        double total = 0.0;
        double weight = 1.0;
        for (double speed : speeds) {
            total += (speed - 1.0) * weight;
            weight *= DIMINISHING_RETURNS_FACTOR;
        }

        return (long) total;
    }

    private void endSession(ServerLevel level, BlockPos source) {
        activeSessions.remove(source);
        setActivatedState(level, source, false);
    }

    /**
     * Plays the sound/particle burst for a stop or abort. Fires immediately
     * when {@link #requestStop} is called — NOT when the inertia coast
     * actually finishes — so the effect reads as "the moment it started
     * winding down," with the lingering speed as the visual follow-through.
     */
    private void playStopEffects(ServerLevel level, BlockPos source, boolean aborted) {
        double cx = source.getX() + 0.5;
        double cy = source.getY() + 1.0;
        double cz = source.getZ() + 0.5;

        if (aborted) {
            // TODO: swap for CoralineSounds.CENTRIFUGE_ABORT.get() once you
            // register it. Placeholder mirrors BEACON_DEACTIVATE but at a
            // lower pitch to read as more "abrupt"/alarming than a normal stop.
            level.playSound(
                    null,
                    source,
                    SoundEvents.BEACON_DEACTIVATE, // TODO: CoralineSounds.CENTRIFUGE_ABORT.get()
                    SoundSource.BLOCKS,
                    1.0f,
                    0.7f
            );

            // Sharper, more chaotic burst to sell "abruptly killed by redstone".
            level.sendParticles(
                    ParticleTypes.SMOKE,
                    cx, cy, cz,
                    40, 0.5, 0.5, 0.5, 0.08
            );
            level.sendParticles(
                    ParticleTypes.CRIT,
                    cx, cy, cz,
                    25, 0.4, 0.4, 0.4, 0.15
            );
        } else {
            level.playSound(
                    null,
                    source,
                    SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.BLOCKS,
                    1.0f,
                    1.0f
            );

            level.sendParticles(
                    ParticleTypes.PORTAL,
                    cx, cy, cz,
                    60, 0.4, 0.4, 0.4, 0.1
            );

            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    cx, cy, cz,
                    20, 0.3, 0.3, 0.3, 0.05
            );
        }
    }

    private void setActivatedState(ServerLevel level, BlockPos pos, boolean activated) {
        BlockState current = level.getBlockState(pos);
        if (current.hasProperty(CentrifugeBlock.ACTIVATED)) {
            level.setBlock(pos, current.setValue(CentrifugeBlock.ACTIVATED, activated), 3);
        }
    }

    private void broadcastTime(ServerLevel level) {
        ClientboundSetTimePacket packet = new ClientboundSetTimePacket(
                level.getGameTime(),
                level.getDayTime(),
                level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)
        );
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }

    /** Full persist + setDirty(), used on start/stop/end — structural changes. */
    private void persistAll() {
        savedData.setSessions(snapshotSessions());
    }

    /** Persist current progress WITHOUT forcing setDirty() — relies on auto-save cadence. */
    private void persistAllQuiet() {
        savedData.setSessionsQuiet(snapshotSessions());
    }

    private List<CentrifugeAccelerationData.SavedSession> snapshotSessions() {
        List<CentrifugeAccelerationData.SavedSession> snapshot = new ArrayList<>(activeSessions.size());
        for (Map.Entry<BlockPos, TimeAccelerationSession> entry : activeSessions.entrySet()) {
            TimeAccelerationSession s = entry.getValue();
            snapshot.add(new CentrifugeAccelerationData.SavedSession(
                    entry.getKey(),
                    s.getTicksRemaining(),
                    s.isStopping(),
                    s.getStopTicksRemaining(),
                    s.getFrozenSpeed()
            ));
        }
        return snapshot;
    }
}