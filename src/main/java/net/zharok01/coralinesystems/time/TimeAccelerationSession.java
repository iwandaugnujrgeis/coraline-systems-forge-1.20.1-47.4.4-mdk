package net.zharok01.coralinesystems.time;

/**
 * Holds the state of a single Centrifuge time-acceleration session.
 *
 * A session moves through three phases:
 *  - RAMPING_UP / AT_PEAK / RAMPING_DOWN (the original cosine-eased curve,
 *    driven entirely by ticksRemaining as before)
 *  - STOPPING: entered early via {@link #beginStopping()} when the player
 *    manually deactivates OR Redstone aborts the session. The speed is
 *    frozen at whatever it was the instant stopping began, held for
 *    {@link #STOP_INERTIA_TICKS} (~1 second), then the session expires.
 *    This creates the "inertia" effect — the abort/stop sound and particles
 *    fire immediately, but the time-skip visibly continues for a beat
 *    before actually cutting off.
 */
public class TimeAccelerationSession {

    public static final int    DURATION_TICKS      = 800;
    public static final double PEAK_SPEED           = 10.0;
    public static final int    RAMP_TICKS           = 40;

    /** How long the frozen "coasting" phase lasts after a stop/abort is requested. */
    public static final int    STOP_INERTIA_TICKS  = 20; // 1 second at 20 TPS

    private int ticksRemaining;

    /** True once beginStopping() has been called — session is coasting to a halt. */
    private boolean stopping = false;

    /** Ticks left in the inertia coast-down. Only meaningful while stopping. */
    private int stopTicksRemaining = 0;

    /** Speed frozen at the moment stopping began. Only meaningful while stopping. */
    private double frozenSpeed = 1.0;

    /** Fresh session — starts at full duration. */
    public TimeAccelerationSession() {
        this.ticksRemaining = DURATION_TICKS;
    }

    /**
     * Restore constructor — resumes a session at an arbitrary point in its
     * timeline. Used by {@link TimeAccelerationManager#tryRestoreSession}.
     *
     * @param ticksRemaining the remaining ticks loaded from saved data
     */
    public TimeAccelerationSession(int ticksRemaining) {
        this.ticksRemaining = ticksRemaining;
    }

    /**
     * Full restore constructor, used when reloading a session that was
     * already mid-stop when the world was last saved.
     */
    public TimeAccelerationSession(int ticksRemaining, boolean stopping, int stopTicksRemaining, double frozenSpeed) {
        this.ticksRemaining = ticksRemaining;
        this.stopping = stopping;
        this.stopTicksRemaining = stopTicksRemaining;
        this.frozenSpeed = frozenSpeed;
    }

    /**
     * Called by the manager the instant a manual stop or Redstone abort is
     * requested. Freezes the current speed and starts the inertia countdown.
     * Safe to call multiple times — only the first call has an effect.
     */
    public void beginStopping() {
        if (stopping) {
            return;
        }
        stopping = true;
        frozenSpeed = getCurrentSpeed();
        stopTicksRemaining = STOP_INERTIA_TICKS;
    }

    public boolean isStopping() {
        return stopping;
    }

    /**
     * Returns the current time-speed multiplier based on where in the session
     * we are. Cosine easing is applied during ramp-up and ramp-down phases.
     * While stopping, returns the frozen speed instead.
     */
    public double getCurrentSpeed() {
        if (stopping) {
            return frozenSpeed;
        }

        int elapsed = DURATION_TICKS - ticksRemaining;

        if (elapsed < RAMP_TICKS) {
            double t = (double) elapsed / RAMP_TICKS;
            return 1.0 + (PEAK_SPEED - 1.0) * (1.0 - Math.cos(t * Math.PI)) / 2.0;
        }

        if (ticksRemaining < RAMP_TICKS) {
            double t = (double) (RAMP_TICKS - ticksRemaining) / RAMP_TICKS;
            return PEAK_SPEED - (PEAK_SPEED - 1.0) * (1.0 - Math.cos(t * Math.PI)) / 2.0;
        }

        return PEAK_SPEED;
    }

    /**
     * Advances by one tick.
     * @return true if still running (either normal phase or coasting to a stop)
     */
    public boolean tick() {
        if (stopping) {
            stopTicksRemaining--;
            return stopTicksRemaining > 0;
        }

        ticksRemaining--;
        return ticksRemaining > 0;
    }

    public boolean isExpired() {
        if (stopping) {
            return stopTicksRemaining <= 0;
        }
        return ticksRemaining <= 0;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public int getStopTicksRemaining() {
        return stopTicksRemaining;
    }

    public double getFrozenSpeed() {
        return frozenSpeed;
    }
}