package net.zharok01.coralinesystems.client.color;

public final class CoralineTintUtils {

    private CoralineTintUtils() {
    }

    public static int lerpStrength(int startRgb, int endRgb, int strength) {
        int clamped = Math.max(1, Math.min(5, strength));
        // 1 -> t=0.0, 5 -> t=1.0
        float t = (clamped - 1) / 4.0F;

        int sr = (startRgb >> 16) & 0xFF;
        int sg = (startRgb >> 8) & 0xFF;
        int sb = startRgb & 0xFF;

        int er = (endRgb >> 16) & 0xFF;
        int eg = (endRgb >> 8) & 0xFF;
        int eb = endRgb & 0xFF;

        int r = Math.round(sr + (er - sr) * t);
        int g = Math.round(sg + (eg - sg) * t);
        int b = Math.round(sb + (eb - sb) * t);

        return (r << 16) | (g << 8) | b;
    }
}