package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.entity.projectile.ThrownStick;

import java.util.EnumSet;

public class GliderKiteSpiderGoal extends Goal {

    private static final double THROW_RANGE = 12.0D;
    private static final double THROW_RANGE_SQ = THROW_RANGE * THROW_RANGE;
    private static final double STRAFE_RANGE_SQ = 9.0D * 9.0D;
    private static final double FLEE_RANGE_SQ = 6.0D * 6.0D;

    private static final int SCAN_CHANCE = 4;
    private static final int WIND_UP_TICKS = 10;
    private static final int THROW_COOLDOWN_TICKS = 30;
    private static final float THROW_VELOCITY = 1.4F;
    private static final float THROW_INACCURACY = 6.0F;
    private static final double ARC_COMPENSATION = 0.2D;

    private static final double FLEE_SPEED = 1.5D;
    private static final int FLEE_PATH_RANGE = 12;
    private static final int FLEE_PATH_HEIGHT = 5;
    private static final int FLEE_REPATH_TICKS = 10;

    private static final float STRAFE_SIDE_SPEED = 0.4F;
    private static final float STRAFE_BACK_SPEED = -0.2F;
    private static final double STRAFE_SPEED_MODIFIER = 0.6D;
    private static final double STRAFE_LOOK_AHEAD = 1.0D;
    private static final int STRAFE_SWITCH_MIN_TICKS = 20;
    private static final int STRAFE_SWITCH_EXTRA_TICKS = 30;

    private final EntitySugarGlider glider;
    private Spider target;
    private int throwCooldown;
    private int fleeRepathCooldown;
    private int strafeSwitchCooldown;
    private boolean strafeClockwise;

    public GliderKiteSpiderGoal(EntitySugarGlider glider) {
        this.glider = glider;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static Spider findNearestSpider(EntitySugarGlider glider, double range) {
        Spider nearest = null;
        double nearestDistSq = range * range;
        for (Spider spider : glider.level().getEntitiesOfClass(Spider.class, glider.getBoundingBox().inflate(range))) {
            double distSq = glider.distanceToSqr(spider);
            if (spider.isAlive() && distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = spider;
            }
        }
        return nearest;
    }

    private boolean canThrow() {
        return !this.glider.level().isDay() && SugarGliderData.getSticks(this.glider) > 0;
    }

    private boolean shouldEngage(LivingEntity spider) {
        double distSq = this.glider.distanceToSqr(spider);
        if (distSq <= FLEE_RANGE_SQ) return true;
        return this.canThrow() && distSq <= THROW_RANGE_SQ && this.glider.hasLineOfSight(spider);
    }

    @Override
    public boolean canUse() {
        if (this.glider.getRandom().nextInt(reducedTickDelay(SCAN_CHANCE)) != 0) return false;

        this.target = findNearestSpider(this.glider, THROW_RANGE);
        return this.target != null && this.shouldEngage(this.target);
    }

    @Override
    public boolean canContinueToUse() {
        return this.target.isAlive() && this.shouldEngage(this.target);
    }

    @Override
    public void start() {
        this.throwCooldown = WIND_UP_TICKS;
        this.fleeRepathCooldown = 0;
    }

    @Override
    public void stop() {
        this.glider.getNavigation().stop();
        this.target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.glider.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        double distSq = this.glider.distanceToSqr(this.target);

        if (distSq < FLEE_RANGE_SQ) {
            this.flee();
        } else if (this.canThrow() && distSq < STRAFE_RANGE_SQ) {
            this.strafe();
        } else {
            this.glider.getNavigation().stop();
        }

        if (this.throwCooldown > 0) {
            this.throwCooldown--;
        } else if (this.canThrow() && this.glider.hasLineOfSight(this.target)) {
            this.throwStick();
        }
    }

    private void flee() {
        if (--this.fleeRepathCooldown > 0) return;
        this.fleeRepathCooldown = FLEE_REPATH_TICKS;

        Vec3 escape = DefaultRandomPos.getPosAway(this.glider, FLEE_PATH_RANGE, FLEE_PATH_HEIGHT, this.target.position());
        if (escape != null) {
            this.glider.getNavigation().moveTo(escape.x, escape.y, escape.z, FLEE_SPEED);
        } else {
            float side = this.strafeClockwise ? STRAFE_SIDE_SPEED : -STRAFE_SIDE_SPEED;
            if (this.coraline$canStrafe(STRAFE_BACK_SPEED, side)) {
                this.coraline$strafeSlowly(STRAFE_BACK_SPEED, side);
            }
        }
    }

    private void strafe() {
        this.glider.getNavigation().stop();
        if (--this.strafeSwitchCooldown <= 0) {
            this.strafeClockwise = !this.strafeClockwise;
            this.strafeSwitchCooldown = STRAFE_SWITCH_MIN_TICKS + this.glider.getRandom().nextInt(STRAFE_SWITCH_EXTRA_TICKS);
        }

        float side = this.strafeClockwise ? STRAFE_SIDE_SPEED : -STRAFE_SIDE_SPEED;
        if (!this.coraline$canStrafe(STRAFE_BACK_SPEED, side)) {
            this.strafeClockwise = !this.strafeClockwise;
            this.strafeSwitchCooldown = STRAFE_SWITCH_MIN_TICKS + this.glider.getRandom().nextInt(STRAFE_SWITCH_EXTRA_TICKS);
            side = -side;
            if (!this.coraline$canStrafe(STRAFE_BACK_SPEED, side)) return;
        }
        this.coraline$strafeSlowly(STRAFE_BACK_SPEED, side);
    }

    private void coraline$strafeSlowly(float forward, float side) {
        this.glider.getMoveControl().setWantedPosition(this.glider.getX(), this.glider.getY(), this.glider.getZ(), STRAFE_SPEED_MODIFIER);
        this.glider.getMoveControl().strafe(forward, side);
    }

    private boolean coraline$canStrafe(float forward, float side) {
        if (!this.glider.onGround() || this.glider.isGliding() || this.glider.getAttachmentFacing() != Direction.DOWN) return false;

        float yawRadians = this.glider.getYRot() * Mth.DEG_TO_RAD;
        float sin = Mth.sin(yawRadians);
        float cos = Mth.cos(yawRadians);
        Vec3 direction = new Vec3(forward * cos - side * sin, 0.0D, side * cos + forward * sin).normalize();

        BlockPos ahead = BlockPos.containing(
                this.glider.getX() + direction.x * STRAFE_LOOK_AHEAD,
                this.glider.getY(),
                this.glider.getZ() + direction.z * STRAFE_LOOK_AHEAD
        );
        BlockPos below = ahead.below();
        Level level = this.glider.level();

        return level.getFluidState(ahead).isEmpty()
                && level.getFluidState(below).isEmpty()
                && level.getBlockState(ahead).getCollisionShape(level, ahead).isEmpty()
                && !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
    }

    private void throwStick() {
        this.throwCooldown = THROW_COOLDOWN_TICKS;
        SugarGliderData.setSticks(this.glider, SugarGliderData.getSticks(this.glider) - 1);

        ThrownStick stick = new ThrownStick(this.glider.level(), this.glider);
        double dx = this.target.getX() - this.glider.getX();
        double dy = this.target.getY(0.5D) - stick.getY();
        double dz = this.target.getZ() - this.glider.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        stick.shoot(dx, dy + horizontalDistance * ARC_COMPENSATION, dz, THROW_VELOCITY, THROW_INACCURACY);
        this.glider.level().addFreshEntity(stick);
        this.glider.playSound(SoundEvents.SNOWBALL_THROW, 0.6F, 1.4F);
    }
}