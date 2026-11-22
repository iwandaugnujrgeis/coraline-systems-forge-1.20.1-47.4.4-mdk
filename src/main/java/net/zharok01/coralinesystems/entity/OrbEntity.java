package net.zharok01.coralinesystems.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class OrbEntity extends FlyingMob implements Enemy {

    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING =
            SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_HOSTILE =
            SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int AMBIENT_PARTICLE_INTERVAL = 4;

    private static final double DESCEND_TRIGGER_HEIGHT = 25.0D;
    private static final double DESCEND_TARGET_HEIGHT = 20.0D;
    private static final int ALTITUDE_CHECK_INTERVAL = 100;

    private static final float PROVOKED_HEALTH = 3.0F;
    private static final float CRITICAL_HEALTH = 4.0F;

    private static final long SPAWN_WINDOW_START = 22331L;
    private static final long SPAWN_WINDOW_END = 23460L;
    private static final int DESPAWN_CHECK_INTERVAL = 120;

    private static final int HOVER_OFFSET_MIN = 10;
    private static final int HOVER_OFFSET_RANGE = 11;

    public OrbEntity(EntityType<? extends OrbEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
        this.moveControl = new OrbMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new OrbDescendGoal(this));
        this.goalSelector.addGoal(5, new OrbRandomFloatAroundGoal(this));
        this.goalSelector.addGoal(7, new OrbLookGoal(this));
        this.goalSelector.addGoal(7, new OrbShootFireballGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (livingEntity) -> this.isHostile() && Math.abs(livingEntity.getY() - this.getY()) <= 64.0D));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return FlyingMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.FLYING_SPEED, 0.08D)
                .add(Attributes.MOVEMENT_SPEED, 0.08D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.tickCount % AMBIENT_PARTICLE_INTERVAL == 0) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            serverLevel.sendParticles(
                    CoralineParticles.ORB_SPARKLE.get(),
                    this.getX(), this.getY() + 0.5D, this.getZ(),
                    2,
                    0.4D, 0.2D, 0.4D,
                    0.01D
            );
        }

        if (!this.level().isClientSide && this.isAlive() && !this.isDeadOrDying()
                && this.tickCount % DESPAWN_CHECK_INTERVAL == 0) {
            long dayTime = this.level().getDayTime() % 24000L;
            if (dayTime < SPAWN_WINDOW_START || dayTime > SPAWN_WINDOW_END) {
                this.despawnWithoutLoot();
            }
        }
    }

    private void despawnWithoutLoot() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        serverLevel.sendParticles(
                ParticleTypes.POOF,
                this.getX(), this.getY() + 0.5D, this.getZ(),
                20,
                0.3D, 0.3D, 0.3D,
                0.05D
        );
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                CoralineSounds.ORB_DESPAWN.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        this.discard();
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    this.getX(), this.getY() + 0.5D, this.getZ(),
                    12,
                    0.3D, 0.3D, 0.3D,
                    0.02D
            );
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    CoralineSounds.ORB_SPAWN.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    public boolean isHostile() {
        return this.entityData.get(DATA_IS_HOSTILE);
    }

    public void setHostile(boolean hostile) {
        this.entityData.set(DATA_IS_HOSTILE, hostile);
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {
        boolean wasHostileBeforeHit = this.isHostile();
        boolean damaged = super.hurt(damageSource, amount);

        if (damaged && !this.level().isClientSide && !wasHostileBeforeHit) {
            this.setHostile(true);
            if (this.isAlive()) {
                this.setHealth(PROVOKED_HEALTH);
                this.playForceFieldBurst(0.0F, 14.0F, 0.9F, 0.0F);
            }
        }

        return damaged;
    }

    public boolean isCritical() {
        return this.getHealth() <= CRITICAL_HEALTH;
    }

    private void playForceFieldBurst(float startSize, float endSize, float startAlpha, float endAlpha) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        serverLevel.sendParticles(
                new OrbShieldBurstData(10, startSize, endSize, startAlpha, endAlpha),
                this.getX(), this.getY() + 0.5D, this.getZ(),
                1,
                0.0D, 0.0D, 0.0D,
                0.0D
        );
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                CoralineSounds.ORB_FORCE_FIELD_BURST.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            this.playForceFieldBurst(0.0F, 10.0F, 1.0F, 0.0F);
            serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    this.getX(), this.getY() + 0.5D, this.getZ(),
                    20,
                    0.3D, 0.3D, 0.3D,
                    0.05D
            );
        }
        super.die(damageSource);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return CoralineSounds.ORB_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return CoralineSounds.ORB_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CoralineSounds.ORB_DEATH.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_CHARGING, false);
        this.entityData.define(DATA_IS_HOSTILE, false);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_IS_CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(DATA_IS_CHARGING, charging);
    }

    public static boolean checkOrbSpawnRules(EntityType<OrbEntity> entityType,
                                             ServerLevelAccessor level,
                                             MobSpawnType spawnType,
                                             BlockPos pos,
                                             RandomSource random) {
        long dayTime = level.getLevel().getDayTime() % 24000L;
        return dayTime >= SPAWN_WINDOW_START && dayTime <= SPAWN_WINDOW_END;
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(
            @NotNull ServerLevelAccessor level,
            @NotNull DifficultyInstance difficulty,
            @NotNull MobSpawnType reason,
            SpawnGroupData spawnData,
            CompoundTag dataTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);

        if (reason == MobSpawnType.NATURAL) {
            BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPosition());
            int hoverOffset = HOVER_OFFSET_MIN + level.getRandom().nextInt(HOVER_OFFSET_RANGE);
            this.moveTo(this.getX(), groundPos.getY() + hoverOffset, this.getZ(), this.getYRot(), this.getXRot());
        }

        return result;
    }

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
                    Vec3 vector = new Vec3(
                            this.wantedX - this.orb.getX(),
                            this.wantedY - this.orb.getY(),
                            this.wantedZ - this.orb.getZ()
                    );
                    double distance = vector.length();
                    vector = vector.normalize();
                    if (this.canReach(vector, Mth.ceil(distance))) {
                        this.orb.setDeltaMovement(this.orb.getDeltaMovement().add(vector.scale(0.035D)));
                    } else {
                        this.operation = MoveControl.Operation.WAIT;
                    }
                }
            }
        }

        private boolean canReach(Vec3 vector, int distanceCeil) {
            AABB boundingBox = this.orb.getBoundingBox();
            for (int i = 1; i < distanceCeil; ++i) {
                boundingBox = boundingBox.move(vector);
                if (!this.orb.level().noCollision(this.orb, boundingBox)) {
                    return false;
                }
            }
            return true;
        }
    }

    static class OrbDescendGoal extends Goal {
        private final OrbEntity orb;
        private boolean descending;

        public OrbDescendGoal(OrbEntity orb) {
            this.orb = orb;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.orb.tickCount % ALTITUDE_CHECK_INTERVAL != 0) {
                return this.descending;
            }
            this.descending = this.heightAboveGround() >= DESCEND_TRIGGER_HEIGHT;
            return this.descending;
        }

        @Override
        public boolean canContinueToUse() {
            return this.descending && this.heightAboveGround() > DESCEND_TARGET_HEIGHT;
        }

        @Override
        public void start() {
            this.descending = true;
        }

        @Override
        public void stop() {
            this.descending = false;
        }

        @Override
        public void tick() {
            double targetY = this.groundY() + DESCEND_TARGET_HEIGHT;
            this.orb.getMoveControl().setWantedPosition(this.orb.getX(), targetY, this.orb.getZ(), 0.8D);
        }

        private double heightAboveGround() {
            return this.orb.getY() - this.groundY();
        }

        private double groundY() {
            BlockPos pos = this.orb.blockPosition();
            return this.orb.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY();
        }
    }

    static class OrbRandomFloatAroundGoal extends Goal {
        private final OrbEntity orb;

        public OrbRandomFloatAroundGoal(OrbEntity orb) {
            this.orb = orb;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            MoveControl mc = this.orb.getMoveControl();
            if (!mc.hasWanted()) return true;
            double dX = mc.getWantedX() - this.orb.getX();
            double dY = mc.getWantedY() - this.orb.getY();
            double dZ = mc.getWantedZ() - this.orb.getZ();
            double distSqr = dX * dX + dY * dY + dZ * dZ;
            return distSqr < 1.0D || distSqr > 3600.0D;
        }

        public boolean canContinueToUse() { return false; }

        public void start() {
            RandomSource random = this.orb.getRandom();
            double targetX = this.orb.getX() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            double targetY = this.orb.getY() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            double targetZ = this.orb.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            this.orb.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 0.8D);
        }
    }

    static class OrbLookGoal extends Goal {
        private final OrbEntity orb;

        public OrbLookGoal(OrbEntity orb) {
            this.orb = orb;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() { return true; }
        public boolean requiresUpdateEveryTick() { return true; }

        public void tick() {
            if (this.orb.getTarget() == null) {
                Vec3 v = this.orb.getDeltaMovement();
                this.orb.setYRot(-((float) Mth.atan2(v.x, v.z)) * 57.295776F);
                this.orb.yBodyRot = this.orb.getYRot();
            } else {
                LivingEntity target = this.orb.getTarget();
                if (target.distanceToSqr(this.orb) < 4096.0D) {
                    double dX = target.getX() - this.orb.getX();
                    double dZ = target.getZ() - this.orb.getZ();
                    this.orb.setYRot(-((float) Mth.atan2(dX, dZ)) * 57.295776F);
                    this.orb.yBodyRot = this.orb.getYRot();
                }
            }
        }
    }

    static class OrbShootFireballGoal extends Goal {
        private final OrbEntity orb;
        public int chargeTime;

        private static final int CHARGE_SOUND_TICK = 20;
        private static final int CHARGE_FIRE_TICK = 40;
        private static final int POST_FIRE_COOLDOWN = -100;

        public OrbShootFireballGoal(OrbEntity orb) { this.orb = orb; }

        public boolean canUse() { return this.orb.isHostile() && this.orb.getTarget() != null; }
        public void start() { this.chargeTime = 0; }
        public void stop() { this.orb.setCharging(false); }
        public boolean requiresUpdateEveryTick() { return true; }

        public void tick() {
            LivingEntity target = this.orb.getTarget();
            if (target != null) {
                double distSqr = this.orb.distanceToSqr(target);
                if (distSqr < 4096.0D && this.orb.hasLineOfSight(target)) {
                    Level level = this.orb.level();
                    ++this.chargeTime;

                    if (this.chargeTime == CHARGE_SOUND_TICK && !this.orb.isSilent()) {
                        level.playSound(null, this.orb.getX(), this.orb.getY(), this.orb.getZ(),
                                CoralineSounds.ORB_CHARGE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                    }

                    if (this.chargeTime == CHARGE_FIRE_TICK) {
                        double dX = target.getX() - this.orb.getX();
                        double dY = target.getY(0.5D) - this.orb.getY(0.5D);
                        double dZ = target.getZ() - this.orb.getZ();

                        if (!this.orb.isSilent()) {
                            level.playSound(null, this.orb.getX(), this.orb.getY(), this.orb.getZ(),
                                    CoralineSounds.ORB_SHOOT.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                        }

                        OrbPulseEntity pulse = new OrbPulseEntity(level, this.orb, dX, dY, dZ);
                        level.addFreshEntity(pulse);
                        this.chargeTime = POST_FIRE_COOLDOWN;
                    }
                } else if (this.chargeTime > 0) {
                    --this.chargeTime;
                }
                this.orb.setCharging(this.chargeTime > CHARGE_SOUND_TICK);
            }
        }
    }
}