package net.zharok01.coralinesystems.time;

import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Server-side singleton that drives the active {@link TimeAccelerationSession}.
 *
 * Lifecycle:
 *   - Created and stored on world load (see ServerEventListener).
 *   - {@link #tick(ServerLevel)} is called every server tick at LOWEST priority.
 *   - {@link #startSession()} is called by CentrifugeBlockEntity on Orb insertion.
 */
public class TimeAccelerationManager {

    @Nullable
    private TimeAccelerationSession activeSession = null;

    /**
     * Starts a new acceleration session. Does nothing if one is already running.
     * @return true if a new session was started, false if one was already active
     */
    public boolean startSession() {
        if (activeSession != null) {
            return false;
        }
        activeSession = new TimeAccelerationSession();
        return true;
    }

    /** @return true if a session is currently running */
    public boolean isActive() {
        return activeSession != null;
    }

    /**
     * Called once per server tick for the overworld.
     *
     * Advances time by the session's extra ticks (on top of vanilla's own +1),
     * then broadcasts the updated time to all players so their skies update
     * every tick instead of once per second.
     *
     * @param level the overworld ServerLevel
     */
    public void tick(ServerLevel level) {
        if (activeSession == null) {
            return;
        }

        double speed = activeSession.getCurrentSpeed();

        // Vanilla unconditionally adds 1 per tick; we add (speed - 1) on top.
        // Net result: dayTime advances by `speed` ticks this server tick.
        long extraTicks = (long) (speed - 1.0);
        if (extraTicks > 0) {
            level.setDayTime(level.getDayTime() + extraTicks);
        }

        // Broadcast updated time to every player observing this level so the
        // client sky moves smoothly instead of updating once per second.
        broadcastTime(level);

        boolean stillRunning = activeSession.tick();
        if (!stillRunning) {
            activeSession = null;
        }
    }

    /**
     * Sends a {@link ClientboundSetTimePacket} to every player currently in
     * {@code level} or in any level that derives its time from {@code level}
     * (e.g. the Nether and End share the overworld's daylight rule).
     */
    private void broadcastTime(ServerLevel level) {
        ClientboundSetTimePacket packet = new ClientboundSetTimePacket(
                level.getGameTime(),
                level.getDayTime(),
                level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DAYLIGHT)
        );

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }
}