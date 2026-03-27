package net.zharok01.coralinesystems.content.entity.custom;

import com.legacy.rediscovered.entity.pigman.PigmanEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.content.sound.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineUtils;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class MonsterEntity extends Monster {
    private static final EntityDataAccessor<Boolean> IS_ANGRY = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SPOTTED = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_GLITCHING = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);

    public int glitchTimer = 0;

    public void setGlitching(boolean glitching) { this.entityData.set(IS_GLITCHING, glitching); }
    public boolean isGlitching() { return this.entityData.get(IS_GLITCHING); }

    public MonsterEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new MonsterGlitchAttackGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 64.0F, 1.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PigmanEntity.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D) // 20 hearts
                .add(Attributes.MOVEMENT_SPEED, 0.35D) // Slightly faster than a player
                .add(Attributes.ATTACK_DAMAGE, 7.0D) // 3.5 hearts per hit
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide) {

            if (this.getTarget() != null && !this.getTarget().isAlive()) {
                this.setTarget(null);
            }

            Player player = this.level().getNearestPlayer(this, 64.0D);
            if (player != null && !player.getAbilities().instabuild && !player.isSpectator()) {

                // Triggered if the player gets too close while the Monster is still invisible:
                if (!this.isSpotted() && this.distanceToSqr(player) < 12.25D) {
                    if (this.random.nextFloat() < 0.25F) { //25% chance to attack, instead of teleporting away!
                        this.setSpotted(true);
                        this.setGlitching(true);
                        this.glitchTimer = 100;
                        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                CoralineSounds.MONSTER_STARE.get(), this.getSoundSource(), 1.0F, 1.0F);
                    } else {
                        double tx = this.getX() + (this.random.nextDouble() - 0.5D) * 32.0D;
                        double tz = this.getZ() + (this.random.nextDouble() - 0.5D) * 32.0D;
                        double ty = this.getY() + (double) (this.random.nextInt(16) - 8);
                        CoralineUtils.randomTeleportStatic(this, tx, ty, tz, true);
                    }
                }
            }

            //The "sighting" trigger:
            if (player != null && !player.getAbilities().instabuild && !player.isSpectator()) {
                boolean looking = this.isLookingAtMe(player);

                if (looking && !this.isSpotted()) {
                    this.setSpotted(true); // Lock the trigger!
                    this.setGlitching(true);
                    this.glitchTimer = 100; // 5 seconds
                    this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            CoralineSounds.MONSTER_STARE.get(), this.getSoundSource(), 1.0F, 1.0F);
                }

                if (this.isSpotted() && !this.isAngry() && this.getTarget() == null) {
                    this.setAngry(true);
                    this.setTarget(player);
                }
            }

            //The "glitch" event:
            if (this.isGlitching()) {
                if (this.glitchTimer > 0) {
                    this.glitchTimer--;

                    // Force "walk in place" by killing momentum and navigation
                    this.getNavigation().stop();
                    this.setDeltaMovement(0, this.getDeltaMovement().y, 0);

                    // Teleport randomly within a 3-block radius every 10 ticks
                    if (this.glitchTimer % 10 == 0) {
                        double tx = this.getX() + (this.random.nextDouble() - 0.5D) * 6.0D;
                        double tz = this.getZ() + (this.random.nextDouble() - 0.5D) * 6.0D;
                        CoralineUtils.randomTeleportStatic(this, tx, this.getY(), tz, true);
                    }
                } else {
                    // Timer is up, let the chase begin!
                    this.setGlitching(false);
                }
            }
        }
        super.aiStep();
    }

    private boolean isLookingAtMe(Player player) {
        if (player.isSpectator() || player.getAbilities().instabuild) {
            return false;
        }

        Vec3 vec3 = player.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0D - 0.025D / d0;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            // Hit-and-run: Retreat (teleport) immediately after hitting
            this.teleport();
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            for(int i = 0; i < 64; ++i) {
                if (this.teleport()) return false;
            }
        }

        Entity attacker = source.getEntity();

        if (attacker instanceof Player player) {
            if (player.getAbilities().instabuild || player.isSpectator()) {
                return super.hurt(source, amount);
            }
        }

        // 3. The "hurt" trigger:
        if (!this.level().isClientSide && !this.isSpotted()) {
            this.setSpotted(true);     // Lock the trigger so it only happens once!
            this.setGlitching(true);
            this.glitchTimer = 100;    // 5 seconds
            this.playSound(CoralineSounds.MONSTER_STARE.get(), 1.0F, 1.5F); // Pitch 1.5 for pain

            // Make the attacker the target immediately:
            if (attacker instanceof LivingEntity living) {
                this.setTarget(living);
                this.setAngry(true);
            }
        }

        return super.hurt(source, amount);
    }

    public void setAngry(boolean angry) { this.entityData.set(IS_ANGRY, angry); }
    public boolean isAngry() { return this.entityData.get(IS_ANGRY); }
    public void setSpotted(boolean spotted) { this.entityData.set(IS_SPOTTED, spotted); }
    public boolean isSpotted() { return this.entityData.get(IS_SPOTTED); }

    protected boolean teleport() {
        if (!this.level().isClientSide() && this.isAlive()) {
            double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * 32.0D;
            double d1 = this.getY() + (double)(this.random.nextInt(16) - 8);
            double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * 32.0D;
            return CoralineUtils.randomTeleportStatic(this, d0, d1, d2, true);
        }
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return CoralineSounds.MONSTER_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return CoralineSounds.MONSTER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CoralineSounds.MONSTER_DEATH.get();
    }

    static class MonsterGlitchAttackGoal extends Goal {
        private final MonsterEntity monster;

        public MonsterGlitchAttackGoal(MonsterEntity monster) {
            this.monster = monster;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // Only chase if angry and the 5-second glitch phase is over:
            return monster.getTarget() != null && monster.isAngry() && !monster.isGlitching();
        }

        @Override
        public void tick() {
            LivingEntity target = this.monster.getTarget();
            if (target != null) {
                this.monster.getLookControl().setLookAt(target, 30.0F, 30.0F);
                double dist = this.monster.distanceToSqr(target);

                if (dist > 4.0D) {
                    this.monster.getNavigation().moveTo(target, 1.5D); // Fast chase speed
                } else {
                    this.monster.doHurtTarget(target); // Whack 'em and teleport away (via doHurtTarget)
                }
            }
        }
    }

    //Reset the target if the target is lost:
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);

        if (target == null) {
            this.setSpotted(false);
            this.setAngry(false);
            this.setGlitching(false);

            if (!this.level().isClientSide) {
                this.getNavigation().stop();
            }
        }
    }

    //Make Monsters pass through Cobwebs:
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ANGRY, false);
        this.entityData.define(IS_SPOTTED, false);
        this.entityData.define(IS_GLITCHING, false);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);

        return spawnData;
    }

    public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }
}