package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumSet;

public class RaccoonPebbleThrowGoal extends Goal {

    private static final double ATTACK_RANGE_SQ = 16.0 * 16.0;
    private static final float THROW_POWER = 1.5F;
    private static final float THROW_INACCURACY = 5.0F;

    private final EntityRaccoon raccoon;
    private final double speedModifier;
    private int attackTime;
    private int attackStep;
    private int maxBurstCount;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public RaccoonPebbleThrowGoal(EntityRaccoon raccoon) {
        this(raccoon, 1.25D);
    }

    public RaccoonPebbleThrowGoal(EntityRaccoon raccoon, double speedModifier) {
        this.raccoon = raccoon;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return raccoon.isTame() && raccoon.getTarget() != null && raccoon.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = raccoon.getTarget();
        return raccoon.isTame()
                && target != null
                && target.isAlive()
                && raccoon.distanceToSqr(target) <= ATTACK_RANGE_SQ;
    }

    @Override
    public void start() {
        super.start();
        seeTime = 0;
        strafingTime = -1;
        attackStep = 0;
        attackTime = 0;
    }

    @Override
    public void stop() {
        super.stop();
        seeTime = 0;
        strafingTime = -1;
        attackStep = 0;
        attackTime = 0;
        raccoon.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = raccoon.getTarget();
        if (target == null) return;

        double distanceSqr = raccoon.distanceToSqr(target);
        boolean canSee = raccoon.getSensing().hasLineOfSight(target);

        if (canSee) {
            seeTime++;
        } else {
            seeTime = 0;
        }

        if (distanceSqr <= ATTACK_RANGE_SQ && seeTime >= 20) {
            raccoon.getNavigation().stop();
            strafingTime++;
        } else {
            raccoon.getNavigation().moveTo(target, speedModifier);
            strafingTime = -1;
        }

        if (strafingTime >= 20) {
            if (raccoon.getRandom().nextFloat() < 0.3F) {
                strafingClockwise = !strafingClockwise;
            }
            if (raccoon.getRandom().nextFloat() < 0.3F) {
                strafingBackwards = !strafingBackwards;
            }
            strafingTime = 0;
        }

        if (strafingTime > -1) {
            if (distanceSqr > ATTACK_RANGE_SQ * 0.75D) {
                strafingBackwards = false;
            } else if (distanceSqr < ATTACK_RANGE_SQ * 0.25D) {
                strafingBackwards = true;
            }

            if (coraline$canStrafe()) {
                raccoon.getMoveControl().strafe(
                        strafingBackwards ? -0.5F : 0.5F,
                        strafingClockwise ? 0.5F : -0.5F
                );
            }
            raccoon.getLookControl().setLookAt(target, 30.0F, 30.0F);
        } else {
            raccoon.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        attackTime--;
        if (distanceSqr <= ATTACK_RANGE_SQ && canSee) {
            if (attackTime <= 0) {
                attackStep++;
                if (attackStep == 1) {
                    maxBurstCount = 2 + raccoon.getRandom().nextInt(2);
                    attackTime = 10 + raccoon.getRandom().nextInt(10);
                } else if (attackStep <= maxBurstCount + 1) {
                    attackTime = 6 + raccoon.getRandom().nextInt(4);
                } else {
                    attackTime = 40 + raccoon.getRandom().nextInt(40);
                    attackStep = 0;
                }

                if (attackStep > 1) {
                    coraline$throwPebble(target);
                }
            }
        } else {
            attackStep = 0;
        }
    }

    private boolean coraline$canStrafe() {
        if (!raccoon.onGround()) return false;

        BlockPathTypes pathType = raccoon.getNavigation().getNodeEvaluator()
                .getBlockPathType(raccoon.level(),
                        (int) raccoon.getX(),
                        (int) raccoon.getY(),
                        (int) raccoon.getZ());

        return raccoon.getPathfindingMalus(pathType) >= 0.0F;
    }

    private void coraline$throwPebble(LivingEntity target) {
        if (raccoon.level().isClientSide()) return;

        double dx = target.getX() - raccoon.getX();
        double dy = target.getBoundingBox().minY + target.getBbHeight() * 0.5 - raccoon.getEyeY();
        double dz = target.getZ() - raccoon.getZ();

        Item stonePebble = ForgeRegistries.ITEMS.getValue(new ResourceLocation("spelunkery", "stone_pebble"));
        EntityType<?> pebbleType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("spelunkery", "pebble"));

        if (stonePebble != null && pebbleType != null) {
            Entity entity = pebbleType.create(raccoon.level());

            if (entity instanceof ThrowableItemProjectile pebble) {
                pebble.setPos(raccoon.getX(), raccoon.getEyeY() - 0.1D, raccoon.getZ());
                pebble.setOwner(raccoon);
                pebble.setItem(new ItemStack(stonePebble));

                pebble.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2, dz, THROW_POWER, THROW_INACCURACY);
                raccoon.level().addFreshEntity(pebble);

                raccoon.level().playSound(
                        null,
                        raccoon.getX(),
                        raccoon.getY(),
                        raccoon.getZ(),
                        SoundEvents.SNOWBALL_THROW,
                        SoundSource.NEUTRAL,
                        0.5F,
                        0.4F / (raccoon.getRandom().nextFloat() * 0.4F + 0.8F)
                );
            }
        }
    }
}