package net.zharok01.coralinesystems.entity;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
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
import net.zharok01.coralinesystems.entity.ai.HelperJumpForJoyGoal;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HelperEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> DATA_SKIN_ID = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_GLITCHING = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_JAMMING = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.BOOLEAN);

    // Radius (in blocks) Helpers scan for a playing Jukebox.
    private static final double JUKEBOX_SCAN_RADIUS = 64.0D;

    // Interval (in ticks) for the walk-in / liveness poll. This ONLY turns
    // jamming ON for Helpers that are not already jamming (late walk-ins that
    // missed the JukeboxBlockEntityMixin#startPlaying trigger, or stragglers
    // that wandered into range). It never touches dancingDuration for a
    // Helper that's already jamming, so it can't clobber the real countdown
    // that the mixin set from the record's actual length.
    private static final int JUKEBOX_POLL_INTERVAL = 20;

    public int dancingDuration = 0; // Tracks the exact length of the song

    public void setDancingDuration(int duration) {
        this.dancingDuration = duration;
        this.setJamming(duration > 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Glitch Logic
            if (this.random.nextFloat() < 0.005F && !this.isGlitching()) {
                this.setGlitching(true);
                if (CoralineSounds.STATIC_BUZZ.isPresent()) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            CoralineSounds.STATIC_BUZZ.get(), this.getSoundSource(), 1.0F, 1.0F);
                }

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(AMParticleRegistry.STATIC_SPARK.get(),
                            this.getX(), this.getY() + 1.0D, this.getZ(),
                            10, 0.2, 0.5, 0.2, 0.02);
                }
            }

            if (this.isGlitching() && this.tickCount % 10 == 0) {
                this.setGlitching(false);
            }

            // Tick down the dancing duration. This is the ONLY place dancingDuration
            // is decremented, and it is the authoritative countdown set once by
            // JukeboxBlockEntityMixin#startPlaying from the record's real length.
            // Nothing else is allowed to overwrite it while it's still counting down.
            if (this.dancingDuration > 0) {
                this.dancingDuration--;
                if (this.dancingDuration <= 0) {
                    this.setJamming(false);
                }
            }

            // Walk-in / liveness poll — fires every 20 ticks (1 second).
            // Only acts on Helpers that are NOT currently jamming: if a playing
            // Jukebox is found within JUKEBOX_SCAN_RADIUS, flip them into the
            // jamming state immediately (does not touch dancingDuration, so it
            // never fights the mixin-driven countdown for Helpers already dancing).
            // Once flipped, the Helper rides along with whatever real Jukebox
            // eventually stops (via the mixin's stopPlaying hook) rather than a
            // guessed duration.
            if (!this.isJamming() && this.tickCount % JUKEBOX_POLL_INTERVAL == 0) {
                boolean foundPlaying = false;

                // JUKEBOX_SCAN_RADIUS (64 blocks) can span multiple chunks, so we
                // walk every chunk the radius touches rather than only this
                // Helper's own chunk (which would silently miss jukeboxes in
                // neighboring chunks at this range).
                int centerChunkX = net.minecraft.core.SectionPos.blockToSectionCoord(this.getBlockX());
                int centerChunkZ = net.minecraft.core.SectionPos.blockToSectionCoord(this.getBlockZ());
                int chunkRadius = (int) Math.ceil(JUKEBOX_SCAN_RADIUS / 16.0D);

                outer:
                for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                    for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                        net.minecraft.world.level.chunk.LevelChunk chunk =
                                this.level().getChunkSource().getChunkNow(centerChunkX + dx, centerChunkZ + dz);
                        if (chunk == null) {
                            continue; // not loaded — nothing to scan
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinId", this.getSkinId());
        tag.putInt("DancingDuration", this.dancingDuration);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinId", 99)) {
            this.setSkinId(tag.getInt("SkinId"));
        }
        if (tag.contains("DancingDuration", 99)) {
            this.setDancingDuration(tag.getInt("DancingDuration"));
        }
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity target) {
        // Neutral (not hostile) toward everyone — Players and Piglins alike —
        // while jamming to a Jukebox.
        if (this.isJamming()) {
            return false;
        }

        //Helpers don't attack each other!
        if (target instanceof HelperEntity) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        // If they are jamming and something tries to set a target (like getting hit),
        // we force it back to null immediately.
        if (this.isJamming()) {
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

            // Guard matches the same check used in tick() — prevents crash if unregistered
            if (CoralineSounds.STATIC_BUZZ.isPresent()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        CoralineSounds.STATIC_BUZZ.get(), this.getSoundSource(), 1.0F, 1.0F);
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(AMParticleRegistry.STATIC_SPARK.get(),
                        this.getX(), this.getY() + 1.0D, this.getZ(),
                        10, 0.2, 0.5, 0.2, 0.02);
            }
        }

        return super.hurt(source, amount);
    }

    //Make Helpers pass through Cobwebs:
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new HelperJumpForJoyGoal(this));

        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                // Added check: If jamming, we can't use combat goals
                if (HelperEntity.this.isJamming()) return false;
                if (!super.canUse()) return false;
                return HelperEntity.this.getTarget() != null &&
                        // distanceToSqr <= 144.0 -> sqrt(144) = 12 blocks actual distance
                        HelperEntity.this.distanceToSqr(HelperEntity.this.getTarget()) <= 144.0D;
            }

            @Override
            public boolean canContinueToUse() {
                if (HelperEntity.this.isJamming()) return false;
                // Small hysteresis band above the engage range (13 blocks) so the goal
                // doesn't flicker in/out right at the 12-block edge.
                return super.canContinueToUse() &&
                        HelperEntity.this.distanceToSqr(HelperEntity.this.getTarget()) <= 169.0D;
            }
        });

        // Priority 3: RANGED (Only triggers if distance > 12 blocks, matching the new melee range)
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0D, 40, 20.0F) {
            @Override
            public boolean canUse() {
                if (HelperEntity.this.isJamming()) return false;
                if (!super.canUse()) return false;
                return HelperEntity.this.getTarget() != null &&
                        HelperEntity.this.distanceToSqr(HelperEntity.this.getTarget()) > 144.0D;
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

        if (tag == null || !tag.contains("SkinId")) {
            //Change this "nextInt(_)" to a desired number of skin variants:
            this.setSkinId(level.getRandom().nextInt(6));
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

    public int getSkinId() { return this.entityData.get(DATA_SKIN_ID); }
    public void setSkinId(int id) { this.entityData.set(DATA_SKIN_ID, id); }

    public static boolean checkHelperSpawnRules(EntityType<HelperEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }
}