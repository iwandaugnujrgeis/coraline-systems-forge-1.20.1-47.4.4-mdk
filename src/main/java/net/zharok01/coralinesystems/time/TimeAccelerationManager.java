// net/zharok01/coralinesystems/time/TimeAccelerationManager.java
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
import net.zharok01.coralinesystems.util.CentrifugeAccelerationData;
import org.jetbrains.annotations.Nullable;

public class TimeAccelerationManager {

    @Nullable
    private TimeAccelerationSession activeSession = null;

    @Nullable
    private BlockPos sessionSource = null;

    /** The SavedData instance for this world — fetched once on construction. */
    private final CentrifugeAccelerationData savedData;

    /**
     * Created by {@link CentrifugeEvent}
     * on world load. Immediately restores any session that was active when the
     * world was last saved.
     *
     * @param level the overworld ServerLevel
     */
    public TimeAccelerationManager(ServerLevel level) {
        this.savedData = CentrifugeAccelerationData.get(level);
        tryRestoreSession(level);
    }

    /**
     * Reconstructs an in-progress session from saved data.
     * Called once during construction — no sound or particles on restore,
     * since those are first-activation events only.
     */
    private void tryRestoreSession(ServerLevel level) {
        if (!savedData.isSessionActive()) {
            return;
        }

        int remaining = savedData.getTicksRemaining();
        BlockPos source = savedData.getSessionSource();

        if (remaining <= 0 || source == null) {
            // Saved state is invalid — clear it and stay idle.
            savedData.onSessionEnd();
            return;
        }

        activeSession = new TimeAccelerationSession(remaining);
        sessionSource = source;

        // Re-apply the activated blockstate in case the chunk has loaded
        // but the blockstate wasn't persisted correctly (e.g. mid-crash).
        setActivatedState(level, sessionSource, true);
    }

    /**
     * Starts a new acceleration session. Does nothing if one is already running.
     *
     * @param level     the overworld ServerLevel
     * @param sourcePos the BlockPos of the Centrifuge that triggered activation
     * @return true if a new session was started
     */
    public boolean startSession(ServerLevel level, BlockPos sourcePos) {
        if (activeSession != null) {
            return false;
        }

        sessionSource = sourcePos.immutable();
        activeSession = new TimeAccelerationSession();

        setActivatedState(level, sessionSource, true);

        // Persist immediately so a crash right after activation still saves.
        savedData.onSessionStart(TimeAccelerationSession.DURATION_TICKS, sessionSource);

        return true;
    }

    /** @return true if a session is currently running */
    public boolean isActive() {
        return activeSession != null;
    }

    /**
     * Called once per server tick for the overworld.
     */
    public void tick(ServerLevel level) {
        if (activeSession == null) {
            return;
        }

        double speed = activeSession.getCurrentSpeed();

        long extraTicks = (long) (speed - 1.0);
        if (extraTicks > 0) {
            level.setDayTime(level.getDayTime() + extraTicks);
        }

        broadcastTime(level);

        boolean stillRunning = activeSession.tick();

        // Update saved progress — no setDirty() here, auto-save handles it.
        savedData.onSessionTick(activeSession.getTicksRemaining());

        if (!stillRunning) {
            onSessionEnd(level);
        }
    }

    private void onSessionEnd(ServerLevel level) {
        if (sessionSource != null) {
            setActivatedState(level, sessionSource, false);

            double cx = sessionSource.getX() + 0.5;
            double cy = sessionSource.getY() + 1.0;
            double cz = sessionSource.getZ() + 0.5;

            level.playSound(
                    null,
                    sessionSource,
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

        // Mark saved data cleared and dirty so it flushes before world closes.
        savedData.onSessionEnd();

        activeSession = null;
        sessionSource = null;
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
}