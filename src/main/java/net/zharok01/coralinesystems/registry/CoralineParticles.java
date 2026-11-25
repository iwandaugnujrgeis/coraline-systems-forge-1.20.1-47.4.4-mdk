package net.zharok01.coralinesystems.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.OrbShieldBurstData;
import org.jetbrains.annotations.NotNull;

public class CoralineParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CoralineSystems.MOD_ID);

    public static final RegistryObject<SimpleParticleType> ORB_SPARKLE =
            PARTICLE_TYPES.register("orb_sparkle", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> CAULDRON_SPLASH =
            PARTICLE_TYPES.register("cauldron_splash", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> CAULDRON_BUBBLE =
            PARTICLE_TYPES.register("cauldron_bubble", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> CAULDRON_FIZZ =
            PARTICLE_TYPES.register("cauldron_fizz", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> BLOOD_DROPLET =
            PARTICLE_TYPES.register("blood_droplet", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> BLAZE_SPARKLE =
            PARTICLE_TYPES.register("blaze_sparkle", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> SHARD_BURST =
            PARTICLE_TYPES.register("shard_burst", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> TIMEDUST_SPARKLE =
            PARTICLE_TYPES.register("timedust_sparkle", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> STAR_SPARKLE =
            PARTICLE_TYPES.register("star_sparkle", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> DARK_SMOKE =
            PARTICLE_TYPES.register("dark_smoke", () -> new SimpleParticleType(false));

    public static final RegistryObject<ParticleType<OrbShieldBurstData>> ORB_SHIELD_BURST =
            PARTICLE_TYPES.register("orb_shield_burst", () -> new ParticleType<OrbShieldBurstData>(true, OrbShieldBurstData.DESERIALIZER) {
                @Override
                public @NotNull Codec<OrbShieldBurstData> codec() {
                    return OrbShieldBurstData.CODEC;
                }
            });

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}