package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineTags;
import net.zharok01.coralinesystems.util.helper.EatItemsHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class PigEatItemsGoal extends Goal {

    private final Pig pig;
    private ItemEntity targetItem;
    private int eatCooldown;
    private int biteDelay;
    private int pathRecalcTicks;

    public PigEatItemsGoal(Pig pig) {
        this.pig = pig;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.pig.isBaby() || this.pig.isVehicle()) return false;

        if (this.eatCooldown > 0) {
            this.eatCooldown--;
            return false;
        }

        List<ItemEntity> items = this.pig.level().getEntitiesOfClass(
                ItemEntity.class,
                this.pig.getBoundingBox().inflate(8.0D),
                item -> item.getItem().is(CoralineTags.PIG_FOOD_DROPPED));

        if (items.isEmpty()) return false;

        items.sort(Comparator.comparingDouble(this.pig::distanceToSqr));
        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.pig.isVehicle()) return false;
        return this.targetItem != null
                && this.targetItem.isAlive()
                && this.pig.distanceToSqr(this.targetItem) < 64.0D;
    }

    @Override
    public void start() {
        this.pathRecalcTicks = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.pig.getNavigation().stop();
        this.eatCooldown = reducedTickDelay(EatItemsHelper.COOLDOWN_TICKS);
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) return;

        this.pig.getLookControl().setLookAt(this.targetItem, 10.0F, (float) this.pig.getMaxHeadXRot());

        if (this.biteDelay > 0) this.biteDelay--;

        if (this.pig.distanceToSqr(this.targetItem) < 2.25D) {
            if (this.biteDelay == 0) {
                EatItemsHelper.eatOne(this.pig, this.targetItem, CoralineSounds.PIG_EAT.get());
                this.pig.heal(1.0F);
                this.biteDelay = EatItemsHelper.BITE_DELAY_TICKS;
            }
        } else if (--this.pathRecalcTicks <= 0) {
            this.pathRecalcTicks = this.adjustedTickDelay(10);
            this.pig.getNavigation().moveTo(this.targetItem, 1.2D);
        }
    }
}