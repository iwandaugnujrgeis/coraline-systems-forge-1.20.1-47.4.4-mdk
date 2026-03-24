package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BreakDoorGoal.class)
public class BreakDoorGoalMixin {

    /**
     * @author zharok_01
     * @reason Force zombies to ignore the difficulty check so they break doors on Easy/Normal.
     */
    @Overwrite
    protected boolean isValidDifficulty(Difficulty difficulty) {
        // This normally checks the 'validDifficulties' predicate.
        // By returning true, we skip the Hard-only requirement.
        return true;
    }
}