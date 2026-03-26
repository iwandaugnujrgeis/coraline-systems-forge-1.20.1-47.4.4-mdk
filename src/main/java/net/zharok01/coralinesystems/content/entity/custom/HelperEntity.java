package net.zharok01.coralinesystems.content.entity.custom;

import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.content.entity.ai.HelperBreakBlockGoal;
import net.zharok01.coralinesystems.content.entity.ai.HelperPlaceBlockGoal;
import net.zharok01.coralinesystems.content.sound.CoralineSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class HelperEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> DATA_SKIN_ID = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_GLITCHING = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockState>> CARRIED_BLOCK = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> IS_JAMMING = SynchedEntityData.defineId(HelperEntity.class, EntityDataSerializers.BOOLEAN);

    public int dancingDuration = 0; // NEW: Tracks the exact length of the song

    public void setCarriedBlock(@Nullable BlockState state) {
        // If the state passed is null, we safely store Air instead
        BlockState nonNullState = (state == null) ? Blocks.AIR.defaultBlockState() : state;
        this.entityData.set(CARRIED_BLOCK, Optional.of(nonNullState));
    }

    public BlockState getCarriedBlock() {
        // We look into the Optional. If it's empty, we return Air.
        // This ensures the method NEVER returns null to the AI goal.
        return this.entityData.get(CARRIED_BLOCK).orElse(Blocks.AIR.defaultBlockState());
    }

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
            }

            if (this.isGlitching() && this.tickCount % 10 == 0) {
                this.setGlitching(false);
            }

            // NEW: Tick down the dancing duration
            if (this.dancingDuration > 0) {
                this.dancingDuration--;
                if (this.dancingDuration <= 0) {
                    this.setJamming(false);
                }
            }

            // NEW: 10-second (200 ticks) fallback check
            if (this.tickCount % 200 == 0 && this.isJamming()) {
                boolean foundPlaying = false;
                BlockPos pos = this.blockPosition();

                for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-16, -4, -16), pos.offset(16, 4, 16))) {
                    if (this.level().getBlockEntity(nearby) instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity jukebox) {
                        if (jukebox.isRecordPlaying()) {
                            foundPlaying = true;
                            break;
                        }
                    }
                }
                if (!foundPlaying) {
                    this.setDancingDuration(0);
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
        tag.putBoolean("IsGlitching", this.isGlitching());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinId", 99)) { // 99 is the ID for all Number types in NBT
            this.setSkinId(tag.getInt("SkinId"));
        }
        this.setGlitching(tag.getBoolean("IsGlitching"));
    }

    static class HelperTakeBlockGoal extends Goal {
        private final HelperEntity helper;

        public HelperTakeBlockGoal(HelperEntity helper) {
            this.helper = helper;
        }

        @Override
        public boolean canUse() {
            if (!this.helper.isJamming() || this.helper.getCarriedBlock() != null) {
                return false;
            } else if (!this.helper.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            } else {
                //60-tick check:
                return this.helper.getRandom().nextInt(reducedTickDelay(60)) == 0;
            }
        }

        @Override
        public void tick() {
            RandomSource random = this.helper.getRandom();
            Level level = this.helper.level();

            //Looks in an 8x3x8 area around the Helper:
            int i = Mth.floor(this.helper.getX() - 4.0D + random.nextDouble() * 8.0D);
            int j = Mth.floor(this.helper.getY() - 1.0D + random.nextDouble() * 3.0D);
            int k = Mth.floor(this.helper.getZ() - 4.0D + random.nextDouble() * 8.0D);

            BlockPos blockpos = new BlockPos(i, j, k);
            BlockState blockstate = level.getBlockState(blockpos);

            // Check if the block is something they should pick up
            // Inside HelperTakeBlockGoal -> tick()
            if (blockstate.is(BlockTags.DIRT) || blockstate.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                this.helper.setCarriedBlock(blockstate);
                level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);

                //The sounds of the blocks:
                this.helper.level().playSound(null, this.helper.blockPosition(),
                        blockstate.getSoundType().getHitSound(), // Use the block's own sound
                        SoundSource.NEUTRAL, 0.5F, 1.2F);
            }
        }
    }

    static class HelperLeaveBlockGoal extends Goal {
        private final HelperEntity helper;

        public HelperLeaveBlockGoal(HelperEntity helper) {
            this.helper = helper;
        }

        @Override
        public boolean canUse() {
            if (!this.helper.isJamming() || this.helper.getCarriedBlock() == null) {
                return false;
            } else if (!this.helper.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            } else {
                // Increased frequency: changed from 2000 to 40;
                // Now they will try to place it every 200 ticks:
                return this.helper.getRandom().nextInt(reducedTickDelay(200)) == 0;
            }
        }

        @Override
        public void tick() {
            RandomSource random = this.helper.getRandom();
            Level level = this.helper.level();

            // Searching for a place to put the block down:
            int i = Mth.floor(this.helper.getX() - 4.0D + random.nextDouble() * 8.0D);
            int j = Mth.floor(this.helper.getY() - 1.0D + random.nextDouble() * 3.0D);
            int k = Mth.floor(this.helper.getZ() - 4.0D + random.nextDouble() * 8.0D);

            BlockPos blockpos = new BlockPos(i, j, k);
            BlockState blockstate = level.getBlockState(blockpos);
            BlockPos belowPos = blockpos.below();
            BlockState belowState = level.getBlockState(belowPos);
            BlockState carried = this.helper.getCarriedBlock();

            // Placement conditions:
            // 1. Target block is AIR
            // 2. Block BELOW is SOLID
            if (carried != null && blockstate.isAir() && !belowState.isAir() && belowState.isSolidRender(level, belowPos)) {
                level.setBlock(blockpos, carried, 3);
                this.helper.setCarriedBlock(null);
            }
        }
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity target) {
        //Stop being hostile when the Jukebox is playing!
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

    //Make Helpers pass through Cobwebs:
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }

    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new HelperBreakBlockGoal(this));
        this.goalSelector.addGoal(1, new HelperTakeBlockGoal(this));
        this.goalSelector.addGoal(1, new HelperLeaveBlockGoal(this));
        this.goalSelector.addGoal(2, new HelperPlaceBlockGoal(this));

        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                // Added check: If jamming, we can't use combat goals
                if (HelperEntity.this.isJamming()) return false;
                if (!super.canUse()) return false;
                return HelperEntity.this.getTarget() != null &&
                        //Trigger melee if distance <= 32 (8 blocks?):
                        HelperEntity.this.distanceToSqr(HelperEntity.this.getTarget()) <= 32.0D;
            }

            @Override
            public boolean canContinueToUse() {
                if (HelperEntity.this.isJamming()) return false;
                return super.canContinueToUse() &&
                        HelperEntity.this.distanceToSqr(HelperEntity.this.getTarget()) <= 36.0D;
            }
        });

        // Priority 3: RANGED (Only triggers if distance > 32)
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0D, 40, 20.0F) {
            @Override
            public boolean canUse() {
                if (HelperEntity.this.isJamming()) return false;
                if (!super.canUse()) return false;
                return HelperEntity.this.getTarget() != null &&
                        HelperEntity.this.distanceToSqr(HelperEntity.this.getTarget()) > 32.0D;
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

        // Only pick a random skin if there isn't one saved in the NBT already
        if (tag == null || !tag.contains("SkinId")) {
            this.setSkinId(level.getRandom().nextInt(7));
        }

        return spawnData;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKIN_ID, 0);
        this.entityData.define(IS_GLITCHING, false);
        this.entityData.define(CARRIED_BLOCK, Optional.empty());
        this.entityData.define(IS_JAMMING, false);
    }

    public int getSkinId() { return this.entityData.get(DATA_SKIN_ID); }
    public void setSkinId(int id) { this.entityData.set(DATA_SKIN_ID, id); }

    public static boolean checkHelperSpawnRules(EntityType<HelperEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }
}