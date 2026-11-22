package net.zharok01.coralinesystems.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.registry.CoralineBlocks;

@OnlyIn(Dist.CLIENT)
public class ShardBurstParticle extends TerrainParticle {

    private ShardBurstParticle(ClientLevel level, double x, double y, double z,
                               double vx, double vy, double vz) {
        super(level, x, y, z, vx, vy, vz,
                CoralineBlocks.SUSPICIOUS_BLOCK.get().defaultBlockState());
        this.lifetime = 18 + level.getRandom().nextInt(10);
        this.gravity = 0.6F;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new ShardBurstParticle(level, x, y, z, vx, vy, vz);
        }
    }
}