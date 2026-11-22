package net.zharok01.coralinesystems.mixin.sounds;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.zharok01.coralinesystems.mixin.accessor.AbstractSoundInstanceAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {

	@Unique private static final Random coraline$random = new Random();

	@Unique private static final float MUSIC_PITCH_DROP = 0.16f;

	@Inject(
			method = "play",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getSound()Lnet/minecraft/client/resources/sounds/Sound;"
			)
	)
	private void randomizeMusicPitch(SoundInstance sound, CallbackInfo ci) {
		if (sound.getSource() == SoundSource.MUSIC && sound instanceof AbstractSoundInstance abstractSound) {
			float pitch = abstractSound.getPitch() - coraline$random.nextFloat() * MUSIC_PITCH_DROP;
			((AbstractSoundInstanceAccessor)abstractSound).setPitch(pitch);
		}
	}

}
