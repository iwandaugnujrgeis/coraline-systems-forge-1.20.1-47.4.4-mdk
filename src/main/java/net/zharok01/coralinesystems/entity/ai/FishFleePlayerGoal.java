package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.util.CoralineFishingTuning;

public class FishFleePlayerGoal extends AvoidEntityGoal<Player> {
    private final AbstractFish fish;

    public FishFleePlayerGoal(AbstractFish fish) {
        super(fish, Player.class, CoralineFishingTuning.FLEE_DETECTION_RADIUS, CoralineFishingTuning.FLEE_SPEED_MODIFIER, CoralineFishingTuning.FLEE_SPEED_MODIFIER, CoralineFishingTuning::isScaryToFish);
        this.fish = fish;
    }

    @Override
    public boolean canUse() {
        return !this.isBusyWithRod() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.isBusyWithRod() && super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        ((LurableFish) this.fish).coraline$setExtraSwimAcceleration(CoralineFishingTuning.FLEE_EXTRA_SWIM_ACCELERATION);
    }

    @Override
    public void stop() {
        super.stop();
        ((LurableFish) this.fish).coraline$setExtraSwimAcceleration(0.0);
    }

    private boolean isBusyWithRod() {
        LurableFish lurable = (LurableFish) this.fish;
        return lurable.coraline$getLureHook() != null || lurable.coraline$isReeling();
    }
}