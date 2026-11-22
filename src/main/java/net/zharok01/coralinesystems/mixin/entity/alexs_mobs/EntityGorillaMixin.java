package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntityGorilla;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIRideParent;
import com.github.alexthe666.alexsmobs.entity.ai.GorillaAIFollowCaravan;
import com.github.alexthe666.alexsmobs.entity.ai.GorillaAIForageLeaves;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.util.access.GorillaPickupAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = EntityGorilla.class, remap = false)
public abstract class EntityGorillaMixin extends TamableAnimal implements GorillaPickupAccess {

    @Shadow
    private boolean hasSilverbackAttributes;

    @Unique private float coraline$pickupProgress;
    @Unique private float coraline$prevPickupProgress;

    protected EntityGorillaMixin(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "m_7380_(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void coraline$saveSilverbackAttributes(CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean("CoralineSystems_HasSilverbackAttributes", this.hasSilverbackAttributes);
    }

    @Inject(method = "m_7378_(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void coraline$loadSilverbackAttributes(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("CoralineSystems_HasSilverbackAttributes")) {
            this.hasSilverbackAttributes = compound.getBoolean("CoralineSystems_HasSilverbackAttributes");
        }
    }

    @Inject(method = "m_8097_()V", at = @At("TAIL"))
    private void coraline$definePickupData(CallbackInfo ci) {
        this.entityData.define(GorillaPickupAccess.PICKING_UP, false);
    }

    @Inject(method = "m_8119_()V", at = @At("TAIL"))
    private void coraline$tickPickupPose(CallbackInfo ci) {
        this.coraline$prevPickupProgress = this.coraline$pickupProgress;
        if (this.coraline$isPickingUp()) {
            if (this.coraline$pickupProgress < GorillaPickupAccess.POSE_TICKS) {
                this.coraline$pickupProgress++;
            }
        } else if (this.coraline$pickupProgress > 0.0F) {
            this.coraline$pickupProgress--;
        }
    }

    @Inject(method = "m_21839_(Z)V", at = @At("HEAD"), cancellable = true)
    private void coraline$disableSitting(boolean sit, CallbackInfo ci) {
        if (sit) {
            ci.cancel();
        }
    }

    @Inject(method = "canTargetItem", at = @At("HEAD"), cancellable = true)
    private void coraline$disableVanillaEating(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Override
    public boolean coraline$isPickingUp() {
        return this.entityData.get(GorillaPickupAccess.PICKING_UP);
    }

    @Override
    public void coraline$setPickingUp(boolean pickingUp) {
        this.entityData.set(GorillaPickupAccess.PICKING_UP, pickingUp);
    }

    @Override
    public float coraline$getPickupProgress(float partialTick) {
        return Mth.lerp(partialTick, this.coraline$prevPickupProgress, this.coraline$pickupProgress);
    }

    @Inject(method = "m_8099_()V", at = @At("TAIL"))
    private void coraline$reorderAndCleanGoals(CallbackInfo ci) {
        Set<WrappedGoal> availableGoals = new HashSet<>(this.goalSelector.getAvailableGoals());
        for (WrappedGoal wrapped : availableGoals) {
            Goal rawGoal = wrapped.getGoal();

            if (rawGoal instanceof GorillaAIFollowCaravan) {
                this.goalSelector.removeGoal(rawGoal);
                continue;
            }

            if (rawGoal instanceof AnimalAIRideParent) {
                this.goalSelector.removeGoal(rawGoal);
                this.goalSelector.addGoal(5, rawGoal);
            } else if (rawGoal instanceof GorillaAIForageLeaves) {
                this.goalSelector.removeGoal(rawGoal);
                this.goalSelector.addGoal(6, rawGoal);
            } else if (rawGoal instanceof BreedGoal) {
                this.goalSelector.removeGoal(rawGoal);
                this.goalSelector.addGoal(7, rawGoal);
            } else if (rawGoal.getClass().getName().contains("AIWalkIdle")) {
                this.goalSelector.removeGoal(rawGoal);
                this.goalSelector.addGoal(8, rawGoal);
            } else if (rawGoal instanceof LookAtPlayerGoal) {
                this.goalSelector.removeGoal(rawGoal);
                this.goalSelector.addGoal(9, rawGoal);
            } else if (rawGoal instanceof RandomLookAroundGoal) {
                this.goalSelector.removeGoal(rawGoal);
                this.goalSelector.addGoal(10, rawGoal);
            }
        }

        Set<WrappedGoal> availableTargets = new HashSet<>(this.targetSelector.getAvailableGoals());
        for (WrappedGoal wrapped : availableTargets) {
            Goal rawGoal = wrapped.getGoal();

            if (rawGoal instanceof HurtByTargetGoal) {
                this.targetSelector.removeGoal(rawGoal);
                this.targetSelector.addGoal(2, rawGoal);
            } else if (rawGoal instanceof OwnerHurtByTargetGoal) {
                this.targetSelector.removeGoal(rawGoal);
                this.targetSelector.addGoal(3, rawGoal);
            } else if (rawGoal instanceof OwnerHurtTargetGoal) {
                this.targetSelector.removeGoal(rawGoal);
                this.targetSelector.addGoal(4, rawGoal);
            }
        }
    }
}