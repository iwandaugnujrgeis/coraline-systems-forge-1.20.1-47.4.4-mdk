package net.zharok01.coralinesystems.content.entity.custom;

import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import com.legacy.rediscovered.entity.pigman.PigmanEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.zharok01.coralinesystems.content.sound.CoralineSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HelperEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> DATA_SKIN_ID = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_GLITCHING = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.random.nextFloat() < 0.005F && !this.isGlitching()) {
                this.setGlitching(true);
                if (CoralineSounds.STATIC_BUZZ.isPresent()) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            CoralineSounds.STATIC_BUZZ.get(), this.getSoundSource(), 1.0F, 1.0F);
                }
            }

            if (this.isGlitching() && this.tickCount % 10 == 0) {
                this.setGlitching(false);
            }
        }
    }

    public void setGlitching(boolean glitch) { this.entityData.set(IS_GLITCHING, glitch); }
    public boolean isGlitching() { return this.entityData.get(IS_GLITCHING); }
    public HelperEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!this.level().isClientSide) {
            this.setGlitching(true);

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    CoralineSounds.STATIC_BUZZ.get(), this.getSoundSource(), 1.0F, 1.0F);
        }

        return super.hurt(source, amount);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                if (!super.canUse()) return false;
                assert HelperEntity.this.getTarget() != null;
                return HelperEntity.this.distanceToSqr(HelperEntity.this.getTarget()) <= 32.0D;
            }

            @Override
            public boolean canContinueToUse() {
                // Stop chasing for a punch if the player gets further than 5 blocks
                if (!super.canContinueToUse()) return false;
                assert HelperEntity.this.getTarget() != null;
                return HelperEntity.this.distanceToSqr(HelperEntity.this.getTarget()) <= 36.0D;
            }
        });

        // Priority 3: Ranged Attack (Only triggers if further than 4 blocks)
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0D, 40, 20.0F) {
            @Override
            public boolean canUse() {
                if (!super.canUse()) return false;
                assert HelperEntity.this.getTarget() != null;
                return HelperEntity.this.distanceToSqr(HelperEntity.this.getTarget()) > 32.0D;
            }
        });

        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PigmanEntity.class, true));
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float velocity) {
        ThrowableBrickEntity brick = new ThrowableBrickEntity(this);

        brick.setItem(new ItemStack(Items.BRICK));

        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - brick.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        brick.shoot(d0, d1 + d3 * 0.2D, d2, 1.6F, 10.0F);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F,
                0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));

        this.level().addFreshEntity(brick);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return CoralineSounds.HELPER_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return CoralineSounds.HELPER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CoralineSounds.HELPER_DEATH.get();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);

        // Randomly pick 1 of 3 skins (0, 1, or 2)
        this.setSkinId(level.getRandom().nextInt(3));

        return spawnData;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKIN_ID, 0);
        this.entityData.define(IS_GLITCHING, false);
    }

    public int getSkinId() { return this.entityData.get(DATA_SKIN_ID); }
    public void setSkinId(int id) { this.entityData.set(DATA_SKIN_ID, id); }
}