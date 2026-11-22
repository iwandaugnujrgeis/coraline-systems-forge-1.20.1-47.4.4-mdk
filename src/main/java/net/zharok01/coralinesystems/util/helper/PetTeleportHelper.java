package net.zharok01.coralinesystems.util.helper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.zharok01.coralinesystems.registry.CoralineSounds;

public class PetTeleportHelper {

    public static void playTeleportEffects(LivingEntity pet, double oldX, double oldY, double oldZ) {
        if (!(pet.level() instanceof ServerLevel serverLevel)) return;

        double newX = pet.getX();
        double newY = pet.getY();
        double newZ = pet.getZ();

        serverLevel.sendParticles(ParticleTypes.CLOUD, oldX, oldY + 0.5, oldZ, 10, 0.3, 0.4, 0.3, 0.02);
        serverLevel.sendParticles(ParticleTypes.CLOUD, newX, newY + 0.5, newZ, 10, 0.3, 0.4, 0.3, 0.02);

        pet.playSound(
                CoralineSounds.PET_TELEPORT.get(),
                1.0F,
                0.8F + pet.getRandom().nextFloat() * 0.4F
        );
    }
}