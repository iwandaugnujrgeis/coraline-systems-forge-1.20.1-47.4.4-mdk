package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FollowOwnerGoal.class)
public abstract class FollowOwnerGoalMixin {

    @Shadow
    @Final
    private PathNavigation navigation;

    @Shadow
    private LivingEntity owner;

    @Shadow
    @Final
    private double speedModifier;

    @Inject(method = "teleportToOwner", at = @At("HEAD"), cancellable = true)
    private void coraline$followOwnerWithoutTeleporting(CallbackInfo ci) {
        this.navigation.moveTo(this.owner, this.speedModifier);
        ci.cancel();
    }
}
