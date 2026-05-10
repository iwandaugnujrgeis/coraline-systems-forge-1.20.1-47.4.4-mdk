package net.zharok01.coralinesystems.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.util.CowMilkDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cow.class)
public abstract class CowMixin extends Animal implements CowMilkDuck {

    @Unique
    private static final EntityDataAccessor<Boolean> CORALINE$DATA_MILKED =
            SynchedEntityData.defineId(CowMixin.class, EntityDataSerializers.BOOLEAN);

    protected CowMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    // ── Synced data ───────────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CORALINE$DATA_MILKED, false);
    }

    @Override
    public boolean coraline$isMilked() {
        return this.entityData.get(CORALINE$DATA_MILKED);
    }

    @Override
    public void coraline$setMilked(boolean milked) {
        this.entityData.set(CORALINE$DATA_MILKED, milked);
    }

    // ── Save / Load ───────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsMilked", this.coraline$isMilked());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.coraline$setMilked(compound.getBoolean("IsMilked"));
    }

    // ── Goal registration ─────────────────────────────────────────────────────

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void coraline$addEatGrassGoal(CallbackInfo ci) {
        this.goalSelector.addGoal(5, new EatBlockGoal(this));
    }

    // ── Grass-eating callback (mirrors Sheep.ate() restoring wool) ────────────

    @Override
    public void ate() {
        super.ate();
        this.coraline$setMilked(false);
    }

    // ── Milking interaction ───────────────────────────────────────────────────

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void coraline$handleMilking(Player player, InteractionHand hand,
                                        CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(Items.BUCKET) && !this.isBaby()) {
            if (this.coraline$isMilked()) {
                // FAIL = "handled and denied" — stops the interaction chain here.
                // PASS would say "not handled, try next" which falls through to
                // item use, causing the player to drink their milk bucket.
                cir.setReturnValue(InteractionResult.FAIL);
            } else {
                // Mark as milked; don't cancel — vanilla gives the milk bucket.
                this.coraline$setMilked(true);
            }
        }
    }
}