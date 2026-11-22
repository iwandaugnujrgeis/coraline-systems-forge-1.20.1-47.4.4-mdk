package net.zharok01.coralinesystems.entity;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.legacy.rediscovered.entity.pigman.PigmanEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.registries.ForgeRegistries;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineStats;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
import net.zharok01.coralinesystems.registry.CoralineUtils;
import net.zharok01.coralinesystems.util.helper.StealthHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public class MonsterEntity extends Monster {

    private static final EntityDataAccessor<Boolean> IS_ANGRY = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SPOTTED = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_GLITCHING = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_FADING = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FADE_TICKS_REMAINING = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TRANSITION_TICKS_REMAINING = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SKIN_ID = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.INT);

    public static final int SKIN_VARIANT_COUNT = 2;

    public int glitchTimer = 0;
    private int attackCooldown = 0;
    private int fadeTicks = 0;
    private int playerSearchTimer = 0;
    @Nullable private Player nearestPlayer = null;
    @Nullable private Player coraline$stalkPlayer = null;
    private boolean coraline$isTrackingPlayer = false;

    private int coraline$phaseTimer;

    private float coraline$lean = 0.0F;
    private float coraline$leanOld = 0.0F;
    private static final float LEAN_EASE_PER_TICK = 0.12F;
    private static final float LEAN_MOVE_SPEED_THRESHOLD = 0.02F;

    public static final int FADE_DURATION_TICKS = 20;
    private static final int ATTACK_COOLDOWN_TICKS = 40;
    private static final int PLAYER_SEARCH_INTERVAL = 10;
    private static final float HIT_FLEE_CHANCE = 0.9F;
    private static final float HURT_FLEE_CHANCE = 0.9F;
    private static final float MAYBE_FLEE_CHANCE = 0.15F;

    private static final int PHASE_STANDBY = 0;
    private static final int PHASE_MATERIALIZING = 1;
    private static final int PHASE_ROAMING = 2;
    private static final int PHASE_DEMATERIALIZING = 3;

    private static final int TRANSITION_DURATION_TICKS = 24;
    private static final int ROAM_DURATION_TICKS = 200;
    private static final int STANDBY_DURATION_TICKS = 400;
    private static final int PHASE_VARIANCE_TICKS = 30;
    private static final double ROAM_START_RANGE_SQ = 2304.0D;
    private static final double SPRINT_SPEED_FACTOR = 1.3D;
    private static final int ROAM_STALL_CHANCE_INVERSE = 40;
    private static final double ROAM_MIN_RADIUS = 8.0D;
    private static final double ROAM_RADIUS_VARIANCE = 10.0D;
    private static final int NEW_MOON_PHASE = 4;
    private static final double TELEPORT_BEHIND_MIN_DISTANCE = 6.0D;
    private static final double TELEPORT_BEHIND_DISTANCE_VARIANCE = 4.0D;
    private static final double TELEPORT_BEHIND_SIDE_SPREAD = 4.0D;
    private static final int TELEPORT_BEHIND_ATTEMPTS = 10;

    @SuppressWarnings("removal")
    private static final ResourceKey<Biome> OLD_WOODS_BIOME = ResourceKey.create(Registries.BIOME, new ResourceLocation("gamma", "old_woods"));

    @SuppressWarnings("removal")
    private static final ResourceKey<Biome> UNDERGROUND_BIOME = ResourceKey.create(Registries.BIOME, new ResourceLocation("gamma", "underground"));

    private int plateSpookTimer = -1;
    private static final int PLATE_SPOOK_DELAY = 20;

    @SuppressWarnings("removal")
    private static final Set<ResourceLocation> PLATE_ARMOR_IDS = Set.of(
            new ResourceLocation("rediscovered", "plate_helmet"),
            new ResourceLocation("rediscovered", "plate_chestplate"),
            new ResourceLocation("rediscovered", "plate_leggings"),
            new ResourceLocation("rediscovered", "plate_boots")
    );

    private static boolean isWearingPlateArmor(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            Item item = player.getItemBySlot(slot).getItem();
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
            if (key != null && PLATE_ARMOR_IDS.contains(key)) return true;
        }
        return false;
    }

    private void screamAndFlee() {
        if (this.level().isClientSide()) return;
        this.playSound(CoralineSounds.MONSTER_SCREAM.get(), 1.5F, 0.8F + this.random.nextFloat() * 0.4F);
        for (int i = 0; i < 16; i++) {
            double scale = 20.0D + this.random.nextDouble() * 10.0D;
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double tx = this.getX() + Math.cos(angle) * scale;
            double tz = this.getZ() + Math.sin(angle) * scale;
            double ty = this.getY() + (double)(this.random.nextInt(8) - 4);
            if (CoralineUtils.randomTeleportStatic(this, tx, ty, tz, true)) {
                playTeleportSound();
                break;
            }
        }
        this.setTarget(null);
    }

    public void setGlitching(boolean glitching) { this.entityData.set(IS_GLITCHING, glitching); }
    public boolean isGlitching() { return this.entityData.get(IS_GLITCHING); }
    public boolean isFading() { return this.entityData.get(IS_FADING); }
    public int getFadeTicksRemaining() { return this.entityData.get(FADE_TICKS_REMAINING); }

    public int coraline$getSkinId() { return this.entityData.get(DATA_SKIN_ID); }

    public boolean coraline$isRoaming() { return this.entityData.get(PHASE) == PHASE_ROAMING; }
    public boolean coraline$isMaterializing() { return this.entityData.get(PHASE) == PHASE_MATERIALIZING; }

    public boolean coraline$isTransitioning() {
        int phase = this.entityData.get(PHASE);
        return phase == PHASE_MATERIALIZING || phase == PHASE_DEMATERIALIZING;
    }

    public float coraline$getTransitionProgress(float partialTicks) {
        float remaining = Math.max(0.0F, this.entityData.get(TRANSITION_TICKS_REMAINING) - partialTicks);
        return Math.min(1.0F, 1.0F - remaining / TRANSITION_DURATION_TICKS);
    }

    public float coraline$getLean(float partialTicks) {
        return Mth.lerp(partialTicks, this.coraline$leanOld, this.coraline$lean);
    }

    private void coraline$tickLean() {
        this.coraline$leanOld = this.coraline$lean;
        float leanTarget = !this.isGlitching() && this.walkAnimation.speed() > LEAN_MOVE_SPEED_THRESHOLD ? 1.0F : 0.0F;
        this.coraline$lean += (leanTarget - this.coraline$lean) * LEAN_EASE_PER_TICK;
    }

    public boolean coraline$usesGhostRender() {
        return this.isFading() || this.coraline$isTransitioning() || (!this.isSpotted() && !this.coraline$isRoaming());
    }

    public MonsterEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.coraline$phaseTimer = this.coraline$randomPhaseDuration(STANDBY_DURATION_TICKS);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new MonsterGlitchAttackGoal(this));
        this.goalSelector.addGoal(1, new MonsterRoamGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 64.0F, 1.0F) {
            @Override
            public boolean canUse() {
                if (!super.canUse()) return false;
                assert this.lookAt != null;
                return !StealthHelper.coraline$isHidden(this.lookAt);
            }

            @Override
            public boolean canContinueToUse() {
                if (!super.canContinueToUse()) return false;
                assert this.lookAt != null;
                return !StealthHelper.coraline$isHidden(this.lookAt);
            }

            @Override
            public void start() {
                super.start();
                MonsterEntity.this.coraline$isTrackingPlayer = true;
            }

            @Override
            public void stop() {
                super.stop();
                MonsterEntity.this.coraline$isTrackingPlayer = false;
            }
        });
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PigmanEntity.class, 10, true, false,
                entity -> !this.isSpotted() && !this.isAngry() && !StealthHelper.coraline$isHidden(entity)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinId", this.coraline$getSkinId());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinId", 99)) {
            this.entityData.set(DATA_SKIN_ID, tag.getInt("SkinId"));
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);

        if (tag == null || !tag.contains("SkinId")) {
            this.entityData.set(DATA_SKIN_ID, level.getRandom().nextInt(SKIN_VARIANT_COUNT));
        }

        return spawnData;
    }

    @Override
    public boolean checkSpawnRules(@NotNull LevelAccessor level, @NotNull MobSpawnType spawnType) {
        if (spawnType == MobSpawnType.NATURAL) {
            Holder<Biome> biome = level.getBiome(this.blockPosition());
            boolean inUnderground = biome.is(UNDERGROUND_BIOME);
            boolean inOldWoodsOnNewMoon = biome.is(OLD_WOODS_BIOME) && level.getMoonPhase() == NEW_MOON_PHASE;
            if (!inUnderground && !inOldWoodsOnNewMoon) return false;
        }
        return super.checkSpawnRules(level, spawnType);
    }

    private int coraline$randomPhaseDuration(int baseTicks) {
        return baseTicks + this.random.nextInt(PHASE_VARIANCE_TICKS * 2 + 1) - PHASE_VARIANCE_TICKS;
    }

    private void coraline$beginPhase(int phase, int durationTicks) {
        this.entityData.set(PHASE, phase);
        this.coraline$phaseTimer = durationTicks;
        this.entityData.set(TRANSITION_TICKS_REMAINING, this.coraline$isTransitioning() ? durationTicks : 0);
    }

    private void coraline$spawnStaticBurst(int count) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(AMParticleRegistry.STATIC_SPARK.get(),
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    count, 0.35D, 0.9D, 0.35D, 0.05D);
        }
    }

    private void coraline$beginFade() {
        this.entityData.set(IS_FADING, true);
        this.fadeTicks = FADE_DURATION_TICKS;
        this.entityData.set(FADE_TICKS_REMAINING, this.fadeTicks);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        super.setTarget(null);
    }

    private void coraline$tickPhaseCycle() {
        int phase = this.entityData.get(PHASE);

        if (this.isSpotted() || this.isAngry()) {
            if (phase != PHASE_STANDBY) {
                this.coraline$beginPhase(PHASE_STANDBY, 0);
            }
            this.coraline$phaseTimer = this.coraline$randomPhaseDuration(STANDBY_DURATION_TICKS);
            return;
        }

        if (this.isFading()) return;

        if (this.coraline$phaseTimer > 0) {
            this.coraline$phaseTimer--;
            if (phase == PHASE_MATERIALIZING || phase == PHASE_DEMATERIALIZING) {
                this.entityData.set(TRANSITION_TICKS_REMAINING, this.coraline$phaseTimer);
                this.coraline$spawnStaticBurst(3);
            }
            return;
        }

        switch (phase) {
            case PHASE_STANDBY -> {
                Player target = this.coraline$stalkPlayer;
                if (target != null && !target.getAbilities().instabuild && this.distanceToSqr(target) < ROAM_START_RANGE_SQ && !this.coraline$isTrackingPlayer) {
                    this.playSound(CoralineSounds.MONSTER_MATERIALIZE.get(), 1.5F, 0.9F + this.random.nextFloat() * 0.2F);
                    this.coraline$spawnStaticBurst(24);
                    this.coraline$beginPhase(PHASE_MATERIALIZING, TRANSITION_DURATION_TICKS);
                }
            }
            case PHASE_MATERIALIZING -> {
                this.coraline$spawnStaticBurst(12);
                this.coraline$beginPhase(PHASE_ROAMING, this.coraline$randomPhaseDuration(ROAM_DURATION_TICKS));
            }
            case PHASE_ROAMING -> {
                this.playSound(CoralineSounds.MONSTER_DEMATERIALIZE.get(), 1.5F, 0.9F + this.random.nextFloat() * 0.2F);
                this.coraline$spawnStaticBurst(24);
                this.coraline$beginPhase(PHASE_DEMATERIALIZING, TRANSITION_DURATION_TICKS);
            }
            default -> {
                this.coraline$spawnStaticBurst(16);
                this.coraline$beginPhase(PHASE_STANDBY, this.coraline$randomPhaseDuration(STANDBY_DURATION_TICKS));
            }
        }
    }

    @Override
    public void aiStep() {
        this.coraline$tickLean();

        if (!this.level().isClientSide) {
            if (this.isFading()) {
                if (this.fadeTicks > 0) {
                    if (this.fadeTicks == 10) {
                        this.playSound(CoralineSounds.MONSTER_VANISH.get(), 1.3F, 1.2F);
                    }
                    this.fadeTicks--;
                    this.entityData.set(FADE_TICKS_REMAINING, this.fadeTicks);
                    this.getNavigation().stop();
                    this.setDeltaMovement(Vec3.ZERO);
                } else {
                    this.discard();
                    return;
                }
            }

            if (this.attackCooldown > 0) this.attackCooldown--;

            this.coraline$tickPhaseCycle();

            if (this.isInWater() || this.isInLava()) {
                for (int i = 0; i < 64; i++) {
                    double tx = this.getX() + (this.random.nextDouble() - 0.5D) * 16.0D;
                    double ty = this.getY() + (this.random.nextInt(8) + 1);
                    double tz = this.getZ() + (this.random.nextDouble() - 0.5D) * 16.0D;
                    if (CoralineUtils.randomTeleportStatic(this, tx, ty, tz, true)) {
                        playTeleportSound();
                        break;
                    }
                }
            }

            LivingEntity currentTarget = this.getTarget();
            if (currentTarget instanceof Player targetPlayer && (targetPlayer.getAbilities().instabuild || targetPlayer.isSpectator())) {
                this.setTarget(null);
            }
            if (currentTarget != null && (!currentTarget.isAlive() || StealthHelper.coraline$isHidden(currentTarget))) {
                this.setTarget(null);
            }

            if (currentTarget != null && (currentTarget.isInWater() || currentTarget.isInLava()) && !this.isFading()) {
                this.coraline$beginFade();
                return;
            }

            if (playerSearchTimer <= 0) {
                nearestPlayer = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 64.0D, EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(entity -> !StealthHelper.coraline$isHidden(entity)));
                coraline$stalkPlayer = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 64.0D, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
                playerSearchTimer = PLAYER_SEARCH_INTERVAL;
            } else {
                playerSearchTimer--;
            }
            Player player = nearestPlayer;
            if (player != null && !StealthHelper.coraline$isHidden(player) && !player.getAbilities().instabuild && !player.isSpectator()) {
                boolean wearingPlate = isWearingPlateArmor(player);
                if (wearingPlate && (this.isSpotted() || this.isAngry())) {
                    screamAndFlee();
                    CoralineTriggers.SCARE_MONSTER.trigger((ServerPlayer) player);
                    return;
                } else if (wearingPlate && this.hasLineOfSight(player)) {
                    if (plateSpookTimer < 0) {
                        plateSpookTimer = PLATE_SPOOK_DELAY;
                    } else if (plateSpookTimer == 0) {
                        plateSpookTimer = -1;
                        this.coraline$beginFade();
                        CoralineTriggers.SCARE_MONSTER.trigger((ServerPlayer) player);
                        return;
                    } else {
                        plateSpookTimer--;
                    }
                } else {
                    plateSpookTimer = -1;
                }

                if (!this.isSpotted() && this.distanceToSqr(player) < 36.0D) {
                    if (this.random.nextFloat() < 0.10F) {
                        this.setSpotted(true);
                        this.setGlitching(true);
                        this.glitchTimer = 100;
                        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                CoralineSounds.MONSTER_STARE.get(), this.getSoundSource(), 1.0F, 1.0F);
                    } else if (!this.coraline$isRoaming()) {
                        double tx = this.getX() + (this.random.nextDouble() - 0.5D) * 32.0D;
                        double tz = this.getZ() + (this.random.nextDouble() - 0.5D) * 32.0D;
                        double ty = this.getY() + (double)(this.random.nextInt(16) - 8);
                        CoralineUtils.randomTeleportStatic(this, tx, ty, tz, true);
                    }
                }

                if (!this.isSpotted() && !wearingPlate && this.isLookingAtMe(player)) {
                    this.setSpotted(true);
                    this.setGlitching(true);
                    this.glitchTimer = 100;
                    this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            CoralineSounds.MONSTER_STARE.get(), this.getSoundSource(), 1.0F, 1.0F);
                    player.awardStat(CoralineStats.MONSTER_SIGHTINGS.get());
                }

                if (this.isSpotted() && !this.isAngry() && currentTarget != player) {
                    this.setAngry(true);
                    this.setTarget(player);
                }
            }

            if (this.isGlitching()) {
                if (this.glitchTimer > 0) {
                    this.glitchTimer--;
                    this.getNavigation().stop();
                    this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                    if (this.glitchTimer % 10 == 0) {
                        double tx = this.getX() + (this.random.nextDouble() - 0.5D) * 6.0D;
                        double tz = this.getZ() + (this.random.nextDouble() - 0.5D) * 6.0D;
                        CoralineUtils.randomTeleportStatic(this, tx, this.getY(), tz, true);
                    }
                } else {
                    this.setGlitching(false);
                }
            }
        }
        super.aiStep();
    }

    private boolean isLookingAtMe(Player player) {
        if (player.isSpectator() || player.getAbilities().instabuild) return false;
        if (!player.hasLineOfSight(this)) return false;
        Vec3 vec3 = player.getViewVector(1.0F);
        Vec3 vec31 = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0D - 0.025D / d0;
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (!hurt) return false;
        if (target instanceof LivingEntity living && !living.isAlive()) {
            teleportShort();
            maybeFlee();
            this.setTarget(null);
        } else {
            if (this.random.nextFloat() < HIT_FLEE_CHANCE) {
                for (int i = 0; i < 8; i++) {
                    if (this.teleport()) break;
                }
                maybeFlee();
            }
            this.attackCooldown = ATTACK_COOLDOWN_TICKS;
        }
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL)) {
            return super.hurt(source, amount);
        }

        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            for (int i = 0; i < 64; ++i) {
                if (this.teleport()) {
                    maybeFlee();
                    return false;
                }
            }
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof Player player) {
            if (player.getAbilities().instabuild || player.isSpectator()) {
                return super.hurt(source, amount);
            }
        }

        if (!this.level().isClientSide) {
            if (attacker != null && this.random.nextFloat() < HURT_FLEE_CHANCE) {
                boolean teleported = false;
                for (int i = 0; i < 16; ++i) {
                    if (this.teleport()) { teleported = true; break; }
                }
                if (teleported) {
                    this.attackCooldown = ATTACK_COOLDOWN_TICKS;
                    maybeFlee();
                }
            }

            boolean coraline$attackerHidden = attacker instanceof LivingEntity hiddenAttacker && StealthHelper.coraline$isHidden(hiddenAttacker);
            if (!this.isSpotted() && !coraline$attackerHidden) {
                this.setSpotted(true);
                this.setGlitching(true);
                this.glitchTimer = 100;
                this.playSound(CoralineSounds.MONSTER_STARE.get(), 1.0F, 1.5F);
                if (attacker instanceof LivingEntity living) {
                    this.setTarget(living);
                    this.setAngry(true);
                }
            }
        }
        return super.hurt(source, amount);
    }

    private void maybeFlee() {
        if (!this.level().isClientSide && this.random.nextFloat() < MAYBE_FLEE_CHANCE) {
            this.coraline$beginFade();
        }
    }

    public void setAngry(boolean angry) { this.entityData.set(IS_ANGRY, angry); }
    public boolean isAngry() { return this.entityData.get(IS_ANGRY); }
    public void setSpotted(boolean spotted) { this.entityData.set(IS_SPOTTED, spotted); }
    public boolean isSpotted() { return this.entityData.get(IS_SPOTTED); }

    protected boolean teleport() {
        if (!this.level().isClientSide() && this.isAlive()) {
            double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * 16.0D;
            double d1 = this.getY() + (double)(this.random.nextInt(16) - 8);
            double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * 16.0D;
            boolean success = CoralineUtils.randomTeleportStatic(this, d0, d1, d2, true);
            if (success) playTeleportSound();
            return success;
        }
        return false;
    }

    private void teleportShort() {
        if (!this.level().isClientSide() && this.isAlive()) {
            double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D;
            double d1 = this.getY();
            double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D;
            boolean success = CoralineUtils.randomTeleportStatic(this, d0, d1, d2, true);
            if (success) playTeleportSound();
        }
    }

    protected boolean teleportTowards(LivingEntity target) {
        if (this.level().isClientSide() || !this.isAlive()) return false;

        Vec3 behind = Vec3.directionFromRotation(0.0F, target.getYRot()).scale(-1.0D);
        for (int i = 0; i < TELEPORT_BEHIND_ATTEMPTS; i++) {
            double distance = TELEPORT_BEHIND_MIN_DISTANCE + this.random.nextDouble() * TELEPORT_BEHIND_DISTANCE_VARIANCE;
            double sideways = (this.random.nextDouble() - 0.5D) * TELEPORT_BEHIND_SIDE_SPREAD;
            double x = target.getX() + behind.x * distance - behind.z * sideways;
            double z = target.getZ() + behind.z * distance + behind.x * sideways;
            double y = target.getY() + this.random.nextInt(3) - 1;
            if (CoralineUtils.randomTeleportStatic(this, x, y, z, true)) {
                playTeleportSound();
                return true;
            }
        }
        return false;
    }

    private void playTeleportSound() {
        float pitch = 0.7F + this.random.nextFloat() * 0.6F;
        this.playSound(CoralineSounds.MONSTER_TELEPORT.get(), 1.3F, pitch);
    }

    @Override
    protected SoundEvent getAmbientSound() { return CoralineSounds.MONSTER_IDLE.get(); }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) { return CoralineSounds.MONSTER_HURT.get(); }

    @Override
    protected @NotNull SoundEvent getDeathSound() { return CoralineSounds.MONSTER_DEATH.get(); }

    static class MonsterGlitchAttackGoal extends Goal {
        private final MonsterEntity monster;
        private int teleportTime = 0;
        private int repathTimer = 0;
        private int stuckTicks = 0;
        private static final int RE_APPROACH_TICKS = 20;
        private static final double LOST_DISTANCE_SQ = 1600.0D;
        private static final int REPATH_INTERVAL_TICKS = 10;
        private static final int STUCK_TELEPORT_TICKS = 40;

        public MonsterGlitchAttackGoal(MonsterEntity monster) {
            this.monster = monster;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = monster.getTarget();
            if (target == null || !monster.isAngry() || monster.isGlitching()) return false;
            if (target instanceof Player p && (p.getAbilities().instabuild || p.isSpectator())) return false;
            if (target instanceof Player p && isWearingPlateArmor(p)) return false;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = monster.getTarget();
            if (target == null || !monster.isAngry() || monster.isGlitching()) return false;
            if (target instanceof Player p) {
                if (p.getAbilities().instabuild || p.isSpectator()) {
                    monster.setTarget(null);
                    return false;
                }
                if (isWearingPlateArmor(p)) return false;
            }
            return true;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            teleportTime = 0;
            repathTimer = 0;
            stuckTicks = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.monster.getTarget();
            if (target == null) return;
            this.monster.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double dist = this.monster.distanceToSqr(target);

            if (dist > LOST_DISTANCE_SQ) {
                teleportTime++;
                if (teleportTime >= RE_APPROACH_TICKS) {
                    monster.teleportTowards(target);
                    teleportTime = 0;
                }
                this.monster.getNavigation().stop();
                return;
            }

            teleportTime = 0;
            if (--repathTimer <= 0) {
                repathTimer = REPATH_INTERVAL_TICKS;
                boolean pathFound = this.monster.getNavigation().moveTo(target, 1.5D);
                stuckTicks = pathFound ? 0 : stuckTicks + REPATH_INTERVAL_TICKS;
                if (stuckTicks >= STUCK_TELEPORT_TICKS) {
                    monster.teleportTowards(target);
                    stuckTicks = 0;
                }
            }

            if (dist <= 4.0D && monster.attackCooldown <= 0) {
                this.monster.doHurtTarget(target);
            }
        }
    }

    static class MonsterRoamGoal extends Goal {
        private final MonsterEntity monster;
        private int repathTimer = 0;
        private int pauseTimer = 0;

        public MonsterRoamGoal(MonsterEntity monster) {
            this.monster = monster;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return monster.coraline$isRoaming() && !monster.isSpotted() && !monster.isAngry();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            repathTimer = 0;
            pauseTimer = 0;
        }

        @Override
        public void stop() {
            monster.getNavigation().stop();
        }

        @Override
        public void tick() {
            Player focus = monster.coraline$stalkPlayer;
            if (focus == null) return;

            monster.getLookControl().setLookAt(focus, 30.0F, 30.0F);

            if (pauseTimer > 0) {
                pauseTimer--;
                monster.getNavigation().stop();
                return;
            }
            if (monster.random.nextInt(ROAM_STALL_CHANCE_INVERSE) == 0) {
                pauseTimer = 5 + monster.random.nextInt(8);
                return;
            }

            double playerAttribute = focus.getAttributeValue(Attributes.MOVEMENT_SPEED);
            double playerWalkSpeed = focus.isSprinting() ? playerAttribute / SPRINT_SPEED_FACTOR : playerAttribute;
            double speedModifier = Math.sqrt(playerWalkSpeed) / monster.getAttributeValue(Attributes.MOVEMENT_SPEED);

            if (--repathTimer <= 0 || monster.getNavigation().isDone()) {
                repathTimer = 30 + monster.random.nextInt(20);
                double orbitSide = monster.random.nextBoolean() ? 1.0D : -1.0D;
                double angle = Math.atan2(monster.getZ() - focus.getZ(), monster.getX() - focus.getX())
                        + orbitSide * (0.4D + monster.random.nextDouble() * 0.5D);
                double radius = ROAM_MIN_RADIUS + monster.random.nextDouble() * ROAM_RADIUS_VARIANCE;
                monster.getNavigation().moveTo(
                        focus.getX() + Math.cos(angle) * radius,
                        monster.getY(),
                        focus.getZ() + Math.sin(angle) * radius,
                        speedModifier);
            } else {
                monster.getNavigation().setSpeedModifier(speedModifier);
            }
        }
    }

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

    public void makeStuckInBlock(BlockState state, @NotNull Vec3 motionMultiplier) {
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
        this.entityData.define(IS_FADING, false);
        this.entityData.define(FADE_TICKS_REMAINING, 0);
        this.entityData.define(PHASE, PHASE_STANDBY);
        this.entityData.define(TRANSITION_TICKS_REMAINING, 0);
        this.entityData.define(DATA_SKIN_ID, 0);
    }
}