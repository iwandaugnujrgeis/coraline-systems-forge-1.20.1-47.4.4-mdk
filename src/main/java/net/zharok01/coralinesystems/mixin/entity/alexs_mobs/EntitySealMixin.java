package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntitySeal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.entity.ai.SealEatItemsGoal;
import net.zharok01.coralinesystems.util.access.SealPickupAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntitySeal.class, remap = false)
public class EntitySealMixin implements SealPickupAccess {

    private static final int FOOD_CHECK_INTERVAL_TICKS = 10;

    @Unique private static final EntityDataAccessor<Boolean> CORALINE$EATING =
            SynchedEntityData.defineId(EntitySeal.class, EntityDataSerializers.BOOLEAN);

    @Unique private float coraline$pickupProgress;
    @Unique private float coraline$prevPickupProgress;
    @Unique private boolean coraline$foodNearby;

    @Inject(method = "m_8097_", at = @At("TAIL"))
    private void coraline$defineEatingData(CallbackInfo ci) {
        ((EntitySeal) (Object) this).getEntityData().define(CORALINE$EATING, false);
    }

    @Inject(method = "m_8119_", at = @At("TAIL"))
    private void coraline$tickPickupPose(CallbackInfo ci) {
        EntitySeal seal = (EntitySeal) (Object) this;

        this.coraline$prevPickupProgress = this.coraline$pickupProgress;
        boolean eating = seal.getEntityData().get(CORALINE$EATING);

        if (eating) {
            if (this.coraline$pickupProgress < POSE_TICKS) this.coraline$pickupProgress++;
        } else if (this.coraline$pickupProgress > 0.0F) {
            this.coraline$pickupProgress--;
        }

        if (!seal.level().isClientSide() && seal.tickCount % FOOD_CHECK_INTERVAL_TICKS == 0) {
            this.coraline$foodNearby = !seal.isBaby()
                    && seal.getTarget() == null
                    && seal.revengeCooldown == 0
                    && !seal.level().getEntitiesOfClass(
                    ItemEntity.class,
                    seal.getBoundingBox().inflate(SealEatItemsGoal.SEARCH_RADIUS),
                    item -> SealEatItemsGoal.isSealFood(item.getItem())).isEmpty();

            if (this.coraline$foodNearby && seal.isBasking()) {
                seal.setBasking(false);
            }
        }
    }

    @Inject(method = "setBasking", at = @At("HEAD"), cancellable = true)
    private void coraline$noBaskingNearFood(boolean basking, CallbackInfo ci) {
        if (basking && this.coraline$foodNearby) {
            ci.cancel();
        }
    }

    @Override
    public void coraline$setEating(boolean eating) {
        ((EntitySeal) (Object) this).getEntityData().set(CORALINE$EATING, eating);
    }

    @Override
    public float coraline$getPickupProgress(float partialTick) {
        return Mth.lerp(partialTick, this.coraline$prevPickupProgress, this.coraline$pickupProgress);
    }

    @Inject(method = "canTargetItem", at = @At("HEAD"), cancellable = true)
    private void coraline$disableItemPickup(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}