package net.zharok01.coralinesystems.mixin.sounds;

import net.minecraft.client.sounds.MusicManager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.zharok01.coralinesystems.client.audio.MusicFrequency;
import net.zharok01.coralinesystems.client.audio.MusicFrequencySettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public class MusicManagerMixin {

    @Shadow private int nextSongDelay;
    @Shadow @Final private RandomSource random;

    @Inject(method = "tick", at = @At("HEAD"))
    private void coraline_applyMusicFrequency(CallbackInfo ci) {
        MusicFrequency frequency = MusicFrequencySettings.INSTANCE.get();
        if (frequency == MusicFrequency.RESERVED) return;

        if (this.nextSongDelay > frequency.maxDelay) {
            this.nextSongDelay = Mth.nextInt(this.random, frequency.minDelay, frequency.maxDelay);
        }
    }
}