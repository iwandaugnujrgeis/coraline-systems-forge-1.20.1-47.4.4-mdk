package net.zharok01.coralinesystems.entity;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.zharok01.coralinesystems.registry.CoralineParticleTypes;

/**
 * Particle payload for the Orb's death "shield burst" — a single energy shell
 * that expands from startSize to endSize while fading from startAlpha to endAlpha,
 * mirroring Rediscovered's PylonShieldBlastData used by PylonBurstEntity.
 */
public record OrbShieldBurstData(int lifetime, float startSize, float endSize, float startAlpha, float endAlpha) implements ParticleOptions
{
    public static final Codec<OrbShieldBurstData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("lifetime").forGetter(OrbShieldBurstData::lifetime),
                    Codec.FLOAT.fieldOf("start_size").forGetter(OrbShieldBurstData::startSize),
                    Codec.FLOAT.fieldOf("end_size").forGetter(OrbShieldBurstData::endSize),
                    Codec.FLOAT.fieldOf("start_alpha").forGetter(OrbShieldBurstData::startAlpha),
                    Codec.FLOAT.fieldOf("end_alpha").forGetter(OrbShieldBurstData::endAlpha)
            ).apply(instance, OrbShieldBurstData::new));

    public static final ParticleOptions.Deserializer<OrbShieldBurstData> DESERIALIZER = new ParticleOptions.Deserializer<OrbShieldBurstData>()
    {
        @Override
        public OrbShieldBurstData fromCommand(ParticleType<OrbShieldBurstData> type, StringReader reader) throws CommandSyntaxException
        {
            reader.expect(' ');
            int lifetime = reader.readInt();
            reader.expect(' ');
            float startSize = reader.readFloat();
            reader.expect(' ');
            float endSize = reader.readFloat();
            reader.expect(' ');
            float startAlpha = reader.readFloat();
            reader.expect(' ');
            float endAlpha = reader.readFloat();
            return new OrbShieldBurstData(lifetime, startSize, endSize, startAlpha, endAlpha);
        }

        @Override
        public OrbShieldBurstData fromNetwork(ParticleType<OrbShieldBurstData> type, FriendlyByteBuf buffer)
        {
            return new OrbShieldBurstData(buffer.readInt(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        }
    };

    /** Default burst: quick 10-tick expansion from nothing out to a large shell, fading to invisible. */
    public OrbShieldBurstData()
    {
        this(10, 0.0F, 30.0F, 1.0F, 0.0F);
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.lifetime);
        buffer.writeFloat(this.startSize);
        buffer.writeFloat(this.endSize);
        buffer.writeFloat(this.startAlpha);
        buffer.writeFloat(this.endAlpha);
    }

    @Override
    public ParticleType<?> getType()
    {
        return CoralineParticleTypes.ORB_SHIELD_BURST;
    }

    @Override
    public String writeToString()
    {
        return String.format(Locale.ROOT, "%s %d %.2f %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.lifetime, this.startSize, this.endSize, this.startAlpha, this.endAlpha);
    }
}