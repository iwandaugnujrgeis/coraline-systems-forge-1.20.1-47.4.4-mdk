package net.zharok01.coralinesystems.registry;

import com.mojang.serialization.Codec;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegisterEvent;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.particle.OrbShieldBurstParticle;
import net.zharok01.coralinesystems.entity.OrbShieldBurstData;

/**
 * Registry for particle types that need a custom codec/payload (i.e. cannot use
 * DeferredRegister + SimpleParticleType). Mirrors Rediscovered's RediscoveredParticles,
 * which registers these via the raw Forge RegisterEvent since ParticleType<T> with a
 * custom Deserializer/Codec isn't a DeferredRegister-friendly type.
 * <p>
 * Simple, no-payload particles stay in {@link CoralineParticles} as before.
 */
public class CoralineParticleTypes
{
    public static final ParticleType<OrbShieldBurstData> ORB_SHIELD_BURST =
            customParticleType(true, OrbShieldBurstData.DESERIALIZER, OrbShieldBurstData.CODEC);

    public static void init(RegisterEvent event)
    {
        register(event, "orb_shield_burst", ORB_SHIELD_BURST);
    }

    private static void register(RegisterEvent event, String key, ParticleType<?> particle)
    {
        event.register(Registries.PARTICLE_TYPE, CoralineSystems.of(key), () -> particle);
    }

    @SuppressWarnings("deprecation")
    private static <T extends ParticleOptions> ParticleType<T> customParticleType(boolean overrideLimiter, ParticleOptions.Deserializer<T> deserializer, Codec<T> codec)
    {
        return new ParticleType<T>(overrideLimiter, deserializer)
        {
            @Override
            public Codec<T> codec()
            {
                return codec;
            }
        };
    }

    /**
     * Client-side factory registration for "special" (non-sprite) particles.
     * Mirrors Rediscovered's RediscoveredParticles.Factories.
     */
    @OnlyIn(Dist.CLIENT)
    public static class Factories
    {
        public static void init(net.minecraftforge.client.event.RegisterParticleProvidersEvent event)
        {
            event.registerSpecial(ORB_SHIELD_BURST, new OrbShieldBurstParticle.Factory());
        }
    }
}