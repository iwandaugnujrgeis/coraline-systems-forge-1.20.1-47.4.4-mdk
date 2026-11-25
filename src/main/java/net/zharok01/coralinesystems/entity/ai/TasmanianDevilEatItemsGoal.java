package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityTasmanianDevil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.zharok01.coralinesystems.util.helper.EatItemsHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class TasmanianDevilEatItemsGoal extends Goal {

    private static final double SEARCH_RADIUS = 10.0D;
    private static final double EAT_REACH_SQ = 2.25D;
    private static final double GIVE_UP_DISTANCE_SQ = 144.0D;
    private static final float BITE_HEAL_AMOUNT = 5.0F;

    private final EntityTasmanianDevil devil;
    private ItemEntity targetItem;
    private int eatCooldown;
    private int biteDelay;
    private int pathRecalcTicks;

    public TasmanianDevilEatItemsGoal(EntityTasmanianDevil devil) {
        this.devil = devil;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.devil.isSitting() || this.devil.isBasking() || this.devil.getTarget() != null) return false;

        if (this.eatCooldown > 0) {
            this.eatCooldown--;
            return false;
        }

        List<ItemEntity> items = this.devil.level().getEntitiesOfClass(
                ItemEntity.class,
                this.devil.getBoundingBox().inflate(SEARCH_RADIUS),
                item -> {
                    FoodProperties food = item.getItem().getFoodProperties(this.devil);
                    return (food != null && food.isMeat()) || item.getItem().is(Items.BONE);
                });

        if (items.isEmpty()) return false;

        items.sort(Comparator.comparingDouble(this.devil::distanceToSqr));
        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.devil.isSitting() || this.devil.isBasking() || this.devil.getTarget() != null) return false;
        return this.targetItem != null
                && this.targetItem.isAlive()
                && this.devil.distanceToSqr(this.targetItem) < GIVE_UP_DISTANCE_SQ;
    }

    @Override
    public void start() {
        this.pathRecalcTicks = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.devil.getNavigation().stop();
        this.eatCooldown = reducedTickDelay(EatItemsHelper.COOLDOWN_TICKS);
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) return;

        this.devil.getLookControl().setLookAt(this.targetItem, 10.0F, (float) this.devil.getMaxHeadXRot());

        if (this.biteDelay > 0) this.biteDelay--;

        if (this.devil.distanceToSqr(this.targetItem) < EAT_REACH_SQ) {
            if (this.biteDelay == 0) {
                boolean isBone = this.targetItem.getItem().is(Items.BONE);
                boolean isRottenFlesh = this.targetItem.getItem().is(Items.ROTTEN_FLESH);

                EatItemsHelper.eatOne(this.devil, this.targetItem, SoundEvents.GENERIC_EAT);
                if (isBone) {
                    this.devil.dropBonemeal();
                } else {
                    this.devil.heal(BITE_HEAL_AMOUNT);
                }
                if (isRottenFlesh && this.devil.getAnimation() != EntityTasmanianDevil.ANIMATION_HOWL) {
                    this.devil.setAnimation(EntityTasmanianDevil.ANIMATION_HOWL);
                }
                this.biteDelay = EatItemsHelper.BITE_DELAY_TICKS;
            }
        } else if (--this.pathRecalcTicks <= 0) {
            this.pathRecalcTicks = this.adjustedTickDelay(10);
            this.devil.getNavigation().moveTo(this.targetItem, 1.2D);
        }
    }
}