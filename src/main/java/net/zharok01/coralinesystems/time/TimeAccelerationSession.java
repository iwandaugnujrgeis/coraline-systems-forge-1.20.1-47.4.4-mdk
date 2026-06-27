package net.zharok01.coralinesystems.time;

/**
 * Holds the state of a single Centrifuge time-acceleration session.
 *
 * Duration is measured in server ticks. At 2× peak speed the session
 * advances 8000 in-game ticks over 4000 server ticks (200 s at 20 TPS).
 */
public class TimeAccelerationSession {

    /** Total server ticks this session runs for. */
    public static final int DURATION_TICKS = 800;
    /** Peak time-speed multiplier (2 = twice vanilla speed). */
    public static final double PEAK_SPEED = 10.0;
    /** Server ticks spent easing in and easing out. */
    public static final int RAMP_TICKS = 40;

    private int ticksRemaining = DURATION_TICKS;

    /**
     * Returns the current time-speed multiplier for this tick, based on
     * where in the session we are.
     *
     * During ramp-up and ramp-down a cosine ease is applied so the
     * acceleration and deceleration feel smooth rather than instant.
     *
     * @return a multiplier ≥ 1.0 and ≤ {@link #PEAK_SPEED}
     */
    public double getCurrentSpeed() {
        int elapsed = DURATION_TICKS - ticksRemaining;

        if (elapsed < RAMP_TICKS) {
            // Ramp-up: t goes from 0 → 1
            double t = (double) elapsed / RAMP_TICKS;
            return 1.0 + (PEAK_SPEED - 1.0) * (1.0 - Math.cos(t * Math.PI)) / 2.0;
        }

        if (ticksRemaining < RAMP_TICKS) {
            // Ramp-down: t goes from 0 → 1
            double t = (double) (RAMP_TICKS - ticksRemaining) / RAMP_TICKS;
            return PEAK_SPEED - (PEAK_SPEED - 1.0) * (1.0 - Math.cos(t * Math.PI)) / 2.0;
        }

        return PEAK_SPEED;
    }

    /**
     * Advances the session by one server tick.
     * @return true if the session is still running, false if it has expired
     */
    public boolean tick() {
        ticksRemaining--;
        return ticksRemaining > 0;
    }

    /** @return true if the session has no ticks remaining */
    public boolean isExpired() {
        return ticksRemaining <= 0;
    }

    /** @return remaining server ticks, for any UI or debugging purposes */
    public int getTicksRemaining() {
        return ticksRemaining;
    }
}