package net.zharok01.coralinesystems.entity;

import com.legacy.rediscovered.entity.pigman.PigmanEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.registries.ForgeRegistries;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineStats;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
import net.zharok01.coralinesystems.registry.CoralineUtils;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public class MonsterEntity extends Monster {

    private static final EntityDataAccessor<Boolean> IS_ANGRY     = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SPOTTED   = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_GLITCHING = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_FADING    = SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FADE_TICKS_REMAINING =
            SynchedEntityData.defineId(MonsterEntity.class, EntityDataSerializers.INT);

    public int glitchTimer     = 0;
    private int attackCooldown  = 0;
    private int fadeTicks       = 0;
    // Throttles getNearestPlayer — only re-scans once every PLAYER_SEARCH_INTERVAL ticks
    private int playerSearchTimer = 0;
    @Nullable private Player nearestPlayer = null;

    public static final int FADE_DURATION_TICKS      = 20;
    private static final int ATTACK_COOLDOWN_TICKS   = 40;
    private static final int PLAYER_SEARCH_INTERVAL  = 10;
    private static final float HIT_FLEE_CHANCE       = 0.9F;
    private static final float HURT_FLEE_CHANCE      = 0.9F;
    private static final float MAYBE_FLEE_CHANCE        = 0.15F;
    /** Squared distance at which plate armor scares an idle Monster (9 blocks). */
    /** Ticks the Monster has been watching a plate-armored player before fleeing. */
    private int plateSpookTimer = -1; // -1 = not active
    /** Delay in ticks before the Monster fades out after seeing plate armor (1 second). */
    private static final int PLATE_SPOOK_DELAY = 20;

    // -------------------------------------------------------------------------
    // Plate armor repellent
    //
    // Referenced by ResourceLocation via ForgeRegistries so we don't need a
    // hard compile-time dependency on Rediscovered's item classes. The Set uses
    // a switch expression so it is constructed only once at class-load time.
    // -------------------------------------------------------------------------

    /**
     * Any player wearing at least one of these items in any armor slot will
     * repel the Monster — causing it to scream and flee rather than trigger.
     *
     * Checked across all four equipment slots (helmet, chest, legs, feet)
     * so partial plate armor is still effective.
     */
    private static final Set<ResourceLocation> PLATE_ARMOR_IDS = Set.of(
            new ResourceLocation("rediscovered", "plate_helmet"),
            new ResourceLocation("rediscovered", "plate_chestplate"),
            new ResourceLocation("rediscovered", "plate_leggings"),
            new ResourceLocation("rediscovered", "plate_boots")
    );

    /**
     * Returns true if the player is wearing at least one piece of plate armor
     * in any equipment slot. Checks all four armor slots so partial sets work.
     *
     * Uses ForgeRegistries to get the ResourceLocation of each equipped item,
     * which is safe even if Rediscovered is absent — the item simply won't
     * match any entry in PLATE_ARMOR_IDS and the method returns false.
     */
    private static boolean isWearingPlateArmor(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            Item item = player.getItemBySlot(slot).getItem();
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
            if (key != null && PLATE_ARMOR_IDS.contains(key)) return true;
        }
        return false;
    }

    /**
     * The Monster's frightened response to plate armor.
     *
     * Plays the scream sound at the Monster's position, teleports 20-30 blocks
     * away in a random horizontal direction, then fully resets state so the
     * Monster returns to invisible/unspotted — as if it was never there.
     *
     * The flee distance uses a wider range (20-30 blocks XZ) than the standard
     * combat teleport (16 blocks) to ensure the Monster clears the player's
     * detection radius before resetting.
     */
    private void screamAndFlee() {
        if (this.level().isClientSide()) return;

        // Play the scream at the Monster's current position before teleporting
        this.playSound(CoralineSounds.MONSTER_SCREAM.get(), 1.5F,
                0.8F + this.random.nextFloat() * 0.4F);

        // Try up to 16 positions, each 20-30 blocks away in XZ
        for (int i = 0; i < 16; i++) {
            // Scale factor picks a distance between 20 and 30 blocks
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

        // Full state reset — Monster returns to invisible/unspotted idle
        this.setTarget(null); // cascades through setTarget override
    }

    // -------------------------------------------------------------------------

    public void setGlitching(boolean glitching) { this.entityData.set(IS_GLITCHING, glitching); }
    public boolean isGlitching() { return this.entityData.get(IS_GLITCHING); }
    public boolean isFading()    { return this.entityData.get(IS_FADING); }
    public int getFadeTicksRemaining() { return this.entityData.get(FADE_TICKS_REMAINING); }

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
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide) {

            // ── Fade countdown ────────────────────────────────────────────────
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

            // ── Attack cooldown tick-down ──────────────────────────────────────
            if (this.attackCooldown > 0) this.attackCooldown--;

            // ── Water escape ──────────────────────────────────────────────────
            if (this.isInWater()) {
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

            // ── Drop Creative / Spectator targets ─────────────────────────────
            LivingEntity currentTarget = this.getTarget();
            if (currentTarget instanceof Player targetPlayer &&
                    (targetPlayer.getAbilities().instabuild || targetPlayer.isSpectator())) {
                this.setTarget(null);
            }
            if (currentTarget != null && !currentTarget.isAlive()) {
                this.setTarget(null);
            }

            // Throttle getNearestPlayer — it's a spatial search, not free.
            // Re-scan every PLAYER_SEARCH_INTERVAL ticks; use cached result otherwise.
            if (playerSearchTimer <= 0) {
                nearestPlayer = this.level().getNearestPlayer(this, 64.0D);
                playerSearchTimer = PLAYER_SEARCH_INTERVAL;
            } else {
                playerSearchTimer--;
            }
            Player player = nearestPlayer;
            if (player != null && !player.getAbilities().instabuild && !player.isSpectator()) {

                // ── Plate armor repellent ─────────────────────────────────────
                // If already active (spotted/angry), flee immediately — no delay.
                // Otherwise: start a PLATE_SPOOK_DELAY-tick timer the moment the
                // Monster has line-of-sight to a plate-armored player. This gives
                // the player a brief window to see the Monster watching them before
                // it fades out with the full fade animation.
                // Timer resets if LOS breaks or armor is removed during the window.
                boolean wearingPlate = isWearingPlateArmor(player);
                if (wearingPlate && (this.isSpotted() || this.isAngry())) {
                    // Already engaged — flee without delay
                    screamAndFlee();
                    CoralineTriggers.SCARE_MONSTER.trigger((ServerPlayer) player);
                    return;
                } else if (wearingPlate && this.hasLineOfSight(player)) {
                    if (plateSpookTimer < 0) {
                        // LOS just established — start the countdown
                        plateSpookTimer = PLATE_SPOOK_DELAY;
                    } else if (plateSpookTimer == 0) {
                        // Timer expired — fade out permanently
                        plateSpookTimer = -1;
                        this.entityData.set(IS_FADING, true);
                        this.fadeTicks = FADE_DURATION_TICKS;
                        this.entityData.set(FADE_TICKS_REMAINING, this.fadeTicks);
                        this.getNavigation().stop();
                        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                        super.setTarget(null);
                        CoralineTriggers.SCARE_MONSTER.trigger((ServerPlayer) player);
                        return;
                    } else {
                        plateSpookTimer--;
                    }
                } else {
                    // LOS broken or armor removed — reset
                    plateSpookTimer = -1;
                }

                // Proximity trigger while still unspotted
                if (!this.isSpotted() && this.distanceToSqr(player) < 36.0D) {
                    if (this.random.nextFloat() < 0.10F) {
                        this.setSpotted(true);
                        this.setGlitching(true);
                        this.glitchTimer = 100;
                        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                CoralineSounds.MONSTER_STARE.get(), this.getSoundSource(), 1.0F, 1.0F);
                    } else {
                        double tx = this.getX() + (this.random.nextDouble() - 0.5D) * 32.0D;
                        double tz = this.getZ() + (this.random.nextDouble() - 0.5D) * 32.0D;
                        double ty = this.getY() + (double)(this.random.nextInt(16) - 8);
                        CoralineUtils.randomTeleportStatic(this, tx, ty, tz, true);
                    }
                }

                // Sighting trigger — hasLineOfSight is a raycast; only run when
                // not yet spotted since post-spotting the check is irrelevant.
                // Skip entirely when the player is wearing plate armor — the
                // plate scare fires instead, and stacking both sounds is jarring.
                if (!this.isSpotted() && !wearingPlate && this.isLookingAtMe(player)) {
                    this.setSpotted(true);
                    this.setGlitching(true);
                    this.glitchTimer = 100;
                    this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            CoralineSounds.MONSTER_STARE.get(), this.getSoundSource(), 1.0F, 1.0F);
                    player.awardStat(CoralineStats.MONSTER_SIGHTINGS.get());
                }

                if (this.isSpotted() && !this.isAngry() && currentTarget == null) {
                    this.setAngry(true);
                    this.setTarget(player);
                }
            }

            // ── Glitch event ──────────────────────────────────────────────────
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

        Vec3 vec3  = player.getViewVector(1.0F); // already normalized by vanilla
        Vec3 vec31 = new Vec3(
                this.getX() - player.getX(),
                this.getEyeY() - player.getEyeY(),
                this.getZ() - player.getZ()
        );
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0D - 0.025D / d0;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
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

            if (!this.isSpotted()) {
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
            this.entityData.set(IS_FADING, true);
            this.fadeTicks = FADE_DURATION_TICKS;
            this.entityData.set(FADE_TICKS_REMAINING, this.fadeTicks);
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            super.setTarget(null);
        }
    }

    public void setAngry(boolean angry)     { this.entityData.set(IS_ANGRY, angry); }
    public boolean isAngry()               { return this.entityData.get(IS_ANGRY); }
    public void setSpotted(boolean spotted) { this.entityData.set(IS_SPOTTED, spotted); }
    public boolean isSpotted()             { return this.entityData.get(IS_SPOTTED); }

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
        if (!this.level().isClientSide() && this.isAlive()) {
            Vec3 awayVec = new Vec3(
                    this.getX() - target.getX(),
                    this.getY(0.5D) - target.getEyeY(),
                    this.getZ() - target.getZ()
            ).normalize();

            for (int i = 0; i < 10; i++) {
                double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D - awayVec.x * 12.0D;
                double d1 = this.getY() + (this.random.nextInt(8) - 4) - awayVec.y * 12.0D;
                double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D - awayVec.z * 12.0D;
                if (CoralineUtils.randomTeleportStatic(this, d0, d1, d2, true)) {
                    playTeleportSound();
                    return true;
                }
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
    protected SoundEvent getHurtSound(DamageSource damageSource) { return CoralineSounds.MONSTER_HURT.get(); }
    @Override
    protected SoundEvent getDeathSound() { return CoralineSounds.MONSTER_DEATH.get(); }

    static class MonsterGlitchAttackGoal extends Goal {
        private final MonsterEntity monster;
        private int teleportTime = 0;
        private static final int RE_APPROACH_TICKS = 20;
        private static final double LOST_DISTANCE_SQ = 9216.0D; // 96 blocks squared

        public MonsterGlitchAttackGoal(MonsterEntity monster) {
            this.monster = monster;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = monster.getTarget();
            if (target == null || !monster.isAngry() || monster.isGlitching()) return false;
            if (target instanceof Player p && (p.getAbilities().instabuild || p.isSpectator())) return false;
            // Don't start chasing a plate-armored player — let aiStep handle fleeing
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
                // Target equipped plate armor mid-chase — aiStep will call screamAndFlee
                // on the next tick; we just stop the goal now so the chase ends cleanly.
                if (isWearingPlateArmor(p)) return false;
            }
            return true;
        }

        @Override
        public void start() {
            teleportTime = 0;
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
            } else {
                teleportTime = 0;
                this.monster.getNavigation().moveTo(target, 1.5D);

                if (dist <= 4.0D && monster.attackCooldown <= 0) {
                    this.monster.doHurtTarget(target);
                }
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

    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ANGRY,             false);
        this.entityData.define(IS_SPOTTED,           false);
        this.entityData.define(IS_GLITCHING,         false);
        this.entityData.define(IS_FADING,            false);
        this.entityData.define(FADE_TICKS_REMAINING, 0);
    }


}