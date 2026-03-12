package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MusicManager.class)
public class MusicManagerMixin {

    @Unique private static final int DELAY = 300;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 1))
    private int changeMusicDelay(int nextSongDelay, int maxDelay) {
        return Math.min(nextSongDelay, DELAY * 20);
    }
}