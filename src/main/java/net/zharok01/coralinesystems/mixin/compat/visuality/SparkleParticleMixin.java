package net.zharok01.coralinesystems.mixin.compat.visuality;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.zharok01.coralinesystems.mixin.accessor.ParticleAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "plus.dragons.visuality.particle.SparkleParticle", remap = false)
public abstract class SparkleParticleMixin {

    private static final int MAX_LIGHT_LEVEL = 15;
    private static final float MIN_ALPHA = 0.1F;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void coraline$setOpacityByLight(ClientLevel level, double x, double y, double z, float red, float green, float blue, SpriteSet sprites, CallbackInfo ci) {
        BlockPos position = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        int lightLevel = level.getMaxLocalRawBrightness(position);
        float alpha = MIN_ALPHA + (1.0F - MIN_ALPHA) * (lightLevel / (float) MAX_LIGHT_LEVEL);
        ((ParticleAccessor) (Object) this).coraline$setAlpha(alpha);
    }

    @Inject(method = "m_7556_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void coraline$useTranslucentRenderType(CallbackInfoReturnable<ParticleRenderType> cir) {
        cir.setReturnValue(ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT);
    }

    @Inject(method = "m_6355_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void coraline$useWorldLight(float partialTick, CallbackInfoReturnable<Integer> cir) {
        ParticleAccessor particle = (ParticleAccessor) (Object) this;
        ClientLevel level = particle.coraline$getLevel();
        BlockPos position = new BlockPos(Mth.floor(particle.coraline$getX()), Mth.floor(particle.coraline$getY()), Mth.floor(particle.coraline$getZ()));
        boolean hasChunk = level.hasChunk(SectionPos.blockToSectionCoord(position.getX()), SectionPos.blockToSectionCoord(position.getZ()));
        int lightColor = hasChunk ? LevelRenderer.getLightColor(level, position) : 0;
        cir.setReturnValue(lightColor);
    }
}