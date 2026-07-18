package net.zharok01.coralinesystems.registry;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;

public class CoralineParticles {

    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CoralineSystems.MOD_ID);

    /**
     * Soft pinkish-white spark that drifts from Orbs and Orb Pulses.
     * overrideLimiter = false — the engine may cull it on lower particle settings.
     */
    public static final RegistryObject<SimpleParticleType> ORB_SPARKLE =
            PARTICLE_TYPES.register("orb_sparkle", () -> new SimpleParticleType(false));

    /**
     * Tinted splash particle for the Brewing Cauldron entity-inside effect.
     */
    public static final RegistryObject<SimpleParticleType> CAULDRON_SPLASH =
            PARTICLE_TYPES.register("cauldron_splash", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}