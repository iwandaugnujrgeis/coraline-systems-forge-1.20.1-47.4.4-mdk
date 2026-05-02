package net.zharok01.coralinesystems.client.fog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public class MorningFogManager {

    public static final float MORNING_FOG_END   = 0.45F;
    public static final float MORNING_FOG_START = 0.05F;

    /** Lerp speed toward the target intensity — slow build / slow burn-off. */
    private static final float LERP_SPEED = 0.008F;

    private static float previousIntensity = 0F;
    private static float currentIntensity  = 0F;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        // Don't show morning fog underground!
        if (mc.gameRenderer.getMainCamera().getPosition().y < level.getSeaLevel() - 10) {
            previousIntensity = currentIntensity;
            currentIntensity = Mth.lerp(LERP_SPEED, currentIntensity, 0F);
            return;
        }

        float target = computeTarget(level);

        previousIntensity = currentIntensity;
        currentIntensity  = Mth.lerp(LERP_SPEED, currentIntensity, target);
    }

    public static float getIntensity(float partialTick) {
        return Mth.lerp(partialTick, previousIntensity, currentIntensity);
    }

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