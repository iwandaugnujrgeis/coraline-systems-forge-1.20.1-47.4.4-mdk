package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.entity.ai.PigEatItemsGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pig.class)
public abstract class PigMixin extends Animal {

    protected PigMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void coraline$addEatItemsGoal(CallbackInfo ci) {
        this.goalSelector.addGoal(3, new PigEatItemsGoal((Pig) (Object) this));
    }
}