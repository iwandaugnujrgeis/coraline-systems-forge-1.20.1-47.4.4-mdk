package net.zharok01.coralinesystems.util.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class TextBackgroundHelper {
    private static final int RGB_MASK = 0x00FFFFFF;
    private static final int BOX_PADDING = 1;

    private TextBackgroundHelper() {
    }

    public static double getOpacity() {
        return Minecraft.getInstance().options.textBackgroundOpacity().get();
    }

    public static int withOpacity(int argb, double opacity) {
        return (argb & RGB_MASK) | ((int) (opacity * 255.0D) << 24);
    }

    public static int getColor() {
        return withOpacity(0, getOpacity());
    }

    public static int replaceVisibleAlpha(int argb) {
        if ((argb >>> 24) == 0) {
            return argb;
        }
        return withOpacity(argb, getOpacity());
    }

    public static void drawBehindText(GuiGraphics graphics, Component text, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        int textWidth = minecraft.font.width(text);
        int lineHeight = minecraft.font.lineHeight;
        graphics.fill(x - BOX_PADDING, y - BOX_PADDING, x + textWidth + BOX_PADDING, y + lineHeight - BOX_PADDING, getColor());
    }
}