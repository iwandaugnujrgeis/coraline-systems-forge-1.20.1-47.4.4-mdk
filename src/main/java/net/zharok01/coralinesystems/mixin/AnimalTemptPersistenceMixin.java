package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TemptGoal.class)
public class AnimalTemptPersistenceMixin {

    @Shadow @Final protected PathfinderMob mob;

    /**
     * Marks an animal as persistent the moment it begins following a player
     * holding a tempting food item.
     *
     * We inject into start() rather than tick() because start() is called
     * exactly once when the animal activates — the animal has just decided to
     * follow — which is the natural point to "tag" it. tick() would set the
     * flag correctly too but fires every single tick for no extra benefit.
     *
     * The Animal instanceof guard mirrors the pattern in Nostalgic Tweaks'
     * TemptGoalMixin and ensures we only affect proper animals, not other
     * PathfinderMob subclasses that happen to use TemptGoal (e.g. Allays).
     */
    @Inject(method = "start", at = @At("HEAD"))
    private void coraline$markPersistentOnTempt(CallbackInfo ci) {
        if (mob instanceof Animal animal && !animal.isPersistenceRequired()) {
            animal.setPersistenceRequired();
        }
    }
}