package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.OptionEnum;
import net.zharok01.coralinesystems.client.audio.MusicFrequency;
import net.zharok01.coralinesystems.client.audio.MusicFrequencySettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends Screen {

    protected SoundOptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void coraline_addMusicFrequencyButton(CallbackInfo ci) {
        CycleButton<MusicFrequency> button = CycleButton
                .<MusicFrequency>builder(OptionEnum::getCaption)
                .withValues(Arrays.asList(MusicFrequency.values()))
                .withInitialValue(MusicFrequencySettings.INSTANCE.get())
                .create(
                        this.width / 2 - 100,
                        this.height / 6 + 24 * 4,
                        200,
                        20,
                        Component.translatable("options.coraline_systems.music_frequency"),
                        (btn, frequency) -> {
                            MusicFrequencySettings.INSTANCE.set(frequency);
                            assert this.minecraft != null;
                            this.minecraft.options.save();
                        }
                );

        this.addRenderableWidget(button);
    }
}