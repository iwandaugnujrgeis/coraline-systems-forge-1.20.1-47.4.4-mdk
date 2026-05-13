package net.zharok01.coralinesystems.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Animal;
import net.zharok01.coralinesystems.util.CowEatAnimationDuck;
import net.zharok01.coralinesystems.util.CowMilkDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class AnimalMixin {

    // ── Save / Load (kept from the original AnimalMixin) ─────────────────────
    //
    // Note: CowMixin now also overrides addAdditionalSaveData / readAdditionalSaveData
    // directly. These injections are harmless (they write the same key twice), but
    // you can remove them here if you prefer to keep save/load in one place.

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void coraline$saveCowMilkedState(CompoundTag compound, CallbackInfo ci) {
        if ((Object) this instanceof CowMilkDuck duck) {
            compound.putBoolean("IsMilked", duck.coraline$isMilked());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void coraline$loadCowMilkedState(CompoundTag compound, CallbackInfo ci) {
        if ((Object) this instanceof CowMilkDuck duck) {
            duck.coraline$setMilked(compound.getBoolean("IsMilked"));
        }
    }

    // ── Eat animation wiring ──────────────────────────────────────────────────
    //
    // handleEntityEvent and aiStep are both overridden in Animal (not Cow), so
    // @Inject into them from CowMixin targeting Cow.class crashes at launch with
    // "cannot find any targets". The fix is identical to the ate() / MobMixin
    // pattern: inject here where the bytecode actually lives, then delegate to
    // CowMixin's private field via the CowEatAnimationDuck interface.

    /**
     * EatBlockGoal broadcasts entity event 10 when the eating animation starts —
     * the same event Sheep uses to begin its head-dip. We catch it here and tell
     * any CowEatAnimationDuck (i.e. our Cow) to start its 40-tick counter.
     */
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void coraline$onEntityEvent(byte id, CallbackInfo ci) {
        if (id == 10 && (Object) this instanceof CowEatAnimationDuck duck) {
            duck.coraline$startEatAnimation();
        }
    }

    /**
     * Decrements the eat-animation counter each tick, mirroring how Sheep
     * manages its own eatAnimationTick in its aiStep override.
     */
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void coraline$tickCowEatAnimation(CallbackInfo ci) {
        if ((Object) this instanceof CowEatAnimationDuck duck) {
            duck.coraline$tickEatAnimation();
        }
    }
}