package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.entity.ai.PigEatItemsGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pig.class)
public abstract class PigMixin extends Animal {

    protected PigMixin(EntityType<? extends Animal> p_27557_, Level p_27558_) {
        super(p_27557_, p_27558_);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void coraline$addEatItemsGoal(CallbackInfo ci) {
        // Priority 3 is usually good for foraging (lower than fleeing from danger,
        // higher than wandering)
        this.goalSelector.addGoal(3, new PigEatItemsGoal((Pig) (Object) this));
    }
}