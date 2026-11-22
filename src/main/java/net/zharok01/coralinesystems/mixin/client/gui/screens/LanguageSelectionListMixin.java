package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.zharok01.coralinesystems.registry.CoralineLanguages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Mixin(targets = "net.minecraft.client.gui.screens.LanguageSelectScreen$LanguageSelectionList")
public class LanguageSelectionListMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/language/LanguageManager;getLanguages()Ljava/util/SortedMap;"
            )
    )
    private SortedMap<String, LanguageInfo> coraline$filterSupportedLanguages(LanguageManager instance) {
        SortedMap<String, LanguageInfo> allLanguages = instance.getLanguages();
        SortedMap<String, LanguageInfo> filteredLanguages = new TreeMap<>();

        for (Map.Entry<String, LanguageInfo> entry : allLanguages.entrySet()) {
            if (CoralineLanguages.isLanguageSupported(entry.getKey())) {
                filteredLanguages.put(entry.getKey(), entry.getValue());
            }
        }

        return filteredLanguages;
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ObjectSelectionList;<init>(Lnet/minecraft/client/Minecraft;IIIII)V"
            ),
            index = 4
    )
    private static int coraline$extendListBottomMargin(int originalY1) {
        return originalY1 + 29;
    }
}