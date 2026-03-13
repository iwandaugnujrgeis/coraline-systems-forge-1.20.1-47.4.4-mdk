package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin extends Entity {

	@Unique private static final int MORNING_END = 6000;

	public MobMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
	private void suppressSunburnDuringMorningFog(CallbackInfoReturnable<Boolean> cir) {
		long dayTime = this.level().getDayTime() % 24000L;
		if (dayTime < MORNING_END) {
			cir.setReturnValue(false);
		}
	}

}
