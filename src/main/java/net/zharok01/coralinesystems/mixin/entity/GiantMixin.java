package net.zharok01.coralinesystems.mixin.entity;

import com.legacy.rediscovered.entity.pigman.PigmanEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.zharok01.coralinesystems.entity.GiantAttackCooldownAccess;
import net.zharok01.coralinesystems.entity.GiantJammingAccess;
import net.zharok01.coralinesystems.entity.StarEntity;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Mixin(Giant.class)
public abstract class GiantMixin extends Monster implements GiantJammingAccess, GiantAttackCooldownAccess {
    @Unique
    private static final int ATTACK_COOLDOWN_TICKS = 40;

    @Unique
    private int coraline$dancingDuration;

    @Unique
    private int coraline$attackCooldownTicks;

    @Unique
    private boolean coraline$attackAnimationWasActive;

    @Unique
    private boolean coraline$cooldownFromStomp;

    protected GiantMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return CoralineSounds.GIANT_AMBIENT.get();
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return CoralineSounds.GIANT_HURT.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return CoralineSounds.GIANT_DEATH.get();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityType<? extends Giant> type, Level level, CallbackInfo ci) {
        Giant giant = (Giant) (Object) this;
        giant.getEntityData().define(GiantJammingAccess.coraline$IS_JAMMING, false);
        giant.getEntityData().define(GiantAttackCooldownAccess.coraline$KICK_ANIMATION_BLOCKED, false);

        Objects.requireNonNull(giant.getAttribute(Attributes.MOVEMENT_SPEED))
                .setBaseValue(0.25D);

        Objects.requireNonNull(giant.getAttribute(Attributes.KNOCKBACK_RESISTANCE))
                .setBaseValue(1.0D);

        boolean[] isDistracted = new boolean[1];

        Goal starGazeGoal = new Goal() {
            private StarEntity targetStar;
            private int cooldown;

            {
                this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
            }

            @Override
            public boolean canUse() {
                if (giant.getTarget() != null && giant.getRandom().nextInt(20) != 0) return false;

                if (giant.getRandom().nextInt(50) != 0) return false;

                List<StarEntity> stars = giant.level().getEntitiesOfClass(StarEntity.class, giant.getBoundingBox().inflate(48.0D));
                if (stars.isEmpty()) return false;

                this.targetStar = stars.get(giant.getRandom().nextInt(stars.size()));
                return true;
            }

            @Override
            public void start() {
                this.cooldown = 100 + giant.getRandom().nextInt(100);
                giant.setTarget(null);
                isDistracted[0] = true;
            }

            @Override
            public boolean canContinueToUse() {
                if (giant.hurtTime > 0) return false;

                return this.cooldown > 0 && this.targetStar != null && this.targetStar.isAlive() && giant.distanceToSqr(this.targetStar) < 2500.0D;
            }

            @Override
            public void tick() {
                this.cooldown--;
                giant.getLookControl().setLookAt(this.targetStar, 30.0F, 30.0F);
                giant.setTarget(null);

                if (giant.getRandom().nextInt(20) == 0 && giant.distanceToSqr(this.targetStar) > 64.0D) {
                    giant.getNavigation().moveTo(this.targetStar, 1.0D);
                }
            }

            @Override
            public void stop() {
                this.targetStar = null;
                isDistracted[0] = false;
                giant.getNavigation().stop();
            }
        };

        this.goalSelector.addGoal(3, starGazeGoal);
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(giant, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(giant, Player.class, 32.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(giant));

        Predicate<LivingEntity> notDistracted = entity -> !isDistracted[0];

        this.targetSelector.addGoal(1, new HurtByTargetGoal(giant));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(giant, Player.class, 10, true, false, notDistracted));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(giant, PigmanEntity.class, 10, true, false, notDistracted));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && this.coraline$isJamming()) {
            return;
        }

        super.setTarget(target);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        return false;
    }

    @Override
    @Unique
    public boolean coraline$hasAttackCooldown() {
        return this.coraline$attackCooldownTicks > 0;
    }

    @Override
    @Unique
    public boolean coraline$isKickAnimationBlocked() {
        return this.entityData.get(GiantAttackCooldownAccess.coraline$KICK_ANIMATION_BLOCKED);
    }

    @Override
    @Unique
    public void coraline$startStompCooldown() {
        this.coraline$attackCooldownTicks = ATTACK_COOLDOWN_TICKS;
        this.coraline$cooldownFromStomp = true;
        this.entityData.set(GiantAttackCooldownAccess.coraline$KICK_ANIMATION_BLOCKED, true);
    }

    @Override
    @Unique
    public void coraline$tickAttackCooldown() {
        boolean attackAnimationActive = this.getAttackAnim(1.0F) > 0.0F;
        if (attackAnimationActive && !this.coraline$attackAnimationWasActive && !this.coraline$hasAttackCooldown()) {
            this.coraline$attackCooldownTicks = ATTACK_COOLDOWN_TICKS;
            this.coraline$cooldownFromStomp = false;
        }
        this.coraline$attackAnimationWasActive = attackAnimationActive;

        if (this.coraline$hasAttackCooldown()) {
            this.coraline$attackCooldownTicks--;
        }
        if (!this.coraline$hasAttackCooldown()) {
            this.coraline$cooldownFromStomp = false;
        }

        boolean blockKickAnimation = this.coraline$hasAttackCooldown()
                && (this.coraline$cooldownFromStomp || !attackAnimationActive);
        this.entityData.set(GiantAttackCooldownAccess.coraline$KICK_ANIMATION_BLOCKED, blockKickAnimation);
    }

    @Override
    @Unique
    public boolean coraline$isJamming() {
        return this.entityData.get(GiantJammingAccess.coraline$IS_JAMMING);
    }

    @Override
    @Unique
    public void coraline$setDancingDuration(int duration) {
        this.coraline$dancingDuration = duration;
        this.entityData.set(GiantJammingAccess.coraline$IS_JAMMING, duration > 0);
    }

    @Override
    @Unique
    public void coraline$tickMusicJamming() {
        Giant giant = (Giant) (Object) this;

        if (this.coraline$dancingDuration > 0) {
            this.coraline$dancingDuration--;
            if (this.coraline$dancingDuration <= 0) {
                this.entityData.set(GiantJammingAccess.coraline$IS_JAMMING, false);
            }
        }

        if (!this.coraline$isJamming() && this.tickCount % 20 == 0) {
            double scanRadius = 64.0D;
            int centerChunkX = SectionPos.blockToSectionCoord(this.getBlockX());
            int centerChunkZ = SectionPos.blockToSectionCoord(this.getBlockZ());
            int chunkRadius = (int) Math.ceil(scanRadius / 16.0D);
            boolean foundPlaying = false;

            outer:
            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    LevelChunk chunk = this.level().getChunkSource().getChunkNow(centerChunkX + dx, centerChunkZ + dz);
                    if (chunk == null) {
                        continue;
                    }

                    for (var blockEntity : chunk.getBlockEntities().values()) {
                        if (blockEntity instanceof JukeboxBlockEntity jukebox && jukebox.isRecordPlaying()) {
                            BlockPos jukeboxPos = jukebox.getBlockPos();
                            double distanceX = this.getBlockX() - jukeboxPos.getX();
                            double distanceY = this.getBlockY() - jukeboxPos.getY();
                            double distanceZ = this.getBlockZ() - jukeboxPos.getZ();
                            if (distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ < scanRadius * scanRadius) {
                                foundPlaying = true;
                                break outer;
                            }
                        }
                    }
                }
            }

            if (foundPlaying) {
                this.entityData.set(GiantJammingAccess.coraline$IS_JAMMING, true);
            }
        }

        if (this.coraline$isJamming()) {
            giant.setTarget(null);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);

        this.setYRot(Mth.wrapDegrees(level.getRandom().nextFloat() * 360.0F));
        this.yHeadRot = this.getYRot();
        this.yBodyRot = this.getYRot();

        return data;
    }
}