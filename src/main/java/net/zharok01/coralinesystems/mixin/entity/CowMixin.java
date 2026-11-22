package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.util.interfaces.CowEatAnimationDuck;
import net.zharok01.coralinesystems.util.interfaces.CowMilkDuck;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cow.class)
public abstract class CowMixin extends Animal implements CowMilkDuck, CowEatAnimationDuck {
    @Unique
    private static final EntityDataAccessor<Boolean> CORALINE$DATA_MILKED =

            SynchedEntityData.defineId(CowMixin.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Integer> coraline$DATA_SMALL_MILKINGS =

            SynchedEntityData.defineId(CowMixin.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Boolean> coraline$DATA_STEW_MILKED =

            SynchedEntityData.defineId(CowMixin.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final int coraline$MAX_SMALL_MILKINGS_PER_GRAZE = 3;

    protected CowMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CORALINE$DATA_MILKED, false);
        this.entityData.define(coraline$DATA_SMALL_MILKINGS, 0);
        this.entityData.define(coraline$DATA_STEW_MILKED, false);
    }

    @Override
    public boolean coraline$isMilked() { return this.entityData.get(CORALINE$DATA_MILKED); }

    @Override
    public void coraline$setMilked(boolean milked) { this.entityData.set(CORALINE$DATA_MILKED, milked); }

    @Override
    public boolean coraline$canReceiveSmallServing() {
        return !this.coraline$isMilked()
                && this.entityData.get(coraline$DATA_SMALL_MILKINGS) < coraline$MAX_SMALL_MILKINGS_PER_GRAZE;
    }

    @Override
    public void coraline$recordSmallServing() {
        int milkings = this.entityData.get(coraline$DATA_SMALL_MILKINGS);
        this.entityData.set(coraline$DATA_SMALL_MILKINGS, Math.min(milkings + 1, coraline$MAX_SMALL_MILKINGS_PER_GRAZE));
    }

    @Override
    public boolean coraline$canReceiveStewServing() {
        return !this.entityData.get(coraline$DATA_STEW_MILKED) && this.coraline$canReceiveSmallServing();
    }

    @Override
    public void coraline$recordStewServing() {
        this.entityData.set(coraline$DATA_STEW_MILKED, true);
        this.coraline$recordSmallServing();
    }
    @Unique
    private int coraline$eatAnimationTick = 0;

    @Override
    public void coraline$startEatAnimation() {
        this.coraline$eatAnimationTick = 40;
    }

    @Override
    public void coraline$tickEatAnimation() {
        if (this.coraline$eatAnimationTick > 0) {
            this.coraline$eatAnimationTick--;
        }
    }

    @Override
    public float coraline$getHeadEatPositionScale(float partialTick) {
        if (this.coraline$eatAnimationTick <= 0) return 0.0F;
        if (this.coraline$eatAnimationTick >= 4 && this.coraline$eatAnimationTick <= 36) return 1.0F;
        return this.coraline$eatAnimationTick < 4
                ? ((float) this.coraline$eatAnimationTick - partialTick) / 4.0F
                : -((float) (this.coraline$eatAnimationTick - 40) - partialTick) / 4.0F;
    }

    @Override
    public float coraline$getHeadEatAngleScale(float partialTick) {
        if (this.coraline$eatAnimationTick > 4 && this.coraline$eatAnimationTick <= 36) {
            float f = ((float) (this.coraline$eatAnimationTick - 4) - partialTick) / 32.0F;
            return ((float) Math.PI / 5.0F) + 0.21991149F * Mth.sin(f * 28.7F) * 0.2F;
        }
        return this.coraline$eatAnimationTick > 0 ? ((float) Math.PI / 5.0F) : 0.0F;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsMilked", this.coraline$isMilked());
        compound.putInt("SmallMilkingsSinceEating", this.entityData.get(coraline$DATA_SMALL_MILKINGS));
        compound.putBoolean("StewMilkedSinceEating", this.entityData.get(coraline$DATA_STEW_MILKED));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.coraline$setMilked(compound.getBoolean("IsMilked"));
        this.entityData.set(coraline$DATA_STEW_MILKED, compound.getBoolean("StewMilkedSinceEating"));
        int milkings = compound.contains("SmallMilkingsSinceEating")
                ? compound.getInt("SmallMilkingsSinceEating")
                : compound.getInt("BottleMilkingsSinceEating");
        this.entityData.set(coraline$DATA_SMALL_MILKINGS, Mth.clamp(milkings, 0, coraline$MAX_SMALL_MILKINGS_PER_GRAZE));
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void coraline$addEatGrassGoal(CallbackInfo ci) {
        this.goalSelector.addGoal(5, new EatBlockGoal(this));
    }

    @Override
    public void ate() {
        super.ate();
        this.coraline$setMilked(false);
        this.entityData.set(coraline$DATA_SMALL_MILKINGS, 0);
        this.entityData.set(coraline$DATA_STEW_MILKED, false);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void coraline$handleMilking(Player player, InteractionHand hand,
                                        CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(Items.BUCKET) && !this.isBaby()) {
            if (this.coraline$isMilked() || this.entityData.get(coraline$DATA_SMALL_MILKINGS) > 0) {
                cir.setReturnValue(InteractionResult.FAIL);
            } else {
                this.coraline$setMilked(true);
            }
        } else if (itemStack.is(Items.GLASS_BOTTLE) && !this.isBaby()) {
            if (!this.coraline$canReceiveSmallServing()) {
                cir.setReturnValue(InteractionResult.FAIL);
            } else {
                this.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
                if (!this.level().isClientSide) {
                    ItemStack filledStack = ItemUtils.createFilledResult(itemStack, player, CoralineItems.MILK_BOTTLE.get().getDefaultInstance());
                    player.setItemInHand(hand, filledStack);
                    this.coraline$recordSmallServing();
                }
                cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
            }
        }
    }
}
