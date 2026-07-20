package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.zharok01.coralinesystems.entity.HelperEntity;

import java.util.EnumSet;

/**
 * Makes a jamming Helper randomly hop in place, as if in a moment of joy.
 * Purely cosmetic — does not claim MOVE/LOOK, so it layers on top of
 * whatever movement animal is already running (e.g. the wander animal).
 */
public class HelperJumpForJoyGoal extends Goal {
    private final HelperEntity helper;

    // 1-in-N chance per tick this animal is checked to trigger a hop.
    private static final int JUMP_CHANCE = 40;

    public HelperJumpForJoyGoal(HelperEntity helper) {
        this.helper = helper;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        return this.helper.isJamming()
                && this.helper.onGround()
                && this.helper.getRandom().nextInt(JUMP_CHANCE) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        // Instant action — one hop request per activation.
        return false;
    }

    @Override
    public void start() {
        this.helper.getJumpControl().jump();
    }
}