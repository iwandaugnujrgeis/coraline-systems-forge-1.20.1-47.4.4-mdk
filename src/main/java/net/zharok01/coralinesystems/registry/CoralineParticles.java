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
     * Tinted splash particle for the Brewing Cauldron entity-inside effect
     * and the "brew finished" burst.
     */
    public static final RegistryObject<SimpleParticleType> CAULDRON_SPLASH =
            PARTICLE_TYPES.register("cauldron_splash", () -> new SimpleParticleType(false));

    /**
     * Tinted rising bubble for the Brewing Cauldron's active-brewing ambient
     * effect.  Rises slowly from the fluid surface and fades out, giving
     * the player a continuous visual cue that fermentation is happening.
     */
    public static final RegistryObject<SimpleParticleType> CAULDRON_BUBBLE =
            PARTICLE_TYPES.register("cauldron_bubble", () -> new SimpleParticleType(false));

    /**
     * Tinted 1-pixel fizz dot for finished Kombucha's ambient idle effect.
     * Wobbles upward from the fluid surface and fades quickly, giving a
     * light carbonated "fizz" feel distinct from the heavier brewing bubbles.
     * Uses the same r/g/b-in-velocity-slots color convention as CAULDRON_BUBBLE,
     * so it can be reused for any other tinted drink in the future.
     */
    public static final RegistryObject<SimpleParticleType> CAULDRON_FIZZ =
            PARTICLE_TYPES.register("cauldron_fizz", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}