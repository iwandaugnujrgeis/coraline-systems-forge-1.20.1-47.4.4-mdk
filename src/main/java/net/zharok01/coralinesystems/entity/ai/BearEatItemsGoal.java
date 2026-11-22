package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.zharok01.coralinesystems.mixin.accessor.EntityGrizzlyBearAccessor;
import net.zharok01.coralinesystems.util.helper.EatItemsHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class BearEatItemsGoal extends Goal {

    private final EntityGrizzlyBear bear;
    private ItemEntity targetItem;
    private int eatCooldown;
    private int biteDelay;
    private int pathRecalcTicks;

    public BearEatItemsGoal(EntityGrizzlyBear bear) {
        this.bear = bear;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.bear.isSitting() || this.bear.isVehicle() || this.bear.getTarget() != null) return false;

        if (this.eatCooldown > 0) {
            this.eatCooldown--;
            return false;
        }

        List<ItemEntity> items = this.bear.level().getEntitiesOfClass(
                ItemEntity.class,
                this.bear.getBoundingBox().inflate(10.0D),
                item -> item.getItem().is(AMTagRegistry.GRIZZLY_FOODSTUFFS));

        if (items.isEmpty()) return false;

        items.sort(Comparator.comparingDouble(this.bear::distanceToSqr));
        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.bear.isSitting() || this.bear.isVehicle() || this.bear.getTarget() != null) return false;
        return this.targetItem != null
                && this.targetItem.isAlive()
                && this.bear.distanceToSqr(this.targetItem) < 144.0D;
    }

    @Override
    public void start() {
        this.pathRecalcTicks = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.bear.getNavigation().stop();
        this.eatCooldown = reducedTickDelay(EatItemsHelper.COOLDOWN_TICKS);
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) return;

        this.bear.getLookControl().setLookAt(this.targetItem, 10.0F, (float) this.bear.getMaxHeadXRot());

        if (this.biteDelay > 0) this.biteDelay--;

        if (this.bear.distanceToSqr(this.targetItem) < 4.0D) {
            if (this.biteDelay == 0) eatItem();
        } else if (--this.pathRecalcTicks <= 0) {
            this.pathRecalcTicks = this.adjustedTickDelay(10);
            this.bear.getNavigation().moveTo(this.targetItem, 1.0D);
        }
    }

    private void eatItem() {
        boolean isHoney = this.targetItem.getItem().is(AMTagRegistry.GRIZZLY_HONEY);

        EatItemsHelper.eatOne(this.bear, this.targetItem, SoundEvents.GENERIC_EAT);
        this.bear.heal(isHoney ? 10.0F : 4.0F);

        if (isHoney) {
            this.bear.setHoneyed(true);
            ((EntityGrizzlyBearAccessor) this.bear).coraline$setHoneyedTime(700);
        }

        this.biteDelay = EatItemsHelper.BITE_DELAY_TICKS;
    }
}