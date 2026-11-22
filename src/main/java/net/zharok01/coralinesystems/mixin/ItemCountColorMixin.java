package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiGraphics.class)
public abstract class ItemCountColorMixin {

    @Redirect(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"
            )
    )
    private int coraline$colorMaxStackCount(
            GuiGraphics instance, Font font, String text, int x, int y, int color, boolean dropShadow,
            Font pFont, ItemStack pStack, int pX, int pY, String pText
    ) {
        if (pStack.getMaxStackSize() > 1 && pStack.getCount() >= pStack.getMaxStackSize()) {
            color = 0xFFFF00;
        }
        return instance.drawString(font, text, x, y, color, dropShadow);
    }
}