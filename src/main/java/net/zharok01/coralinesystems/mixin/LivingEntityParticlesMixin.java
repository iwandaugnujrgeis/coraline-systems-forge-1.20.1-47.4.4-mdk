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

    @Redirect(
            method = "tickEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void coraline$hideFirstPersonPotionParticles(Level level, ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.getCameraEntity() == (LivingEntity) (Object) this && minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        level.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }
}