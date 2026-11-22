package net.zharok01.coralinesystems.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.zharok01.coralinesystems.registry.CoralineEffects;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

public class ThrownDimensionalShard extends ThrowableItemProjectile {

    private static final int CLARITY_DURATION = 600;
    private static final float SPLASH_RANGE = 4.0F;
    private static final float SPLASH_RANGE_SQ = SPLASH_RANGE * SPLASH_RANGE;

    public ThrownDimensionalShard(EntityType<? extends ThrownDimensionalShard> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownDimensionalShard(Level level, LivingEntity shooter) {
        super(IsotopicEntities.THROWN_DIMENSIONAL_SHARD.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return CoralineItems.DIMENSIONAL_SHARD.get();
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) return;

        spawnImpactParticles();
        playImpactSound();
        applyClarityInRange();
        discard();
    }

    private void spawnImpactParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            RandomSource random = serverLevel.getRandom();
            for (int i = 0; i < 24; i++) {
                double vx = (random.nextDouble() - 0.5) * 0.8;
                double vy = random.nextDouble() * 0.6;
                double vz = (random.nextDouble() - 0.5) * 0.8;
                serverLevel.sendParticles(
                        CoralineParticles.SHARD_BURST.get(),
                        getX(), getY(), getZ(),
                        1, vx, vy, vz, 0.1
                );
            }
        }
    }

    private void playImpactSound() {
        level().playSound(null, blockPosition(), CoralineSounds.SHARD_IMPACT.get(),
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.9F + level().getRandom().nextFloat() * 0.2F);
    }

    private void applyClarityInRange() {
        AABB searchBox = getBoundingBox().inflate(SPLASH_RANGE, 2.0F, SPLASH_RANGE);
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, searchBox)) {
            double distSq = distanceToSqr(entity);
            if (distSq >= SPLASH_RANGE_SQ) continue;

            double falloff = 1.0 - Math.sqrt(distSq) / SPLASH_RANGE;
            int scaledDuration = (int)(CLARITY_DURATION * falloff + 0.5);
            if (scaledDuration < 20) continue;

            entity.addEffect(new MobEffectInstance(
                    CoralineEffects.CLARITY.get(),
                    scaledDuration,
                    0,
                    false,
                    true
            ));
        }
    }
}