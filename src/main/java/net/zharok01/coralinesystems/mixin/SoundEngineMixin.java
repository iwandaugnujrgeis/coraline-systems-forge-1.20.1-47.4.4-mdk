package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {

	@Unique private static final Random RANDOM = new Random();
	@Unique private static final float VARIANCE = 0.1f;

	@Inject(
			method = "play",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getSound()Lnet/minecraft/client/resources/sounds/Sound;"
			)
	)
	private void randomizeMusicPitch(SoundInstance sound, CallbackInfo ci) {
		if (sound.getSource() == SoundSource.MUSIC && sound instanceof AbstractSoundInstance abstractSound) {
			float pitch = abstractSound.getPitch();
			pitch += RANDOM.nextFloat(VARIANCE * 2) - VARIANCE;
			((AbstractSoundInstanceAccessor)abstractSound).setPitch(pitch);
		}
	}

}
