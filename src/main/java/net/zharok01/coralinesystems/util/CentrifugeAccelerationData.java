package net.zharok01.coralinesystems.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Persists ALL currently-active Centrifuge time-acceleration sessions to the
 * world's data folder (data/coraline_acceleration_data.dat on the overworld).
 *
 * Survives world reload, server restart, and singleplayer quit-to-menu,
 * closing the loophole where a player could log out to freeze a session.
 *
 * Supports an arbitrary number of concurrent sessions (one per Centrifuge),
 * each tracked by its source BlockPos.
 */
public class CentrifugeAccelerationData extends SavedData {

    private static final String IDENTIFIER = "coraline_acceleration_data";

    private static final String KEY_SESSIONS = "Sessions";

    private static final String KEY_SOURCE_X          = "SourceX";
    private static final String KEY_SOURCE_Y          = "SourceY";
    private static final String KEY_SOURCE_Z          = "SourceZ";
    private static final String KEY_TICKS_REMAINING   = "TicksRemaining";
    private static final String KEY_STOPPING          = "Stopping";
    private static final String KEY_STOP_TICKS_REMAIN = "StopTicksRemaining";
    private static final String KEY_FROZEN_SPEED      = "FrozenSpeed";

    private List<SavedSession> sessions = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Access
    // -------------------------------------------------------------------------

    /**
     * Retrieves (or creates) the data instance from the overworld's storage.
     * Always stored on the overworld regardless of which dimension calls this.
     */
    public static CentrifugeAccelerationData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(
                        CentrifugeAccelerationData::load,
                        CentrifugeAccelerationData::new,
                        IDENTIFIER
                );
    }

    // -------------------------------------------------------------------------
    // Public API — called by TimeAccelerationManager
    // -------------------------------------------------------------------------

    /** @return an immutable-in-spirit snapshot list of every saved session */
    public List<SavedSession> getSessions() {
        return sessions;
    }

    /**
     * Replaces the full session list and marks dirty immediately. Used for
     * structural changes: session start, stop requested, or session end.
     */
    public void setSessions(List<SavedSession> newSessions) {
        this.sessions = newSessions;
        setDirty();
    }

    /**
     * Replaces the full session list WITHOUT forcing a dirty flag. Used for
     * routine per-tick progress updates — relies on Vanilla's auto-save
     * cadence (every 6000 ticks by default) to flush mid-session progress.
     * The worst case on a crash is resuming a few seconds earlier, which is
     * acceptable — mirrors the original single-session design's tradeoff.
     */
    public void setSessionsQuiet(List<SavedSession> newSessions) {
        this.sessions = newSessions;
    }

    // -------------------------------------------------------------------------
    // Serialisation
    // -------------------------------------------------------------------------

    public static CentrifugeAccelerationData load(CompoundTag tag) {
        CentrifugeAccelerationData data = new CentrifugeAccelerationData();

        ListTag list = tag.getList(KEY_SESSIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag sessionTag = list.getCompound(i);

            BlockPos source = new BlockPos(
                    sessionTag.getInt(KEY_SOURCE_X),
                    sessionTag.getInt(KEY_SOURCE_Y),
                    sessionTag.getInt(KEY_SOURCE_Z)
            );

            int ticksRemaining = sessionTag.getInt(KEY_TICKS_REMAINING);
            boolean stopping = sessionTag.getBoolean(KEY_STOPPING);
            int stopTicksRemaining = sessionTag.getInt(KEY_STOP_TICKS_REMAIN);
            double frozenSpeed = sessionTag.getDouble(KEY_FROZEN_SPEED);

            data.sessions.add(new SavedSession(source, ticksRemaining, stopping, stopTicksRemaining, frozenSpeed));
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();

        for (SavedSession session : sessions) {
            CompoundTag sessionTag = new CompoundTag();
            sessionTag.putInt(KEY_SOURCE_X, session.source().getX());
            sessionTag.putInt(KEY_SOURCE_Y, session.source().getY());
            sessionTag.putInt(KEY_SOURCE_Z, session.source().getZ());
            sessionTag.putInt(KEY_TICKS_REMAINING, session.ticksRemaining());
            sessionTag.putBoolean(KEY_STOPPING, session.stopping());
            sessionTag.putInt(KEY_STOP_TICKS_REMAIN, session.stopTicksRemaining());
            sessionTag.putDouble(KEY_FROZEN_SPEED, session.frozenSpeed());
            list.add(sessionTag);
        }

        tag.put(KEY_SESSIONS, list);
        return tag;
    }

    /**
     * Immutable snapshot of one Centrifuge's session state, used for both
     * in-memory transfer (Manager -> SavedData) and NBT round-tripping.
     */
    public record SavedSession(
            BlockPos source,
            int ticksRemaining,
            boolean stopping,
            int stopTicksRemaining,
            double frozenSpeed
    ) {}
}