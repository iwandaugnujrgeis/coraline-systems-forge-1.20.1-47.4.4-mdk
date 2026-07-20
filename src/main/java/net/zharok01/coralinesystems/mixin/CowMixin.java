package net.zharok01.coralinesystems.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
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
import net.zharok01.coralinesystems.util.interfaces.CowEatAnimationDuck;
import net.zharok01.coralinesystems.util.interfaces.CowMilkDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cow.class)
public abstract class CowMixin extends Animal implements CowMilkDuck, CowEatAnimationDuck {

    // ── Synced milked flag (unchanged from your working version) ─────────────

    @Unique
    private static final EntityDataAccessor<Boolean> CORALINE$DATA_MILKED =
            SynchedEntityData.defineId(CowMixin.class, EntityDataSerializers.BOOLEAN);

    protected CowMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CORALINE$DATA_MILKED, false);
    }

    @Override
    public boolean coraline$isMilked() { return this.entityData.get(CORALINE$DATA_MILKED); }

    @Override
    public void coraline$setMilked(boolean milked) { this.entityData.set(CORALINE$DATA_MILKED, milked); }

    // ── Eat animation tick (local, not synced — model-side only needs it client-side) ──

    @Unique
    private int coraline$eatAnimationTick = 0;

    // ── Eat animation tick ────────────────────────────────────────────────────
    //
    // handleEntityEvent(10) and aiStep() are both overridden in Animal, not Cow,
    // so @Inject into them from CowMixin causes "cannot find target" at launch.
    // Instead, AnimalMixin calls these two interface methods via the duck check —
    // the same pattern used for ate() in MobMixin.

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

    /**
     * Returns 0→1 as the cow lowers its head, holds at 1, then returns to 0.
     * Mirrors Sheep.getHeadEatPositionScale() exactly.
     */
    @Override
    public float coraline$getHeadEatPositionScale(float partialTick) {
        if (this.coraline$eatAnimationTick <= 0) return 0.0F;
        if (this.coraline$eatAnimationTick >= 4 && this.coraline$eatAnimationTick <= 36) return 1.0F;
        return this.coraline$eatAnimationTick < 4
                ? ((float) this.coraline$eatAnimationTick - partialTick) / 4.0F
                : -((float) (this.coraline$eatAnimationTick - 36) - partialTick) / 4.0F;
    }

    /**
     * Returns the head pitch angle during eating, with a gentle sine wobble
     * in the middle of the animation. Mirrors Sheep.getHeadEatAngleScale().
     */
    @Override
    public float coraline$getHeadEatAngleScale(float partialTick) {
        if (this.coraline$eatAnimationTick > 4 && this.coraline$eatAnimationTick <= 36) {
            float f = ((float) (this.coraline$eatAnimationTick - 4) - partialTick) / 32.0F;
            // ~36 degrees down + small sine wobble for natural feel
            return ((float) Math.PI / 5.0F) + 0.21991149F * Mth.sin(f * 28.7F) * 0.2F;
        }
        return this.coraline$eatAnimationTick > 0 ? ((float) Math.PI / 5.0F) : 0.0F;
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

    // ── Grass-eating callback ─────────────────────────────────────────────────

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
                cir.setReturnValue(InteractionResult.FAIL);
            } else {
                this.coraline$setMilked(true);
            }
        }
    }
}