package net.zharok01.coralinesystems.mixin.entity.dragon;

import com.legacy.rediscovered.entity.dragon.AbstractRedDragonEntity;
import com.legacy.rediscovered.entity.dragon.RedDragonBossEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RedDragonBossEntity.class, remap = false)
public abstract class RedDragonBossWanderMixin {

    private static final float TETHER_RADIUS = 48.0f;

    @Inject(method = "maxHorizontalWanderDistance", at = @At("HEAD"), cancellable = true)
    private void coraline$clampWanderRadius(CallbackInfoReturnable<Float> cir) {
        RedDragonBossEntity self = (RedDragonBossEntity) (Object) this;
        if (self.getPylonTime() > 0) {
            cir.setReturnValue(TETHER_RADIUS);
        }
    }

    @Inject(method = "setNewTarget", at = @At("TAIL"))
    private void coraline$clampTargetToHome(CallbackInfo ci) {
        RedDragonBossEntity self = (RedDragonBossEntity) (Object) this;
        if (self.getPylonTime() <= 0) return;

        AbstractRedDragonEntity base = (AbstractRedDragonEntity) (Object) this;
        if (base.homePos == null || base.targetVec == null) return;

        Vec3 home = Vec3.atBottomCenterOf(base.homePos);
        double distSq = base.targetVec.distanceToSqr(home);
        if (distSq > TETHER_RADIUS * TETHER_RADIUS) {
            base.targetVec = home.add(
                    base.targetVec.subtract(home).normalize().scale(TETHER_RADIUS)
            );
        }
    }
}