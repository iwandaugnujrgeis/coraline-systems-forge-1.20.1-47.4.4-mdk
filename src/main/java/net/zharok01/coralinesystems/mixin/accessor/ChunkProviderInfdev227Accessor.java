package net.zharok01.coralinesystems.mixin.accessor;

import mod.bluestaggo.modernerbeta.util.noise.PerlinOctaveNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "mod.bluestaggo.modernerbeta.level.chunk.provider.ChunkProviderInfdev227", remap = false)
public interface ChunkProviderInfdev227Accessor {

    @Accessor("octaveNoiseA") PerlinOctaveNoise getOctaveNoiseA();
    @Accessor("octaveNoiseB") PerlinOctaveNoise getOctaveNoiseB();
    @Accessor("octaveNoiseC") PerlinOctaveNoise getOctaveNoiseC();
    @Accessor("octaveNoiseD") PerlinOctaveNoise getOctaveNoiseD();
    @Accessor("octaveNoiseE") PerlinOctaveNoise getOctaveNoiseE();
    @Accessor("octaveNoiseF") PerlinOctaveNoise getOctaveNoiseF();
    @Accessor("seaLevel") int getSeaLevel();
}