package net.zharok01.coralinesystems.entity.projectile;

import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.zharok01.coralinesystems.registry.IsotopicEntities;
import org.jetbrains.annotations.NotNull;

public class ThrownStick extends ThrowableItemProjectile {

    private static final float STICK_DAMAGE = 3.0F;

    public ThrownStick(EntityType<? extends ThrownStick> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownStick(Level level, LivingEntity shooter) {
        super(IsotopicEntities.THROWN_STICK.get(), shooter, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return Items.STICK;
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof EntitySugarGlider);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;

        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), STICK_DAMAGE);
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) return;

        this.level().playSound(null, this.blockPosition(), SoundEvents.WOOD_HIT, SoundSource.NEUTRAL, 0.8F, 1.2F);
        this.discard();
    }
}