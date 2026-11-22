package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSoundInstance.class)
public interface AbstractSoundInstanceAccessor {

	@Accessor("pitch")
	void setPitch(float pitch);

	@Accessor("random")
	RandomSource getRandom();

}