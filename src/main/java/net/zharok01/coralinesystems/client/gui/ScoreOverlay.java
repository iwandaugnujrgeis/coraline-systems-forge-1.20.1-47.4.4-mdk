package net.zharok01.coralinesystems.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.zharok01.coralinesystems.registry.CoralineSounds;

public class ScoreOverlay {
    private static int displayScore = 0;
    private static long startTime = -1;
    private static final long DURATION = 5000L;

    public static void trigger(int score) {
        displayScore = score;
        startTime = System.currentTimeMillis();

        // Re-integrating the sound trigger from the original ScoreToast logic
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(CoralineSounds.SPECIAL_SCORE.get(), 1.0F));
        }
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        if (startTime == -1) return;

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > DURATION) {
            startTime = -1;
            return;
        }

        // Smoother Animation: Using a Sine curve for easing instead of linear visibility
        float progress = 1.0F;
        if (elapsed < 600L) {
            // Smoothly slide in from the top
            progress = Mth.sin(((float)elapsed / 600.0F) * (float)Math.PI / 2.0F);
        } else if (elapsed > DURATION - 600L) {
            // Smoothly slide back up
            progress = Mth.sin(((float)(DURATION - elapsed) / 600.0F) * (float)Math.PI / 2.0F);
        }

        // Calculate Y offset: -20 is off-screen, 1 is your requested offset
        float yOffset = Mth.lerp(progress, -20.0F, 1.0F);

        Minecraft mc = Minecraft.getInstance();
        Component text = Component.translatable("deathScreen.score")
                .append(": ")
                .append(Component.literal(String.valueOf(displayScore)).withStyle(ChatFormatting.YELLOW));

        // Draw with 1-pixel X offset and the animated Y offset
        graphics.drawString(mc.font, text, 1, (int)yOffset, 0xFFFFFF, true);
    }
}