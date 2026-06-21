package net.zharok01.coralinesystems.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.registry.CoralineSounds;

import java.util.EnumSet;

public class OrbEntity extends FlyingMob implements Enemy {
    // We use this to track if the Orb is currently charging an attack (for animations/rendering)
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.BOOLEAN);

    public OrbEntity(EntityType<? extends OrbEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
        this.moveControl = new OrbMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new OrbRandomFloatAroundGoal(this));
        this.goalSelector.addGoal(7, new OrbLookGoal(this));
        this.goalSelector.addGoal(7, new OrbShootFireballGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (livingEntity) -> {
            return Math.abs(livingEntity.getY() - this.getY()) <= 16.0D;
        }));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return FlyingMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.FLYING_SPEED, 0.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_CHARGING, false);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_IS_CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(DATA_IS_CHARGING, charging);
    }

    // --- DAWN SPAWNING LOGIC ---
    public static boolean checkOrbSpawnRules(EntityType<OrbEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // Vanilla dayTime goes from 0 to 24000. Dawn starts roughly around 23000 and ends shortly after 0.
        long dayTime = level.getLevel().getDayTime() % 24000;
        boolean isDawn = dayTime >= 23000 || dayTime <= 1000;

        // Ensure it's dawn, not peaceful, and the space is empty enough to spawn a floating mob
        return isDawn && level.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }

    // ==========================================
    // AI CONTROLLERS (Adapted from Vanilla Ghast)
    // ==========================================

    static class OrbMoveControl extends MoveControl {
        private final OrbEntity orb;
        private int floatDuration;

        public OrbMoveControl(OrbEntity orb) {
            super(orb);
            this.orb = orb;
        }

        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                if (this.floatDuration-- <= 0) {
                    this.floatDuration += this.orb.getRandom().nextInt(5) + 2;
                    Vec3 vector = new Vec3(this.wantedX - this.orb.getX(), this.wantedY - this.orb.getY(), this.wantedZ - this.orb.getZ());
                    double distance = vector.length();
                    vector = vector.normalize();
                    if (this.canReach(vector, Mth.ceil(distance))) {
                        this.orb.setDeltaMovement(this.orb.getDeltaMovement().add(vector.scale(0.1D)));
                    } else {
                        this.operation = MoveControl.Operation.WAIT;
                    }
                }
            }
        }

        private boolean canReach(Vec3 vector, int distanceCeil) {
            AABB boundingBox = this.orb.getBoundingBox();
            for(int i = 1; i < distanceCeil; ++i) {
                boundingBox = boundingBox.move(vector);
                if (!this.orb.level().noCollision(this.orb, boundingBox)) {
                    return false;
                }
            }
            return true;
        }
    }

    static class OrbRandomFloatAroundGoal extends Goal {
        private final OrbEntity orb;

        public OrbRandomFloatAroundGoal(OrbEntity orb) {
            this.orb = orb;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            MoveControl moveControl = this.orb.getMoveControl();
            if (!moveControl.hasWanted()) {
                return true;
            } else {
                double distanceX = moveControl.getWantedX() - this.orb.getX();
                double distanceY = moveControl.getWantedY() - this.orb.getY();
                double distanceZ = moveControl.getWantedZ() - this.orb.getZ();
                double distanceSqr = distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;
                return distanceSqr < 1.0D || distanceSqr > 3600.0D;
            }
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            RandomSource random = this.orb.getRandom();
            double targetX = this.orb.getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double targetY = this.orb.getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double targetZ = this.orb.getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            this.orb.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0D);
        }
    }

    static class OrbLookGoal extends Goal {
        private final OrbEntity orb;

        public OrbLookGoal(OrbEntity orb) {
            this.orb = orb;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return true;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            if (this.orb.getTarget() == null) {
                Vec3 vector = this.orb.getDeltaMovement();
                this.orb.setYRot(-((float)Mth.atan2(vector.x, vector.z)) * 57.295776F);
                this.orb.yBodyRot = this.orb.getYRot();
            } else {
                LivingEntity target = this.orb.getTarget();
                if (target.distanceToSqr(this.orb) < 4096.0D) {
                    double distanceX = target.getX() - this.orb.getX();
                    double distanceZ = target.getZ() - this.orb.getZ();
                    this.orb.setYRot(-((float)Mth.atan2(distanceX, distanceZ)) * 57.295776F);
                    this.orb.yBodyRot = this.orb.getYRot();
                }
            }
        }
    }

    static class OrbShootFireballGoal extends Goal {
        private final OrbEntity orb;
        public int chargeTime;

        public OrbShootFireballGoal(OrbEntity orb) {
            this.orb = orb;
        }

        public boolean canUse() {
            return this.orb.getTarget() != null;
        }

        public void start() {
            this.chargeTime = 0;
        }

        public void stop() {
            this.orb.setCharging(false);
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity target = this.orb.getTarget();
            if (target != null) {
                double distanceSqr = this.orb.distanceToSqr(target);
                if (distanceSqr < 4096.0D && this.orb.hasLineOfSight(target)) {
                    Level level = this.orb.level();
                    ++this.chargeTime;

                    if (this.chargeTime == 10 && !this.orb.isSilent()) {
                        // Warning sound before shooting
                        this.orb.playSound(CoralineSounds.ORB_CHARGE.get(), 1.0F, 1.0F); //TODO: Add the translations!
                    }

                    if (this.chargeTime == 20) {
                        Vec3 viewVector = this.orb.getViewVector(1.0F);
                        double dX = target.getX() - (this.orb.getX() + viewVector.x * 4.0D);
                        double dY = target.getY(0.5D) - (0.5D + this.orb.getY(0.5D));
                        double dZ = target.getZ() - (this.orb.getZ() + viewVector.z * 4.0D);

                        if (!this.orb.isSilent()) {
                            this.orb.playSound(CoralineSounds.ORB_SHOOT.get(), 1.0F, 1.0F); //TODO: Add the translations!
                        }

                        // ---> PIN: PLACEHOLDER PROJECTILE <---
                        // Change this later to your custom Isotopic Projectile!
                        LargeFireball fireball = new LargeFireball(level, this.orb, dX, dY, dZ, 1);
                        fireball.setPos(this.orb.getX() + viewVector.x * 1.5D, this.orb.getY(0.5D), this.orb.getZ() + viewVector.z * 1.5D);
                        level.addFreshEntity(fireball);
                        // ---------------------------------------

                        this.chargeTime = -40; // Cooldown before next attack
                    }
                } else if (this.chargeTime > 0) {
                    --this.chargeTime;
                }
                this.orb.setCharging(this.chargeTime > 10);
            }
        }
    }
}