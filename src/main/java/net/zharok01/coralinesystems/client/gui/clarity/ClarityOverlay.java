package net.zharok01.coralinesystems.client.gui.clarity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.zharok01.coralinesystems.client.gui.score.ScoreOverlay;
import net.zharok01.coralinesystems.registry.CoralineEffects;
import net.zharok01.coralinesystems.util.helper.TextBackgroundHelper;

public class ClarityOverlay {

    private static final long SLIDE_MS = 600L;
    private static final int FONT_LINE_HEIGHT = 9;
    private static final int LINE_SPACING = 2;
    private static final int SCORE_RESTING_Y = 3;

    private enum Phase { HIDDEN, SLIDING_IN, VISIBLE, SLIDING_OUT }

    private static Phase phase = Phase.HIDDEN;
    private static long phaseStartMs = 0L;

    public static boolean isVisible() {
        return phase != Phase.HIDDEN;
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean hasEffect = mc.player.hasEffect(CoralineEffects.CLARITY.get());

        switch (phase) {
            case HIDDEN -> {
                if (hasEffect) {
                    phase = Phase.SLIDING_IN;
                    phaseStartMs = System.currentTimeMillis();
                } else {
                    return;
                }
            }
            case SLIDING_IN -> {
                if (!hasEffect) {
                    phase = Phase.SLIDING_OUT;
                    long elapsed = System.currentTimeMillis() - phaseStartMs;
                    float inProgress = Math.min(1.0F, (float) elapsed / SLIDE_MS);
                    phaseStartMs = System.currentTimeMillis() - (long)((1.0F - inProgress) * SLIDE_MS);
                } else if (System.currentTimeMillis() - phaseStartMs >= SLIDE_MS) {
                    phase = Phase.VISIBLE;
                }
            }
            case VISIBLE -> {
                if (!hasEffect) {
                    phase = Phase.SLIDING_OUT;
                    phaseStartMs = System.currentTimeMillis();
                }
            }
            case SLIDING_OUT -> {
                if (hasEffect) {
                    long elapsed = System.currentTimeMillis() - phaseStartMs;
                    float outProgress = Math.min(1.0F, (float) elapsed / SLIDE_MS);
                    phase = Phase.SLIDING_IN;
                    phaseStartMs = System.currentTimeMillis() - (long)((1.0F - outProgress) * SLIDE_MS);
                } else if (System.currentTimeMillis() - phaseStartMs >= SLIDE_MS) {
                    phase = Phase.HIDDEN;
                    return;
                }
            }
        }

        long elapsed = System.currentTimeMillis() - phaseStartMs;
        float progress = switch (phase) {
            case SLIDING_IN -> Mth.sin((Math.min(1.0F, (float) elapsed / SLIDE_MS)) * (float) Math.PI / 2.0F);
            case VISIBLE -> 1.0F;
            case SLIDING_OUT -> {
                float f = Math.min(1.0F, (float) elapsed / SLIDE_MS);
                yield 1.0F - (f * f);
            }
            default -> 0.0F;
        };

        int restingY = ScoreOverlay.isVisible()
                ? SCORE_RESTING_Y + FONT_LINE_HEIGHT + LINE_SPACING
                : SCORE_RESTING_Y;

        float yOffset = Mth.lerp(progress, -20.0F, (float) restingY);

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        Component coords = Component.literal(String.format("%.1f, %.1f, %.1f", x, y, z));

        TextBackgroundHelper.drawBehindText(graphics, coords, 1, (int) yOffset);
        graphics.drawString(mc.font, coords, 1, (int) yOffset, 0x00FFD4, true);
    }
}