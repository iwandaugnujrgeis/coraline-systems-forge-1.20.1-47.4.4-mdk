package net.zharok01.coralinesystems.client.fog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

/**
 * Tracks a smoothed 0..1 intensity value for the morning fog effect.
 *
 * Day-time curve (ticks out of 24000):
 *   22000–24000  pre-dawn build-up  (0 → 1 over 2000 ticks)
 *   0–1500       sunrise peak       (stays at 1)
 *   1500–6500    morning burn-off   (1 → 0 over 5000 ticks)
 *   6500–22000   daytime / night    (0)
 *
 * The raw curve target is then smoothed with a slow lerp so the fog
 * fades in and out gradually rather than snapping to the time value.
 */
public class MorningFogManager {

    /**
     * How much fog end is reduced at peak intensity.
     * 0.35 = the fog wall is at 35 % of render distance at full morning fog.
     * Raise toward 1.0 for lighter haze, lower toward 0.1 for thick mist.
     */
    public static final float MORNING_FOG_END   = 0.45F;
    public static final float MORNING_FOG_START = 0.05F;

    /** Lerp speed toward the target intensity — slow build / slow burn-off. */
    private static final float LERP_SPEED = 0.008F;

    private static float previousIntensity = 0F;
    private static float currentIntensity  = 0F;

    // -------------------------------------------------------------------------

    /**
     * Must be called once per client tick (from CoralineFogEvents.clientTick).
     */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        // Don't show morning fog underground — mirrors FLGN's underground check
        if (mc.gameRenderer.getMainCamera().getPosition().y < level.getSeaLevel() - 10) {
            previousIntensity = currentIntensity;
            currentIntensity = Mth.lerp(LERP_SPEED, currentIntensity, 0F);
            return;
        }

        float target = computeTarget(level);

        previousIntensity = currentIntensity;
        currentIntensity  = Mth.lerp(LERP_SPEED, currentIntensity, target);
    }

    /** Returns the smoothed intensity interpolated to the current partial tick. */
    public static float getIntensity(float partialTick) {
        return Mth.lerp(partialTick, previousIntensity, currentIntensity);
    }

    // -------------------------------------------------------------------------

    private static float computeTarget(ClientLevel level) {
        long raw = level.getDayTime() % 24000L;

        // Pre-dawn build-up (22000 → 24000)
        if (raw >= 22000L) {
            return (raw - 22000L) / 2000.0F;
        }
        // Sunrise peak (0 → 1500)
        if (raw < 1500L) {
            return 1.0F;
        }
        // Morning burn-off (1500 → 1600)
        if (raw < 1600L) {
            return 1.0F - (raw - 1500L) / 100.0F;
        }
        // Rest of day / night
        return 0.0F;
    }
}