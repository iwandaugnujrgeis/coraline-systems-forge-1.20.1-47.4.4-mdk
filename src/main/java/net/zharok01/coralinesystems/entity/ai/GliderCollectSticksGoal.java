package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class GliderCollectSticksGoal extends Goal {

    private static final double SEARCH_RADIUS = 10.0D;
    private static final double PICKUP_REACH_SQ = 2.25D;
    private static final double WALK_SPEED = 1.1D;
    private static final int SCAN_COOLDOWN_TICKS = 40;
    private static final int GIVE_UP_AFTER_TICKS = 300;

    private final EntitySugarGlider glider;
    private ItemEntity targetItem;
    private int scanCooldown;
    private int pathRecalcTicks;
    private int ticksRunning;

    public GliderCollectSticksGoal(EntitySugarGlider glider) {
        this.glider = glider;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean isStashFull() {
        return SugarGliderData.getSticks(this.glider) >= SugarGliderData.MAX_STICKS;
    }

    @Override
    public boolean canUse() {
        if (this.glider.level().isDay() || this.glider.isOrderedToSit() || this.isStashFull()) return false;

        if (this.scanCooldown > 0) {
            this.scanCooldown--;
            return false;
        }
        this.scanCooldown = reducedTickDelay(SCAN_COOLDOWN_TICKS);

        List<ItemEntity> items = this.glider.level().getEntitiesOfClass(
                ItemEntity.class,
                this.glider.getBoundingBox().inflate(SEARCH_RADIUS),
                item -> item.isAlive() && !item.hasPickUpDelay() && item.getItem().is(Items.STICK));

        if (items.isEmpty()) return false;

        items.sort(Comparator.comparingDouble(this.glider::distanceToSqr));
        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null
                && this.targetItem.isAlive()
                && !this.glider.level().isDay()
                && !this.glider.isOrderedToSit()
                && !this.isStashFull()
                && this.ticksRunning < GIVE_UP_AFTER_TICKS;
    }

    @Override
    public void start() {
        this.pathRecalcTicks = 0;
        this.ticksRunning = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.glider.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.ticksRunning++;

        if (this.glider.distanceToSqr(this.targetItem) < PICKUP_REACH_SQ) {
            this.pickUpSticks();
        } else if (--this.pathRecalcTicks <= 0) {
            this.pathRecalcTicks = this.adjustedTickDelay(10);
            this.glider.getNavigation().moveTo(this.targetItem, WALK_SPEED);
        }
    }

    private void pickUpSticks() {
        int carried = SugarGliderData.getSticks(this.glider);
        ItemStack remaining = this.targetItem.getItem().copy();
        int taken = Math.min(SugarGliderData.MAX_STICKS - carried, remaining.getCount());
        remaining.shrink(taken);

        SugarGliderData.setSticks(this.glider, carried + taken);
        this.glider.playSound(SoundEvents.ITEM_PICKUP, 0.4F, 1.6F);

        if (remaining.isEmpty()) {
            this.glider.take(this.targetItem, taken);
            this.targetItem.discard();
        } else {
            this.targetItem.setItem(remaining);
        }
    }
}