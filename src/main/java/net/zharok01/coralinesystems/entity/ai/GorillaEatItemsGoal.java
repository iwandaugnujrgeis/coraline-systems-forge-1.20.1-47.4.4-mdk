package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityGorilla;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.util.access.GorillaPickupAccess;
import net.zharok01.coralinesystems.util.helper.EatItemsHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class GorillaEatItemsGoal extends Goal {

    private static final double SEARCH_RADIUS = 10.0D;
    private static final double EAT_REACH_SQ = 4.0D;
    private static final double GIVE_UP_DISTANCE_SQ = 144.0D;
    private static final float BITE_HEAL_AMOUNT = 4.0F;
    private static final float TAME_CHANCE = 0.3F;

    private final EntityGorilla gorilla;
    private ItemEntity targetItem;
    private int eatCooldown;
    private int biteDelay;
    private int pathRecalcTicks;

    public GorillaEatItemsGoal(EntityGorilla gorilla) {
        this.gorilla = gorilla;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.gorilla.isPassenger() || this.gorilla.getTarget() != null) return false;

        if (this.eatCooldown > 0) {
            this.eatCooldown--;
            return false;
        }

        List<ItemEntity> items = this.gorilla.level().getEntitiesOfClass(
                ItemEntity.class,
                this.gorilla.getBoundingBox().inflate(SEARCH_RADIUS),
                item -> item.getItem().is(AMTagRegistry.GORILLA_FOODSTUFFS));

        if (items.isEmpty()) return false;

        items.sort(Comparator.comparingDouble(this.gorilla::distanceToSqr));
        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.gorilla.isPassenger() || this.gorilla.getTarget() != null) return false;
        return this.targetItem != null
                && this.targetItem.isAlive()
                && this.gorilla.distanceToSqr(this.targetItem) < GIVE_UP_DISTANCE_SQ;
    }

    @Override
    public void start() {
        this.pathRecalcTicks = 0;
    }

    @Override
    public void stop() {
        this.targetItem = null;
        ((GorillaPickupAccess) this.gorilla).coraline$setPickingUp(false);
        this.gorilla.getNavigation().stop();
        this.eatCooldown = reducedTickDelay(EatItemsHelper.COOLDOWN_TICKS);
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) return;

        GorillaPickupAccess pickup = (GorillaPickupAccess) this.gorilla;
        this.gorilla.getLookControl().setLookAt(this.targetItem, 10.0F, (float) this.gorilla.getMaxHeadXRot());

        if (this.biteDelay > 0) this.biteDelay--;

        if (this.gorilla.distanceToSqr(this.targetItem) < EAT_REACH_SQ) {
            this.gorilla.getNavigation().stop();

            this.gorilla.setStanding(false);

            if (!pickup.coraline$isPickingUp()) {
                pickup.coraline$setPickingUp(true);
                this.biteDelay = Math.max(this.biteDelay, GorillaPickupAccess.POSE_TICKS);
            }

            if (this.biteDelay == 0) eatItem();
        } else {
            pickup.coraline$setPickingUp(false);
            if (--this.pathRecalcTicks <= 0) {
                this.pathRecalcTicks = this.adjustedTickDelay(10);
                this.gorilla.getNavigation().moveTo(this.targetItem, 1.0D);
            }
        }
    }

    private void eatItem() {
        Entity thrower = this.targetItem.getOwner();
        boolean tameFood = EntityGorilla.isTameableFood(this.targetItem.getItem()) && !this.gorilla.isTame();

        EatItemsHelper.eatOne(this.gorilla, this.targetItem, SoundEvents.GENERIC_EAT);
        this.gorilla.heal(BITE_HEAL_AMOUNT);
        this.biteDelay = EatItemsHelper.BITE_DELAY_TICKS;

        if (tameFood && thrower instanceof Player player) {
            if (this.gorilla.getRandom().nextFloat() < TAME_CHANCE) {
                this.gorilla.tame(player);
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.TAME_ANIMAL.trigger(serverPlayer, this.gorilla);
                }
                this.gorilla.level().broadcastEntityEvent(this.gorilla, (byte) 7);
            } else {
                this.gorilla.level().broadcastEntityEvent(this.gorilla, (byte) 6);
            }
        }
    }
}