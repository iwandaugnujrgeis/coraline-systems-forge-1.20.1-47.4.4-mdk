// net/zharok01/coralinesystems/time/TimeAccelerationSession.java
package net.zharok01.coralinesystems.time;

/**
 * Holds the state of a single Centrifuge time-acceleration session.
 */
public class TimeAccelerationSession {

    public static final int    DURATION_TICKS = 800;
    public static final double PEAK_SPEED     = 10.0;
    public static final int    RAMP_TICKS     = 40;

    private int ticksRemaining;

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
     * Returns the current time-speed multiplier based on where in the session
     * we are. Cosine easing is applied during ramp-up and ramp-down phases.
     */
    public double getCurrentSpeed() {
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

    /** Advances by one tick. @return true if still running */
    public boolean tick() {
        ticksRemaining--;
        return ticksRemaining > 0;
    }

    public boolean isExpired() {
        return ticksRemaining <= 0;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }
}