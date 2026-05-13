// Postponing this for now! Will take this up after vacation!
package net.zharok01.coralinesystems.mixin.husbandry;

import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zharok01.coralinesystems.content.entity.ai.UniversalBreedEggDropGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chicken.class)
public abstract class ChickenMixin {

    @Shadow public int eggTime;

    /**
     * Goal 1: Remove random Egg spawning without breeding.
     * We inject at the head of aiStep to constantly reset the eggTime timer.
     * This prevents the '--this.eggTime <= 0' condition from ever being met.
     */
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void cancelRandomEggTimer(CallbackInfo ci) {
        // Reset the timer to a high value every tick so it never reaches zero.
        this.eggTime = 6000;
    }

    /**
     * Goal 2 & 3: Replace the baby-spawning BreedGoal with our Egg-dropping version.
     * We redirect the 'addGoal' call specifically when it is trying to add a BreedGoal.
     */
    @Redirect(
            method = "registerGoals",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V")
    )
    private void redirectBreedGoal(GoalSelector goalSelector, int priority, Goal goal) {
        // If the game is trying to add the standard BreedGoal, add our custom one instead.
        if (goal instanceof BreedGoal) {
            Chicken chicken = (Chicken) (Object) this;
            goalSelector.addGoal(priority, new UniversalBreedEggDropGoal(chicken, 1.0, new ItemStack(Items.EGG)));
        } else {
            // For all other goals (Panic, Float, etc.), let them pass through normally.
            goalSelector.addGoal(priority, goal);
        }
    }
}