package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Animal;
import net.zharok01.coralinesystems.util.interfaces.CowEatAnimationDuck;
import net.zharok01.coralinesystems.util.interfaces.CowMilkDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class AnimalMixin {

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

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void coraline$onEntityEvent(byte id, CallbackInfo ci) {
        if (id == 10 && (Object) this instanceof CowEatAnimationDuck duck) {
            duck.coraline$startEatAnimation();
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void coraline$tickCowEatAnimation(CallbackInfo ci) {
        if ((Object) this instanceof CowEatAnimationDuck duck) {
            duck.coraline$tickEatAnimation();
        }
    }
}