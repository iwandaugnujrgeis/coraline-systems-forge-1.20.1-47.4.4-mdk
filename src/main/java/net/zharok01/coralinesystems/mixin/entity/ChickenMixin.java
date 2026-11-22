package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zharok01.coralinesystems.entity.ai.BreedEggDropGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chicken.class)
public abstract class ChickenMixin {

    @Shadow public int eggTime;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void cancelRandomEggTimer(CallbackInfo ci) {
        this.eggTime = 6000;
    }

    @Redirect(
            method = "registerGoals",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V")
    )
    private void redirectBreedGoal(GoalSelector goalSelector, int priority, Goal goal) {
        if (goal instanceof BreedGoal) {
            Chicken chicken = (Chicken) (Object) this;
            goalSelector.addGoal(priority, new BreedEggDropGoal(chicken, 1.0, new ItemStack(Items.EGG)));
        } else {
            goalSelector.addGoal(priority, goal);
        }
    }
}