package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.zharok01.coralinesystems.entity.HelperEntity;

import java.util.EnumSet;

public class HelperJumpForJoyGoal extends Goal {
    private final HelperEntity helper;

    private static final int JUMP_CHANCE = 40;

    public HelperJumpForJoyGoal(HelperEntity helper) {
        this.helper = helper;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        return this.helper.isJamming()
                && this.helper.onGround()
                && this.helper.getRandom().nextInt(JUMP_CHANCE) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        this.helper.getJumpControl().jump();
    }
}
