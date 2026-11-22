package net.zharok01.coralinesystems.mixin.entity.dragon;

import com.legacy.rediscovered.entity.dragon.AbstractRedDragonEntity;
import com.legacy.rediscovered.entity.dragon.RedDragonBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractRedDragonEntity.class, remap = false)
public abstract class AbstractRedDragonTargetMixin {

    @Shadow public BlockPos homePos;

    @Unique
    private static final float TETHER_RADIUS = 48.0f;

    @Inject(method = "setDragonTarget", at = @At("HEAD"), cancellable = true)
    private void coraline$guardTetherTarget(Entity target, CallbackInfo ci) {
        if (target == null) return;

        if (!((Object) this instanceof RedDragonBossEntity boss)) return;
        if (boss.getPylonTime() <= 0 || homePos == null) return;

        Vec3 home = Vec3.atBottomCenterOf(homePos);
        if (target.position().distanceToSqr(home) > TETHER_RADIUS * TETHER_RADIUS) {
            ci.cancel();
        }
    }
}