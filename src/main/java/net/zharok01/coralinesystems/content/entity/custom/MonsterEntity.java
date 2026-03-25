package net.zharok01.coralinesystems.content.entity.custom;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.legacy.rediscovered.entity.pigman.PigmanEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.content.sound.CoralineSounds;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class MonsterEntity extends Monster {
    private static final EntityDataAccessor<Boolean> IS_ANGRY = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SPOTTED = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);

    public MonsterEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        // Priority 0: The "Glitch and Stare" phase
        this.goalSelector.addGoal(0, new MonsterGlitchAttackGoal(this));

        // Priority 1: Tracking from distance
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 64.0F, 1.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PigmanEntity.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D) // 20 hearts
                .add(Attributes.MOVEMENT_SPEED, 0.35D) // Slightly faster than a player
                .add(Attributes.ATTACK_DAMAGE, 7.0D) // 3.5 hearts per hit
                .add(Attributes.FOLLOW_RANGE, 64.0D); // Matches your "Herobrine" look distance
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ANGRY, false);
        this.entityData.define(IS_SPOTTED, false);
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide) {
            // Particle logic from your prompt
            if (this.random.nextInt(5) == 0) {
                this.level().addParticle(AMParticleRegistry.STATIC_SPARK.get(),
                        this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0, 0, 0);
            }
        } else {
            Player player = this.level().getNearestPlayer(this, 64.0D);
            if (player != null) {
                boolean looking = this.isLookingAtMe(player);

                // Update spotted status for the Renderer
                if (looking && !this.isSpotted()) {
                    this.setSpotted(true);
                    this.playSound(CoralineSounds.MONSTER_STARE.get(), 1.0F, 1.0F); // Ominous sound
                }

                if (looking && !this.isAngry()) {
                    this.setAngry(true);
                    this.setTarget(player);
                }
            }
        }
        super.aiStep();
    }

    private boolean isLookingAtMe(Player player) {
        // FIX: Ignore players who can't be targeted (Creative/Spectator)
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
            return this.randomTeleport(d0, d1, d2, true);
        }
        return false;
    }

    static class MonsterGlitchAttackGoal extends Goal {
        private final MonsterEntity monster;
        private int glitchTicks;

        public MonsterGlitchAttackGoal(MonsterEntity monster) {
            this.monster = monster;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return monster.getTarget() != null && monster.isAngry();
        }

        @Override
        public void start() {
            this.glitchTicks = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.monster.getTarget();
            if (target != null) {
                this.monster.getLookControl().setLookAt(target, 30.0F, 30.0F);
                double dist = this.monster.distanceToSqr(target);

                if (this.glitchTicks < 40) { // Glitch phase (2 seconds)
                    this.monster.setDeltaMovement(0, this.monster.getDeltaMovement().y, 0); // Walk in place
                    this.glitchTicks++;
                } else {
                    // Attack phase
                    if (dist > 4.0D) {
                        this.monster.getNavigation().moveTo(target, 1.5D);
                    } else {
                        this.monster.doHurtTarget(target);
                        this.glitchTicks = 0; // Reset to glitch/retreat again
                    }
                }
            }
        }
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