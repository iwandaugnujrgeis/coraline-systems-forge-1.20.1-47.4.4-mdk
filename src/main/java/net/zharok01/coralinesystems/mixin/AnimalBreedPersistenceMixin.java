package net.zharok01.coralinesystems.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class AnimalBreedPersistenceMixin {

    /**
     * If either parent is persistent, the baby inherits the flag automatically.
     *
     * finalizeSpawnChildFromBreeding is called once per birth, with both parents
     * and the freshly created baby available — the cleanest possible hook.
     *
     * "this" is the animal that initiated breeding (parent A). The second Animal
     * parameter is the mate (parent B). Either parent being persistent is enough
     * to mark the baby — e.g. a tamed horse bred with a wild horse should still
     * produce a persistent foal.
     *
     * setPersistenceRequired() is a simple NBT-backed boolean flag that is never
     * cleared by the age-up process, so the baby will remain persistent when it
     * grows into an adult with no further handling needed.
     */
    @Inject(
            method = "finalizeSpawnChildFromBreeding",
            at = @At("HEAD")
    )
    private void coraline$inheritPersistenceOnBirth(ServerLevel level, Animal mate, AgeableMob baby,
                                                    CallbackInfo ci) {
        Animal self = (Animal) (Object) this;

        if ((self.isPersistenceRequired() || mate.isPersistenceRequired()) && !baby.isPersistenceRequired()) {
            baby.setPersistenceRequired();
        }
    }
}