package net.zharok01.coralinesystems.client.gui;

import net.minecraft.ChatFormatting; // Fixed import based on[cite: 24]
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public class ScoreToast implements Toast {
    private final int score;
    private boolean playedSound = false;

    public ScoreToast(int score) {
        this.score = score;
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent component, long timeSinceLastVisible) {
        // Render the standard toast texture background
        graphics.blit(TEXTURE, 0, 0, 0, 0, this.width(), this.height());

        // Play the Coraline special sound only once per toast appearance
        if (!playedSound) {
            component.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(CoralineSounds.SPECIAL_SCORE.get(), 1.0F));
            playedSound = true;
        }

        // Draw the labels using ChatFormatting constants[cite: 24]
        graphics.drawString(component.getMinecraft().font,
                Component.translatable("deathScreen.score").withStyle(ChatFormatting.GOLD), 30, 7, -1);

        graphics.drawString(component.getMinecraft().font,
                Component.literal(String.valueOf(score)).withStyle(ChatFormatting.YELLOW), 30, 18, -1);

        // Slide out after 5 seconds of visibility
        return timeSinceLastVisible >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }
}