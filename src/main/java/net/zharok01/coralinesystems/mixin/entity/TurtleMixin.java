package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.entity.ai.BreedEggDropGoal;
import net.zharok01.coralinesystems.registry.CoralineItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Turtle.class)
public abstract class TurtleMixin {

    @Redirect(
            method = "registerGoals",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"
            )
    )
    private void redirectTurtleGoals(GoalSelector goalSelector, int priority, Goal goal) {
        if (goal instanceof BreedGoal) {
            Turtle turtle = (Turtle) (Object) this;
            goalSelector.addGoal(priority, new BreedEggDropGoal(turtle, 1.0, new ItemStack(CoralineItems.TURTLE_EGG.get())));
        } else if (goal.getClass().getSimpleName().equals("TurtleLayEggGoal")) {
        } else {
            goalSelector.addGoal(priority, goal);
        }
    }
}