package net.zharok01.coralinesystems.mixin.worldgen;

import mod.bluestaggo.modernerbeta.util.noise.PerlinOctaveNoise;
import net.zharok01.coralinesystems.mixin.accessor.ChunkProviderInfdev227Accessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "mod.bluestaggo.modernerbeta.level.chunk.provider.ChunkProviderInfdev227", remap = false)
public class ChunkProviderInfdev227Mixin {

    @Unique private static final float AMPLITUDE_DIVISOR = 768.0F;
    @Unique private static final float SELECTOR_DIVISOR = 5.0F;
    @Unique private static final float HILLS_DIVISOR = 3.0F;
    @Unique private static final int BASE_HEIGHT_OFFSET = 24;

    @Inject(
            method = "sampleHeightmap",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sampleHeightmap(int x, int z, CallbackInfoReturnable<Integer> cir) {
        ChunkProviderInfdev227Accessor self = (ChunkProviderInfdev227Accessor) (Object) this;

        PerlinOctaveNoise noiseA = self.getOctaveNoiseA();
        PerlinOctaveNoise noiseB = self.getOctaveNoiseB();
        PerlinOctaveNoise noiseC = self.getOctaveNoiseC();
        PerlinOctaveNoise noiseD = self.getOctaveNoiseD();
        PerlinOctaveNoise noiseE = self.getOctaveNoiseE();
        PerlinOctaveNoise noiseF = self.getOctaveNoiseF();
        int seaLevel = self.getSeaLevel();

        float primaryNoise = (float)(
                noiseA.sample((double)(x * 32.0F), 0.0, (double)(z * 32.0F))
                        - noiseB.sample((double)(x * 64.0F), 0.0, (double)(z * 64.0F))
        ) / AMPLITUDE_DIVISOR;

        float selector = (float) noiseE.sampleXY((double)(x / 4.0F), (double)(z / 4.0F));
        float terrainScale = (float) noiseF.sampleXY((double)(x / 8.0F), (double)(z / 8.0F)) / SELECTOR_DIVISOR;

        float detailNoise;
        if (selector > 0.0F) {
            detailNoise = (float)(noiseC.sampleXY((double)(x / 3.888889F * 2.0F), (double)(z / 3.888889F * 2.0F)) * (double) terrainScale / (double) HILLS_DIVISOR);
        } else {
            detailNoise = (float)(noiseD.sampleXY((double)(x / 3.888889F), (double)(z / 3.888889F)) * (double) terrainScale);
        }

        int heightVal = (int)(primaryNoise + seaLevel + BASE_HEIGHT_OFFSET + detailNoise);

        if ((float) noiseE.sampleXY((double) x, (double) z) < 0.0F) {
            heightVal = heightVal / 2 << 1;
            if ((float) noiseE.sampleXY((double)(x / 5), (double)(z / 5)) < 0.0F) {
                ++heightVal;
            }
        }

        cir.setReturnValue(heightVal);
    }
}