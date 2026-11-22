package net.zharok01.coralinesystems.client.gui.compat.controlify;

import dev.isxander.controlify.font.BindingFontHelper;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class CoralineHintRenderer {
    public static final int MARGIN = 6;
    private static final int ICON_LABEL_GAP = 4;
    private static final int ROW_SPACING = 3;
    private static final int BACKGROUND_COLOR = 0x80000000;

    public record Hint(Component icon, Component label) {
    }

    public static void coraline$draw(GuiGraphics guiGraphics, List<Hint> hints, int startY) {
        Font font = Minecraft.getInstance().font;
        int y = startY;

        for (Hint hint : hints) {
            int iconHeight = BindingFontHelper.getComponentHeight(font, hint.icon());
            int rowHeight = Math.max(iconHeight, font.lineHeight);
            int textY = y + iconHeight / 2 - font.lineHeight / 2;
            int labelX = MARGIN + font.width(hint.icon()) + ICON_LABEL_GAP;

            guiGraphics.fill(MARGIN - 2, y - 1, labelX + font.width(hint.label()) + 2, y + rowHeight + 1, BACKGROUND_COLOR);
            guiGraphics.drawString(font, hint.icon(), MARGIN, textY, -1, false);
            guiGraphics.drawString(font, hint.label(), labelX, textY, -1, false);
            y += rowHeight + ROW_SPACING;
        }
    }
}