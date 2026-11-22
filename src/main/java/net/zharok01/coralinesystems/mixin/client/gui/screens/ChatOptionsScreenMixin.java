package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatOptionsScreen.class)
public class ChatOptionsScreenMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/SimpleOptionsSubScreen;<init>(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Options;Lnet/minecraft/network/chat/Component;[Lnet/minecraft/client/OptionInstance;)V"
            ),
            index = 2
    )
    private static Component coraline$separateTitleString(Component originalTitle) {
        return Component.translatable("options.coraline_systems.chat.title_screen");
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/SimpleOptionsSubScreen;<init>(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Options;Lnet/minecraft/network/chat/Component;[Lnet/minecraft/client/OptionInstance;)V"
            ),
            index = 3
    )
    private static OptionInstance<?>[] coraline$removeChatToggles(OptionInstance<?>[] originalOptions) {
        OptionInstance<?>[] newOptions = new OptionInstance<?>[originalOptions.length - 2];

        System.arraycopy(originalOptions, 0, newOptions, 0, 14);

        newOptions[14] = originalOptions[15];

        return newOptions;
    }
}