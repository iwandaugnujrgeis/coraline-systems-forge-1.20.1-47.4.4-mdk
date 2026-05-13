package net.zharok01.coralinesystems.content.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineTags;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class PigEatItemsGoal extends Goal {
    private final Pig pig;
    private ItemEntity targetItem;

    public PigEatItemsGoal(Pig pig) {
        this.pig = pig;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.pig.isBaby() || this.pig.getHealth() <= 0) return false;

        // Find nearby items matching the tag
        List<ItemEntity> items = this.pig.level().getEntitiesOfClass(ItemEntity.class,
                this.pig.getBoundingBox().inflate(8.0D),
                item -> item.getItem().is(CoralineTags.PIG_FOOD_DROPPED));

        if (items.isEmpty()) return false;

        // Target the closest one
        items.sort(Comparator.comparingDouble(this.pig::distanceToSqr));
        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return targetItem != null && targetItem.isAlive() && this.pig.distanceToSqr(targetItem) < 64.0D;
    }

    @Override
    public void start() {
        this.pig.getNavigation().moveTo(this.targetItem, 1.2D);
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) return;

        // Move towards item
        this.pig.getLookControl().setLookAt(this.targetItem, 10.0F, (float)this.pig.getMaxHeadXRot());
        this.pig.getNavigation().moveTo(this.targetItem, 1.2D);

        // If close enough, "eat" the item
        if (this.pig.distanceToSqr(this.targetItem) < 1.5D) {
            eatItem();
        }
    }

    private void eatItem() {
        ItemStack stack = this.targetItem.getItem();

        // Visuals/Sounds
        this.pig.playSound(CoralineSounds.PIG_EAT.get(), 1.0F, 0.9F);

        // Healing or growth logic (optional)
        if (this.pig.getHealth() < this.pig.getMaxHealth()) {
            this.pig.heal(1.0F);
        }

        stack.shrink(1);
        if (stack.isEmpty()) {
            this.targetItem.discard();
        }
    }
}