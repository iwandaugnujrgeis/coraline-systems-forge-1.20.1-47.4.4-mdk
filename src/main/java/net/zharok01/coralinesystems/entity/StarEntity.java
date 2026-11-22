package net.zharok01.coralinesystems.entity;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class StarEntity extends FlyingMob {

    private static final long SPAWN_WINDOW_START = 13000L;
    private static final long SPAWN_WINDOW_END = 22299L;
    private static final int DESPAWN_CHECK_INTERVAL = 120;

    private static final int AMBIENT_PARTICLE_INTERVAL = 4;

    private static final double DESCEND_TRIGGER_HEIGHT = 25.0D;
    private static final double DESCEND_TARGET_HEIGHT = 20.0D;
    private static final int ALTITUDE_CHECK_INTERVAL = 100;

    private static final double ABSOLUTE_MIN_Y = 45.0D;

    private static final int HOVER_OFFSET_MIN = 10;
    private static final int HOVER_OFFSET_RANGE = 11;

    public StarEntity(EntityType<? extends StarEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
        this.moveControl = new StarMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new StarDescendGoal(this));
        this.goalSelector.addGoal(5, new StarRandomFloatAroundGoal(this));
        this.goalSelector.addGoal(7, new StarLookGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return FlyingMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FLYING_SPEED, 0.08D)
                .add(Attributes.MOVEMENT_SPEED, 0.08D);
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

        if (!this.level().isClientSide && this.getY() < ABSOLUTE_MIN_Y) {
            this.setPos(this.getX(), ABSOLUTE_MIN_Y, this.getZ());
            Vec3 vel = this.getDeltaMovement();
            if (vel.y < 0) {
                this.setDeltaMovement(vel.x, 0.0D, vel.z);
            }
        }

        if (!this.level().isClientSide && this.tickCount % AMBIENT_PARTICLE_INTERVAL == 0) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            serverLevel.sendParticles(
                    CoralineParticles.STAR_SPARKLE.get(),
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
                this.coraline$despawnWithoutLoot();
            }
        }
    }

    private void coraline$despawnWithoutLoot() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        serverLevel.sendParticles(
                AMParticleRegistry.STATIC_SPARK.get(),
                this.getX(), this.getY() + 0.5D, this.getZ(),
                20,
                0.3D, 0.3D, 0.3D,
                0.05D
        );
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                CoralineSounds.ORB_DESPAWN.get(), SoundSource.NEUTRAL, 0.6F, 1.2F);
        this.discard();
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            serverLevel.sendParticles(
                    AMParticleRegistry.STATIC_SPARK.get(),
                    this.getX(), this.getY() + 0.5D, this.getZ(),
                    8,
                    0.2D, 0.2D, 0.2D,
                    0.02D
            );
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    CoralineSounds.ORB_SPAWN.get(), SoundSource.NEUTRAL, 0.6F, 1.4F);
        }
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    this.getX(), this.getY() + 0.5D, this.getZ(),
                    15,
                    0.3D, 0.3D, 0.3D,
                    0.05D
            );
        }
        super.die(damageSource);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return CoralineSounds.STAR_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return CoralineSounds.ORB_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CoralineSounds.ORB_DEATH.get();
    }

    public static boolean checkStarSpawnRules(EntityType<StarEntity> entityType,
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
            double spawnY = Math.max(groundPos.getY() + hoverOffset, ABSOLUTE_MIN_Y);
            this.moveTo(this.getX(), spawnY, this.getZ(), this.getYRot(), this.getXRot());
        }

        return result;
    }

    static class StarMoveControl extends MoveControl {
        private final StarEntity star;
        private int floatDuration;

        public StarMoveControl(StarEntity star) {
            super(star);
            this.star = star;
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                if (this.floatDuration-- <= 0) {
                    this.floatDuration += this.star.getRandom().nextInt(5) + 2;
                    Vec3 vector = new Vec3(
                            this.wantedX - this.star.getX(),
                            this.wantedY - this.star.getY(),
                            this.wantedZ - this.star.getZ()
                    );
                    double distance = vector.length();
                    vector = vector.normalize();
                    if (this.coraline$canReach(vector, Mth.ceil(distance))) {
                        this.star.setDeltaMovement(this.star.getDeltaMovement().add(vector.scale(0.035D)));
                    } else {
                        this.operation = MoveControl.Operation.WAIT;
                    }
                }
            }
        }

        private boolean coraline$canReach(Vec3 vector, int distanceCeil) {
            AABB boundingBox = this.star.getBoundingBox();
            for (int i = 1; i < distanceCeil; ++i) {
                boundingBox = boundingBox.move(vector);
                if (!this.star.level().noCollision(this.star, boundingBox)) {
                    return false;
                }
            }
            return true;
        }
    }

    static class StarDescendGoal extends Goal {
        private final StarEntity star;
        private boolean descending;

        public StarDescendGoal(StarEntity star) {
            this.star = star;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.star.tickCount % ALTITUDE_CHECK_INTERVAL != 0) {
                return this.descending;
            }
            this.descending = this.coraline$heightAboveGround() >= DESCEND_TRIGGER_HEIGHT;
            return this.descending;
        }

        @Override
        public boolean canContinueToUse() {
            return this.descending && this.coraline$heightAboveGround() > DESCEND_TARGET_HEIGHT;
        }

        @Override
        public void start() { this.descending = true; }

        @Override
        public void stop() { this.descending = false; }

        @Override
        public void tick() {
            double targetY = Math.max(this.coraline$groundY() + DESCEND_TARGET_HEIGHT, ABSOLUTE_MIN_Y);
            this.star.getMoveControl().setWantedPosition(this.star.getX(), targetY, this.star.getZ(), 0.8D);
        }

        private double coraline$heightAboveGround() {
            return this.star.getY() - this.coraline$groundY();
        }

        private double coraline$groundY() {
            BlockPos pos = this.star.blockPosition();
            return this.star.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY();
        }
    }

    static class StarRandomFloatAroundGoal extends Goal {
        private final StarEntity star;

        public StarRandomFloatAroundGoal(StarEntity star) {
            this.star = star;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl mc = this.star.getMoveControl();
            if (!mc.hasWanted()) return true;
            double dX = mc.getWantedX() - this.star.getX();
            double dY = mc.getWantedY() - this.star.getY();
            double dZ = mc.getWantedZ() - this.star.getZ();
            double distSqr = dX * dX + dY * dY + dZ * dZ;
            return distSqr < 1.0D || distSqr > 3600.0D;
        }

        @Override
        public boolean canContinueToUse() { return false; }

        @Override
        public void start() {
            RandomSource random = this.star.getRandom();
            double targetX = this.star.getX() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            double targetY = Math.max(
                    this.star.getY() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F,
                    ABSOLUTE_MIN_Y
            );
            double targetZ = this.star.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            this.star.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 0.8D);
        }
    }

    static class StarLookGoal extends Goal {
        private final StarEntity star;

        public StarLookGoal(StarEntity star) {
            this.star = star;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() { return true; }

        @Override
        public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void tick() {
            Vec3 v = this.star.getDeltaMovement();
            this.star.setYRot(-((float) Mth.atan2(v.x, v.z)) * 57.295776F);
            this.star.yBodyRot = this.star.getYRot();
        }
    }
}