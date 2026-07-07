package net.zharok01.coralinesystems.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

/**
 * Persists the active Centrifuge time-acceleration session to the world's
 * data folder (data/coraline_acceleration_data.dat on the overworld).
 *
 * Survives world reload, server restart, and singleplayer quit-to-menu,
 * closing the loophole where a player could log out to freeze the session.
 */
public class CentrifugeAccelerationData extends SavedData {

    private static final String IDENTIFIER = "coraline_acceleration_data";

    // NBT keys
    private static final String KEY_ACTIVE          = "SessionActive";
    private static final String KEY_TICKS_REMAINING = "TicksRemaining";
    private static final String KEY_SOURCE_X        = "SourceX";
    private static final String KEY_SOURCE_Y        = "SourceY";
    private static final String KEY_SOURCE_Z        = "SourceZ";

    private boolean sessionActive   = false;
    private int     ticksRemaining  = 0;
    @Nullable
    private BlockPos sessionSource  = null;

    // -------------------------------------------------------------------------
    // Access
    // -------------------------------------------------------------------------

    /**
     * Retrieves (or creates) the data instance from the overworld's storage.
     * Always stored on the overworld regardless of which dimension calls this,
     * mirroring the pattern used by {@link CoralineWorldData}.
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

    /** @return true if a session was active when the world was last saved */
    public boolean isSessionActive() {
        return sessionActive;
    }

    /** @return the remaining ticks of the saved session, or 0 if none */
    public int getTicksRemaining() {
        return ticksRemaining;
    }

    /** @return the BlockPos of the Centrifuge that started the session, or null */
    @Nullable
    public BlockPos getSessionSource() {
        return sessionSource;
    }

    /**
     * Records a newly started session and marks the data dirty for the next
     * auto-save flush.
     */
    public void onSessionStart(int totalTicks, BlockPos source) {
        sessionActive  = true;
        ticksRemaining = totalTicks;
        sessionSource  = source.immutable();
        setDirty();
    }

    /**
     * Called once per server tick to keep {@code ticksRemaining} current.
     * Does NOT call {@link #setDirty()} — we rely on the auto-save cadence
     * (every 6000 ticks by default) to flush mid-session progress. The worst
     * case on a crash is resuming a few seconds earlier, which is acceptable.
     */
    public void onSessionTick(int remaining) {
        ticksRemaining = remaining;
    }

    /**
     * Records session end and marks dirty so the cleared state is flushed
     * before the world closes.
     */
    public void onSessionEnd() {
        sessionActive  = false;
        ticksRemaining = 0;
        sessionSource  = null;
        setDirty();
    }

    // -------------------------------------------------------------------------
    // Serialisation
    // -------------------------------------------------------------------------

    public static CentrifugeAccelerationData load(CompoundTag tag) {
        CentrifugeAccelerationData data = new CentrifugeAccelerationData();
        data.sessionActive  = tag.getBoolean(KEY_ACTIVE);
        data.ticksRemaining = tag.getInt(KEY_TICKS_REMAINING);

        if (data.sessionActive) {
            data.sessionSource = new BlockPos(
                    tag.getInt(KEY_SOURCE_X),
                    tag.getInt(KEY_SOURCE_Y),
                    tag.getInt(KEY_SOURCE_Z)
            );
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean(KEY_ACTIVE,          sessionActive);
        tag.putInt(KEY_TICKS_REMAINING,     ticksRemaining);

        if (sessionSource != null) {
            tag.putInt(KEY_SOURCE_X, sessionSource.getX());
            tag.putInt(KEY_SOURCE_Y, sessionSource.getY());
            tag.putInt(KEY_SOURCE_Z, sessionSource.getZ());
        }

        return tag;
    }
}