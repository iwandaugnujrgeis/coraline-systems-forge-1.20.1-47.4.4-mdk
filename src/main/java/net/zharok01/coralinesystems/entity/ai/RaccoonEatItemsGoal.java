package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.zharok01.coralinesystems.util.helper.EatItemsHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class RaccoonEatItemsGoal extends Goal {

    private static final float BITE_HEAL_AMOUNT = 2.0F;
    private static final double SEARCH_RADIUS = 8.0D;
    private static final double EAT_REACH_SQ = 2.25D;
    private static final double GIVE_UP_DISTANCE_SQ = 64.0D;
    private static final int MAX_BITES_PER_MEAL = 3;
    private static final int MEAL_COOLDOWN_TICKS = 600;

    private final EntityRaccoon raccoon;
    private ItemEntity targetItem;
    private int eatCooldown;
    private int biteDelay;
    private int pathRecalcTicks;
    private int bitesTaken;

    public RaccoonEatItemsGoal(EntityRaccoon raccoon) {
        this.raccoon = raccoon;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.raccoon.isBaby() || this.raccoon.isVehicle() || this.raccoon.isSitting()) return false;

        if (this.eatCooldown > 0) {
            this.eatCooldown--;
            return false;
        }

        List<ItemEntity> items = this.raccoon.level().getEntitiesOfClass(
                ItemEntity.class,
                this.raccoon.getBoundingBox().inflate(SEARCH_RADIUS),
                item -> EntityRaccoon.isRaccoonFood(item.getItem()));

        if (items.isEmpty()) return false;

        items.sort(Comparator.comparingDouble(this.raccoon::distanceToSqr));
        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.raccoon.isVehicle() || this.raccoon.isSitting()) return false;
        return this.bitesTaken < MAX_BITES_PER_MEAL
                && this.targetItem != null
                && this.targetItem.isAlive()
                && this.raccoon.distanceToSqr(this.targetItem) < GIVE_UP_DISTANCE_SQ;
    }

    @Override
    public void start() {
        this.pathRecalcTicks = 0;
        this.bitesTaken = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.raccoon.getNavigation().stop();
        this.eatCooldown = reducedTickDelay(this.bitesTaken >= MAX_BITES_PER_MEAL ? MEAL_COOLDOWN_TICKS : EatItemsHelper.COOLDOWN_TICKS);
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) return;

        this.raccoon.getLookControl().setLookAt(this.targetItem, 10.0F, (float) this.raccoon.getMaxHeadXRot());

        if (this.biteDelay > 0) this.biteDelay--;

        if (this.raccoon.distanceToSqr(this.targetItem) < EAT_REACH_SQ) {
            if (this.biteDelay == 0) {
                EatItemsHelper.eatOne(this.raccoon, this.targetItem, SoundEvents.GENERIC_EAT);
                this.raccoon.heal(BITE_HEAL_AMOUNT);
                this.bitesTaken++;
                this.biteDelay = EatItemsHelper.BITE_DELAY_TICKS;
            }
        } else if (--this.pathRecalcTicks <= 0) {
            this.pathRecalcTicks = this.adjustedTickDelay(10);
            this.raccoon.getNavigation().moveTo(this.targetItem, 1.2D);
        }
    }
}