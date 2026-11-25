package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {

    @Accessor("alpha")
    void coraline$setAlpha(float alpha);

    @Accessor("level")
    ClientLevel coraline$getLevel();

    @Accessor("x")
    double coraline$getX();

    @Accessor("y")
    double coraline$getY();

    @Accessor("z")
    double coraline$getZ();
}
