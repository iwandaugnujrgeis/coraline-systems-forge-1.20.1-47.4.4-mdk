package net.zharok01.coralinesystems.client.fog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MorningFogManager {

    public static final float MORNING_FOG_END = 0.45F;
    public static final float MORNING_FOG_START = 0.05F;

    private static final float LERP_SPEED = 0.008F;

    @SuppressWarnings("removal")
    public static final ResourceLocation SKYLANDS_DIMENSION = new ResourceLocation("rediscovered", "skylands");

    private static float previousIntensity = 0F;
    private static float currentIntensity = 0F;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        boolean inSkylands = level.dimension().location().equals(SKYLANDS_DIMENSION);
        if (!inSkylands && mc.gameRenderer.getMainCamera().getPosition().y < level.getSeaLevel() - 10) {
            previousIntensity = currentIntensity;
            currentIntensity = Mth.lerp(LERP_SPEED, currentIntensity, 0F);
            return;
        }

        long raw = level.getDayTime() % 24000L;
        float target;
        if (raw >= 22000L) {
            target = (raw - 22000L) / 2000.0F;
        } else if (raw < 1500L) {
            target = 1.0F;
        } else if (raw < 1600L) {
            target = 1.0F - (raw - 1500L) / 100.0F;
        } else {
            target = 0.0F;
        }

        previousIntensity = currentIntensity;
        currentIntensity = Mth.lerp(LERP_SPEED, currentIntensity, target);
    }

    public static float getIntensity(float partialTick) {
        return Mth.lerp(partialTick, previousIntensity, currentIntensity);
    }
}