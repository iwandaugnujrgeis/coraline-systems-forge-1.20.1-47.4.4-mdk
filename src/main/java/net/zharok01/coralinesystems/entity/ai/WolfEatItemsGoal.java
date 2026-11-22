package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineTags;
import net.zharok01.coralinesystems.util.helper.EatItemsHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class WolfEatItemsGoal extends Goal {

    private final Wolf wolf;
    private ItemEntity targetItem;
    private int eatCooldown;
    private int biteDelay;
    private int pathRecalcTicks;

    public WolfEatItemsGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.coraline$canEatAtCurrentHealth()) return false;
        if (this.wolf.isOrderedToSit() || this.wolf.getTarget() != null) return false;

        if (this.eatCooldown > 0) {
            this.eatCooldown--;
            return false;
        }

        List<ItemEntity> items = this.wolf.level().getEntitiesOfClass(
                ItemEntity.class,
                this.wolf.getBoundingBox().inflate(10.0D),
                item -> item.getItem().is(CoralineTags.WOLF_MEAT_DROPPED));

        if (items.isEmpty()) return false;

        items.sort(Comparator.comparingDouble(this.wolf::distanceToSqr));
        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.coraline$canEatAtCurrentHealth()) return false;
        if (this.wolf.isOrderedToSit() || this.wolf.getTarget() != null) return false;
        return this.targetItem != null
                && this.targetItem.isAlive()
                && this.wolf.distanceToSqr(this.targetItem) < 144.0D;
    }

    private boolean coraline$canEatAtCurrentHealth() {
        return !this.wolf.isTame() || this.wolf.getHealth() < this.wolf.getMaxHealth();
    }

    @Override
    public void start() {
        this.pathRecalcTicks = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.wolf.getNavigation().stop();
        this.eatCooldown = reducedTickDelay(EatItemsHelper.COOLDOWN_TICKS);
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) return;

        this.wolf.getLookControl().setLookAt(this.targetItem, 10.0F, (float) this.wolf.getMaxHeadXRot());

        if (this.biteDelay > 0) this.biteDelay--;

        if (this.wolf.distanceToSqr(this.targetItem) < 2.25D) {
            if (this.biteDelay == 0) {
                FoodProperties food = this.targetItem.getItem().getFoodProperties(this.wolf);
                float healAmount = food != null ? food.getNutrition() : 1.0F;

                EatItemsHelper.eatOne(this.wolf, this.targetItem, CoralineSounds.WOLF_EAT.get());
                this.wolf.heal(healAmount);
                this.biteDelay = EatItemsHelper.BITE_DELAY_TICKS;
            }
        } else if (--this.pathRecalcTicks <= 0) {
            this.pathRecalcTicks = this.adjustedTickDelay(10);
            this.wolf.getNavigation().moveTo(this.targetItem, 1.2D);
        }
    }
}