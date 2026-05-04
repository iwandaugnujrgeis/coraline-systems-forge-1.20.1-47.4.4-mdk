package net.zharok01.coralinesystems.content.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

public class BrumeEntity extends Zombie implements RangedAttackMob {

    public BrumeEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes() //
                .add(Attributes.MOVEMENT_SPEED, 0.2D) //[cite: 40]
                .add(Attributes.FOLLOW_RANGE, 35.0D) //[cite: 40]
                .add(Attributes.ATTACK_DAMAGE, 4.0D) //[cite: 40]
                .add(Attributes.ARMOR, 2.0D); //[cite: 40]
    }

    @Override
    protected void addBehaviourGoals() {
        // Distance-based attack switching logic
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && BrumeEntity.this.getTarget() != null &&
                        BrumeEntity.this.distanceToSqr(BrumeEntity.this.getTarget()) <= 25.0D;
            }
        });

        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0D, 40, 20.0F) {
            @Override
            public boolean canUse() {
                return super.canUse() && BrumeEntity.this.getTarget() != null &&
                        BrumeEntity.this.distanceToSqr(BrumeEntity.this.getTarget()) > 25.0D;
            }
        });

        // Retaining standard Zombie roaming behavior[cite: 41]
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, this::canBreakDoors)); //[cite: 41]
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D)); //[cite: 41]

        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglin.class)); //[cite: 41]
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true)); //[cite: 41]
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false)); //[cite: 41]
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true)); //[cite: 41]
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        Snowball snowball = new Snowball(this.level(), this); //

        // Math derived natively from the Vanilla Snow Golem
        double d0 = target.getEyeY() - 1.1F; //[cite: 39]
        double d1 = target.getX() - this.getX(); //[cite: 39]
        double d2 = d0 - snowball.getY(); //[cite: 39]
        double d3 = target.getZ() - this.getZ(); //[cite: 39]
        double d4 = Math.sqrt(d1 * d1 + d3 * d3) * 0.2F; //[cite: 39]

        snowball.shoot(d1, d2 + d4, d3, 1.6F, 12.0F); //[cite: 39]
        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F)); //[cite: 39]
        this.level().addFreshEntity(snowball); //[cite: 39]
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean attackResult = super.doHurtTarget(target); //[cite: 41]

        // Native freezing logic application based on effective difficulty
        if (attackResult && this.getMainHandItem().isEmpty() && target instanceof LivingEntity livingTarget && livingTarget.canFreeze()) {
            float difficulty = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            livingTarget.setTicksFrozen(livingTarget.getTicksFrozen() + (int)(140 * difficulty)); //[cite: 40]
        }

        return attackResult;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty); //[cite: 41]
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SNOWBALL)); //[cite: 40]
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    public static boolean checkBrumeSpawnRules(EntityType<BrumeEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }
}