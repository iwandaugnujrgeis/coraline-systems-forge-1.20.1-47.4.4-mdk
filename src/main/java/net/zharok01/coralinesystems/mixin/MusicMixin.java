package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Music.class)
public class MusicMixin {

    @Unique private static final int MIN_DELAY = 2400;
    @Unique
    private static final int MAX_DELAY = 3000;

    @Shadow @Mutable
    @Final
    private int minDelay;
    @Shadow
    @Mutable @Final private int maxDelay;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Holder<SoundEvent> holder, int i, int p, boolean b, CallbackInfo ci) {
        this.minDelay = MIN_DELAY;
        this.maxDelay = MAX_DELAY;
    }
}