package net.zharok01.coralinesystems.mixin.compat.nostalgic_tweaks;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.zharok01.coralinesystems.util.helper.AnimalPersistenceHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TemptGoal.class)
public class AnimalTemptPersistenceMixin {

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Inject(method = "start", at = @At("HEAD"))
    private void coraline$markPersistentOnTempt(CallbackInfo ci) {
        if (mob instanceof Animal animal && AnimalPersistenceHelper.coraline$shouldPersist(animal)) {
            animal.setPersistenceRequired();
        }
    }
}