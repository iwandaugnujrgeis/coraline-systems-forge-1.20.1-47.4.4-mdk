package net.zharok01.coralinesystems.mixin;

import com.legacy.rediscovered.entity.pigman.PigmanEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;

@Mixin(Giant.class)
public abstract class GiantMixin extends Monster {

    protected GiantMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return CoralineSounds.GIANT_AMBIENT.get();
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return CoralineSounds.GIANT_HURT.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return CoralineSounds.GIANT_DEATH.get();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityType<? extends Giant> type, Level level, CallbackInfo ci) {

        Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED))
                .setBaseValue(0.25D);

        Objects.requireNonNull(this.getAttribute(Attributes.KNOCKBACK_RESISTANCE))
                .setBaseValue(1.0D);

        /*
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE))
                .setBaseValue(3.0D);

        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_KNOCKBACK))
                .addPermanentModifier(new AttributeModifier(
                        UUID.fromString("a3b4c5d6-e7f8-1234-abcd-000000000001"),
                        "Giant kick knockback",
                        4.0D,
                        AttributeModifier.Operation.ADDITION
                ));
        */

        //this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this)); // Ported from Rediscovered

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PigmanEntity.class, true));
    }

    // Overriding the method from `Mob` to add the randomized rotation that Rediscovered used to handle.
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);

        // Ported from Rediscovered: Randomize rotation upon spawn
        this.setYRot(Mth.wrapDegrees(level.getRandom().nextFloat() * 360.0F));
        this.yHeadRot = this.getYRot();
        this.yBodyRot = this.getYRot();

        return data;
    }
}