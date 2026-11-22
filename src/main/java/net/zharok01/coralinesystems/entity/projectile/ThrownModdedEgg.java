package net.zharok01.coralinesystems.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class ThrownModdedEgg extends ThrowableItemProjectile {

    protected ThrownModdedEgg(EntityType<? extends ThrownModdedEgg> entityType, Level level) {
        super(entityType, level);
    }

    protected ThrownModdedEgg(EntityType<? extends ThrownModdedEgg> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }

    protected abstract EntityType<? extends Animal> getBabyEntityType();

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, this.getItem()),
                        this.getX(), this.getY(), this.getZ(),
                        (this.random.nextFloat() - 0.5) * 0.08,
                        (this.random.nextFloat() - 0.5) * 0.08,
                        (this.random.nextFloat() - 0.5) * 0.08
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            if (this.random.nextInt(8) == 0) {
                int count = this.random.nextInt(32) == 0 ? 4 : 1;
                for (int i = 0; i < count; i++) {
                    Animal baby = this.getBabyEntityType().create(this.level());
                    if (baby != null) {
                        baby.setAge(-24000);
                        baby.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                        this.level().addFreshEntity(baby);
                    }
                }
            }
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }
}