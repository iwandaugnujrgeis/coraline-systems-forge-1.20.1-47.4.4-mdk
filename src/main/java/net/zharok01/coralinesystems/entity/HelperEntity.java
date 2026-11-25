package net.zharok01.coralinesystems.entity;

import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.entity.ai.HelperFleePlayerGoal;
import net.zharok01.coralinesystems.entity.ai.HelperJumpForJoyGoal;
import net.zharok01.coralinesystems.event.static_portal.StaticPortalTeleporter;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HelperEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> DATA_SKIN_ID = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_GLITCHING = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_JAMMING = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean cowardly = true;

    private static final double JUKEBOX_SCAN_RADIUS = 64.0D;
    private static final int JUKEBOX_POLL_INTERVAL = 20;

    public int dancingDuration = 0;

    public boolean isCowardly() {
        return this.cowardly;
    }

    public void setDancingDuration(int duration) {
        this.dancingDuration = duration;
        this.setJamming(duration > 0);
    }

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

                this.coraline$spawnDarkSmokeBurst(10);
            }

            if (this.isGlitching() && this.tickCount % 10 == 0) {
                this.setGlitching(false);
            }

            if (this.dancingDuration > 0) {
                this.dancingDuration--;
                if (this.dancingDuration <= 0) {
                    this.setJamming(false);
                }
            }

            if (!this.isJamming() && this.tickCount % JUKEBOX_POLL_INTERVAL == 0) {
                boolean foundPlaying = false;
                int centerChunkX = net.minecraft.core.SectionPos.blockToSectionCoord(this.getBlockX());
                int centerChunkZ = net.minecraft.core.SectionPos.blockToSectionCoord(this.getBlockZ());
                int chunkRadius = (int) Math.ceil(JUKEBOX_SCAN_RADIUS / 16.0D);

                outer:
                for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                    for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                        net.minecraft.world.level.chunk.LevelChunk chunk =
                                this.level().getChunkSource().getChunkNow(centerChunkX + dx, centerChunkZ + dz);
                        if (chunk == null) {
                            continue;
                        }

                        for (net.minecraft.world.level.block.entity.BlockEntity be : chunk.getBlockEntities().values()) {
                            if (be instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity jukebox
                                    && jukebox.isRecordPlaying()
                                    && be.getBlockPos().closerThan(this.blockPosition(), JUKEBOX_SCAN_RADIUS)) {
                                foundPlaying = true;
                                break outer;
                            }
                        }
                    }
                }

                if (foundPlaying) {
                    this.setJamming(true);
                }
            }
        }
    }

    public void setJamming(boolean jamming) {
        this.entityData.set(IS_JAMMING, jamming);
    }

    public boolean isJamming() {
        return this.entityData.get(IS_JAMMING);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinId", this.getSkinId());
        tag.putInt("DancingDuration", this.dancingDuration);
        tag.putBoolean("Cowardly", this.cowardly);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinId", 99)) {
            this.setSkinId(tag.getInt("SkinId"));
        }
        if (tag.contains("DancingDuration", 99)) {
            this.setDancingDuration(tag.getInt("DancingDuration"));
        }
        if (tag.contains("Cowardly")) {
            this.cowardly = tag.getBoolean("Cowardly");
        }
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity target) {
        if (this.isJamming()) return false;
        if (target instanceof HelperEntity) return false;
        if (target instanceof Player && this.cowardly) return false;
        return super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isJamming()) {
            super.setTarget(null);
        } else if (target instanceof Player && this.cowardly) {
            super.setTarget(null);
        } else {
            super.setTarget(target);
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
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!this.level().isClientSide) {
            this.setGlitching(true);

            if (CoralineSounds.STATIC_BUZZ.isPresent()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        CoralineSounds.STATIC_BUZZ.get(), this.getSoundSource(), 1.0F, 1.0F);
            }

            this.coraline$spawnDarkSmokeBurst(10);
        }

        return super.hurt(source, amount);
    }

    private void coraline$spawnDarkSmokeBurst(int count) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(CoralineParticles.DARK_SMOKE.get(),
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    count, 0.35D, 0.9D, 0.35D, 0.05D);
        }
    }

    public void makeStuckInBlock(BlockState state, @NotNull Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HelperJumpForJoyGoal(this));
        this.goalSelector.addGoal(2, new HelperFleePlayerGoal(this));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0D, 40, 20.0F) {
            @Override
            public boolean canUse() {
                if (HelperEntity.this.isJamming()) return false;
                return super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                if (HelperEntity.this.isJamming()) return false;
                return super.canContinueToUse();
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
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return CoralineSounds.HELPER_HURT.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return CoralineSounds.HELPER_DEATH.get();
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);

        if (tag == null || !tag.contains("SkinId")) {
            this.setSkinId(level.getRandom().nextInt(1));
        }

        if (tag == null || !tag.contains("Cowardly")) {
            this.cowardly = level.getRandom().nextFloat() >= 0.1F;
        }

        return spawnData;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKIN_ID, 0);
        this.entityData.define(IS_GLITCHING, false);
        this.entityData.define(IS_JAMMING, false);
    }

    public int getSkinId() {
        return this.entityData.get(DATA_SKIN_ID);
    }

    public void setSkinId(int id) {
        this.entityData.set(DATA_SKIN_ID, id);
    }

    public static boolean checkHelperSpawnRules(EntityType<HelperEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        boolean inFarlands = Math.abs(pos.getX()) >= StaticPortalTeleporter.FARLANDS_EDGE || Math.abs(pos.getZ()) >= StaticPortalTeleporter.FARLANDS_EDGE;
        return inFarlands && Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }
}