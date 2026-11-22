package net.zharok01.coralinesystems.mixin.compat.nostalgic_tweaks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.zharok01.coralinesystems.util.helper.AnimalPersistenceHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class AnimalBreedPersistenceMixin {

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
    private void coraline$inheritPersistenceOnBirth(ServerLevel level, Animal mate, AgeableMob baby, CallbackInfo ci) {
        Animal self = (Animal) (Object) this;

        if (baby != null && (self.isPersistenceRequired() || mate.isPersistenceRequired())
                && AnimalPersistenceHelper.coraline$shouldPersist(baby)) {
            baby.setPersistenceRequired();
        }
    }
}