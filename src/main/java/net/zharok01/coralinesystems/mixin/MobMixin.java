package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.Mob;
import net.zharok01.coralinesystems.util.CowMilkDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class MobMixin {

    /**
     * ate() is defined in Mob, not overridden in Animal or Cow, so this is
     * the only class we can inject into — targeting Cow or Animal produces
     * "Cannot resolve any target instructions" because no bytecode exists
     * there for Mixin to hook.
     *
     * The duck-type check keeps this surgical: every Mob calls ate() when
     * EatBlockGoal fires, but only the ones that implement CowMilkDuck
     * (i.e. our Cow) will have their milked flag reset.
     */
    @Inject(method = "ate", at = @At("TAIL"))
    private void coraline$onAte(CallbackInfo ci) {
        if ((Object) this instanceof CowMilkDuck duck) {
            duck.coraline$setMilked(false);
        }
    }
}