package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.util.helper.EatItemsHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class CrowEatItemsGoal extends Goal {

    private static final float BITE_HEAL_AMOUNT = 4.0F;
    private static final double SEARCH_RADIUS = 12.0D;
    private static final double EAT_REACH_SQ = 2.25D;
    private static final double GIVE_UP_DISTANCE_SQ = 256.0D;
    private static final double WALK_DISTANCE_SQ = 64.0D;
    private static final int COMMAND_COLLECT = 3;

    private final EntityCrow crow;
    private ItemEntity targetItem;
    private int eatCooldown;
    private int biteDelay;
    private int pathRecalcTicks;

    public CrowEatItemsGoal(EntityCrow crow) {
        this.crow = crow;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.crow.isBaby() || this.crow.isVehicle() || this.crow.isPassenger() || this.crow.isSitting()) return false;
        if (this.crow.isTame() && this.crow.getCommand() == COMMAND_COLLECT) return false;
        if (this.crow.getTarget() != null) return false;

        if (this.eatCooldown > 0) {
            this.eatCooldown--;
            return false;
        }

        List<ItemEntity> items = this.crow.level().getEntitiesOfClass(
                ItemEntity.class,
                this.crow.getBoundingBox().inflate(SEARCH_RADIUS),
                item -> isCrowFood(item.getItem()));

        items.sort(Comparator.comparingDouble(this.crow::distanceToSqr));
        if (items.isEmpty()) return false;

        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.crow.isVehicle() || this.crow.isPassenger() || this.crow.isSitting()) return false;
        if (this.crow.isTame() && this.crow.getCommand() == COMMAND_COLLECT) return false;
        return this.targetItem != null
                && this.targetItem.isAlive()
                && this.crow.distanceToSqr(this.targetItem) < GIVE_UP_DISTANCE_SQ;
    }

    @Override
    public void start() {
        this.pathRecalcTicks = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.crow.getNavigation().stop();
        this.eatCooldown = reducedTickDelay(EatItemsHelper.COOLDOWN_TICKS);
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) return;

        this.crow.getLookControl().setLookAt(this.targetItem, 10.0F, (float) this.crow.getMaxHeadXRot());

        if (this.biteDelay > 0) this.biteDelay--;

        if (this.crow.distanceToSqr(this.targetItem) < EAT_REACH_SQ) {
            if (this.biteDelay == 0) {
                EatItemsHelper.eatOne(this.crow, this.targetItem, SoundEvents.GENERIC_EAT);
                this.crow.heal(BITE_HEAL_AMOUNT);
                this.crow.peck();
                this.biteDelay = EatItemsHelper.BITE_DELAY_TICKS;
            }
        } else if (this.crow.distanceToSqr(this.targetItem) <= WALK_DISTANCE_SQ && !this.crow.isFlying()) {
            if (--this.pathRecalcTicks <= 0) {
                this.pathRecalcTicks = this.adjustedTickDelay(10);
                this.crow.getNavigation().moveTo(this.targetItem, 1.0D);
            }
        } else {
            double dx = this.crow.getX() - this.targetItem.getX();
            double dz = this.crow.getZ() - this.targetItem.getZ();
            float horizontalDistance = Mth.sqrt((float) (dx * dx + dz * dz));
            double hoverHeight = horizontalDistance < 5.0F ? 0.0D : 1.8D;

            this.crow.setFlying(true);
            this.crow.getMoveControl().setWantedPosition(this.targetItem.getX(), this.targetItem.getY() + hoverHeight, this.targetItem.getZ(), 1.0D);
        }
    }

    private static boolean isCrowFood(ItemStack stack) {
        return stack.getItem().isEdible() || stack.is(AMTagRegistry.CROW_FOODSTUFFS);
    }
}