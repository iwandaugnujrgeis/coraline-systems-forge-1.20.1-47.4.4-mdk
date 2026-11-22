package net.zharok01.coralinesystems.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.zharok01.coralinesystems.entity.ai.CryptidFleePlayerGoal;
import net.zharok01.coralinesystems.entity.ai.CryptidKnockOnWoodGoal;
import net.zharok01.coralinesystems.entity.ai.CryptidThrowPlayerGoal;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CryptidEntity extends Monster {

    private static final EntityDataAccessor<Boolean> EYE_IS_GREEN =
            SynchedEntityData.defineId(CryptidEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> THROW_ANIM_TICKS =
            SynchedEntityData.defineId(CryptidEntity.class, EntityDataSerializers.INT);

    public static final int THROW_ANIM_DURATION = 20;

    private boolean fleeing = false;

    public CryptidEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CryptidThrowPlayerGoal(this));
        this.goalSelector.addGoal(2, new CryptidFleePlayerGoal(this));
        this.goalSelector.addGoal(3, new CryptidKnockOnWoodGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EYE_IS_GREEN, true);
        this.entityData.define(THROW_ANIM_TICKS, 0);
    }

    public boolean hasGreenEye() {
        return this.entityData.get(EYE_IS_GREEN);
    }

    public void startThrowAnim() {
        this.entityData.set(THROW_ANIM_TICKS, THROW_ANIM_DURATION);
    }

    public float getThrowAnimProgress() {
        int ticks = this.entityData.get(THROW_ANIM_TICKS);
        if (ticks <= 0) return 0.0F;
        return (float) ticks / THROW_ANIM_DURATION;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            int ticks = this.entityData.get(THROW_ANIM_TICKS);
            if (ticks > 0) {
                this.entityData.set(THROW_ANIM_TICKS, ticks - 1);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level,
                                        @NotNull DifficultyInstance difficulty,
                                        @NotNull MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag tag) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);

        if (tag == null || !tag.contains("EyeIsGreen")) {
            this.entityData.set(EYE_IS_GREEN, level.getRandom().nextBoolean());
        }

        return spawnData;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("EyeIsGreen", this.hasGreenEye());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("EyeIsGreen")) {
            this.entityData.set(EYE_IS_GREEN, tag.getBoolean("EyeIsGreen"));
        }
    }

    public boolean isFleeing() {
        return this.fleeing;
    }

    public void setFleeing(boolean fleeing) {
        this.fleeing = fleeing;
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity target) {
        if (target instanceof Player) return false;
        return super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof Player) {
            super.setTarget(null);
        } else {
            super.setTarget(target);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return CoralineSounds.CRYPTID_IDLE.get();
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return CoralineSounds.CRYPTID_HURT.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return CoralineSounds.CRYPTID_DEATH.get();
    }

    public static boolean checkCryptidSpawnRules(EntityType<CryptidEntity> type,
                                                 ServerLevelAccessor level,
                                                 MobSpawnType spawnType,
                                                 BlockPos pos,
                                                 RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }
}