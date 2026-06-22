package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityParticlesMixin {

    /**
     * @author Coraline Systems
     * @reason Intercepts the potion particle spawning in LivingEntity#tickEffects.
     * If the entity emitting the particles is the camera entity and the player is in first-person view,
     * the particle is simply deleted before it even renders.
     */
    @Redirect(
            method = "tickEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void coraline_systems$hideFirstPersonPotionParticles(Level level, ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        Minecraft minecraft = Minecraft.getInstance();

        // Check if the entity is the player's camera and if we are in first-person mode
        if (minecraft.getCameraEntity() == (LivingEntity) (Object) this && minecraft.options.getCameraType().isFirstPerson()) {
            return; // Short-circuit: Do not spawn the particle
        }

        // Otherwise, allow the particle to spawn normally (for other entities or third-person view)
        level.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }
}