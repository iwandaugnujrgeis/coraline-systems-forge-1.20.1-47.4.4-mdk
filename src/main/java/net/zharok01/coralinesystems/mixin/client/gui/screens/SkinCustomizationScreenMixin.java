package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import net.zharok01.coralinesystems.network.ServerboundSelectSkinPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;
import net.zharok01.coralinesystems.client.entity.skin.CoralineSkinConfig;
import net.zharok01.coralinesystems.client.entity.skin.SkinVariant;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(SkinCustomizationScreen.class)
public abstract class SkinCustomizationScreenMixin extends Screen {

    protected SkinCustomizationScreenMixin(@NotNull Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void coraline_addSkinSelector(CallbackInfo ci) {
        List<SkinVariant> available = Arrays.asList(SkinVariant.values());
        SkinVariant current = CoralineSkinConfig.getSelectedVariant();

        int ourButtonY = this.height / 6 + 24 * 4;

        CycleButton<SkinVariant> button = CycleButton
                .builder((SkinVariant variant) -> Component.translatable(variant.getTranslationKey()))
                .withValues(available)
                .withInitialValue(current)
                .create(
                        this.width / 2 - 100,
                        ourButtonY,
                        200,
                        20,
                        Component.translatable("options.coraline_systems.skin_selector"),
                        (btn, variant) -> coraline_onSkinSelected(variant)
                );

        this.addRenderableWidget(button);
    }

    @Unique
    private void coraline_onSkinSelected(SkinVariant variant) {
        CoralineSkinConfig.setSelectedVariant(variant);

        if (Minecraft.getInstance().getConnection() != null) {
            CoralinePacketHandler.sendToServer(new ServerboundSelectSkinPacket(variant));
        }
    }
}