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

    // OPTIMIZATION: Cache the text so we don't rebuild it 60 times a second
    private static Component cachedText = null;

    public static void trigger(int score) {
        displayScore = score;
        startTime = System.currentTimeMillis();

        // Build the component exactly ONCE when triggered
        cachedText = Component.translatable("deathScreen.score")
                .append(": ")
                .append(Component.literal(String.valueOf(displayScore)).withStyle(ChatFormatting.YELLOW));

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(CoralineSounds.SPECIAL_SCORE.get(), 1.0F));
        }
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        // Exit early if not triggered or if the text failed to cache
        if (startTime == -1 || cachedText == null) return;

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > DURATION) {
            startTime = -1;
            cachedText = null; // Clear from memory when done
            return;
        }

        float progress = 1.0F;
        if (elapsed < 600L) {
            // ENTRANCE: Ease Out (Sine wave) - Starts fast, slows down into position
            progress = Mth.sin(((float)elapsed / 600.0F) * (float)Math.PI / 2.0F);
        } else if (elapsed > DURATION - 600L) {
            // EXIT: Ease In (Quadratic) - Starts slow, accelerates off-screen
            float exitTime = (float)(elapsed - (DURATION - 600L)) / 600.0F;
            progress = 1.0F - (exitTime * exitTime);
        }

        // Apply your requested 3-pixel Y offset
        float yOffset = Mth.lerp(progress, -20.0F, 3.0F);

        Minecraft mc = Minecraft.getInstance();

        // Draw the cached text instead of building a new one
        graphics.drawString(mc.font, cachedText, 1, (int)yOffset, 0xFFFFFF, true);
    }
}