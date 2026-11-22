package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TransferableSelectionList.class)
public class TransferableSelectionListMixin {
    
    @Redirect(
            method = "renderHeader",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/MutableComponent;withStyle([Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"
            )
    )
    private MutableComponent coraline$systems$removeHeaderFormatting(MutableComponent instance, ChatFormatting[] formats) {
        return instance;
    }
}