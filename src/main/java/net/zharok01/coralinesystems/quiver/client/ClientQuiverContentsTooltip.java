package net.zharok01.coralinesystems.quiver.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.quiver.QuiverContents;
import net.zharok01.coralinesystems.quiver.QuiverContentsTooltip;
import org.violetmoon.quark.content.client.tooltip.ShulkerBoxTooltips;

@OnlyIn(Dist.CLIENT)
public class ClientQuiverContentsTooltip implements ClientTooltipComponent {
    private static final int FRAME_PADDING = 5;
    private static final int SLOT_SIZE = 18;
    private static final int UNTINTED = -1;

    private final NonNullList<ItemStack> contents;

    public ClientQuiverContentsTooltip(QuiverContentsTooltip tooltip) {
        this.contents = tooltip.contents();
    }

    @Override
    public int getHeight() {
        return FRAME_PADDING * 2 + SLOT_SIZE + 1;
    }

    @Override
    public int getWidth(Font font) {
        return FRAME_PADDING * 2 + SLOT_SIZE * QuiverContents.SLOT_COUNT;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int frameY = y - 1;
        ShulkerBoxTooltips.ShulkerComponent.renderTooltipBackground(guiGraphics, Minecraft.getInstance(), guiGraphics.pose(), x, frameY, QuiverContents.SLOT_COUNT, 1, UNTINTED);
        for (int slot = 0; slot < this.contents.size(); slot++) {
            ItemStack stack = this.contents.get(slot);
            int slotX = x + FRAME_PADDING + 1 + slot * SLOT_SIZE;
            int slotY = frameY + FRAME_PADDING + 1;
            guiGraphics.renderItem(stack, slotX, slotY);
            guiGraphics.renderItemDecorations(font, stack, slotX, slotY);
        }
    }
}
