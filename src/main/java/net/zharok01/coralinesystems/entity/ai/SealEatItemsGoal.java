package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntitySeal;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.util.access.SealPickupAccess;
import net.zharok01.coralinesystems.util.helper.EatItemsHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class SealEatItemsGoal extends Goal {

    private static final float BITE_HEAL_AMOUNT = 4.0F;
    public static final double SEARCH_RADIUS = 8.0D;
    private static final double EAT_REACH_SQ = 4.0D;
    private static final double GIVE_UP_DISTANCE_SQ = 64.0D;

    private final EntitySeal seal;
    private ItemEntity targetItem;
    private int eatCooldown;
    private int biteDelay;
    private int pathRecalcTicks;

    public static boolean isSealFood(ItemStack stack) {
        return stack.is(AMTagRegistry.SEAL_OFFERINGS) || stack.is(AMTagRegistry.SEAL_BREEDABLES);
    }

    public SealEatItemsGoal(EntitySeal seal) {
        this.seal = seal;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.seal.isBaby() || this.seal.isVehicle() || this.seal.isBasking()) return false;
        if (this.seal.getTarget() != null || this.seal.revengeCooldown > 0) return false;

        if (this.eatCooldown > 0) {
            this.eatCooldown--;
            return false;
        }

        List<ItemEntity> items = this.seal.level().getEntitiesOfClass(
                ItemEntity.class,
                this.seal.getBoundingBox().inflate(SEARCH_RADIUS),
                item -> isSealFood(item.getItem()));

        items.sort(Comparator.comparingDouble(this.seal::distanceToSqr));
        if (items.isEmpty()) return false;

        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.seal.isVehicle() || this.seal.isBasking() || this.seal.revengeCooldown > 0) return false;
        return this.targetItem != null
                && this.targetItem.isAlive()
                && this.seal.distanceToSqr(this.targetItem) < GIVE_UP_DISTANCE_SQ;
    }

    @Override
    public void start() {
        this.pathRecalcTicks = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.seal.getNavigation().stop();
        ((SealPickupAccess) this.seal).coraline$setEating(false);
        this.eatCooldown = reducedTickDelay(EatItemsHelper.COOLDOWN_TICKS);
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) return;

        this.seal.getLookControl().setLookAt(this.targetItem, 10.0F, (float) this.seal.getMaxHeadXRot());

        if (this.biteDelay > 0) this.biteDelay--;

        boolean inReach = this.seal.distanceToSqr(this.targetItem) < EAT_REACH_SQ;
        ((SealPickupAccess) this.seal).coraline$setEating(inReach);

        if (inReach) {
            if (this.biteDelay == 0) {
                EatItemsHelper.eatOne(this.seal, this.targetItem, SoundEvents.GENERIC_EAT);
                this.seal.heal(BITE_HEAL_AMOUNT);
                this.biteDelay = EatItemsHelper.BITE_DELAY_TICKS;
            }
        } else if (--this.pathRecalcTicks <= 0) {
            this.pathRecalcTicks = this.adjustedTickDelay(10);
            this.seal.getNavigation().moveTo(this.targetItem, 1.2D);
        }
    }
}