package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.block.IMaglevRail;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Targets vanilla {@code AbstractMinecart} (Forge/vanilla class, readable
 * Parchment names in the decompiled source) — no {@code remap = false}
 * needed on this @Mixin; standard SRG remapping applies.
 */
@Mixin(AbstractMinecart.class)
public class AbstractMinecartMixin {

    /**
     * Fixes slope traversal at maglev speeds (0.8 m/tick).
     *
     * <h3>What vanilla does</h3>
     * Near the end of {@code moveAlongTrack}, after {@code applyNaturalSlowdown()},
     * vanilla corrects horizontal speed for the energy cost of climbing (or
     * gain from descending):
     * <pre>
     *   double d17 = (startY - currentRailY) * 0.05;
     *   double d18 = velocity.horizontalDistance();
     *   velocity = velocity.multiply((d18 + d17) / d18, 1.0, (d18 + d17) / d18);
     * </pre>
     * Going uphill, {@code currentRailY > startY}, so {@code d17} is negative.
     * At vanilla speed (<= 0.4 m/tick) {@code |d17|} never exceeds {@code d18},
     * so the multiplier stays positive. At maglev speed (0.8 m/tick) the cart
     * covers more distance — and climbs more Y — per tick, so once sustained
     * speed is high enough, {@code |d17| > d18} and the multiplier goes
     * negative, flipping the cart's velocity and throwing it back down the
     * slope. This only manifests once a cart has built up to that speed
     * threshold, which is why it appeared on lap 2 rather than lap 1.
     *
     * <h3>The fix</h3>
     * {@code @Redirect} the specific {@code Vec3.multiply} call that applies
     * this correction and clamp the multiplier to a minimum of {@code 0.0}
     * for any block implementing {@link IMaglevRail}. Worst case, the cart
     * momentarily loses horizontal speed instead of reversing direction;
     * natural boost/acceleration then recovers it on the next tick. All
     * non-maglev rails are completely unaffected.
     *
     * <h3>Ordinal note</h3>
     * {@code Vec3.multiply(DDD)} is called twice inside {@code moveAlongTrack}:
     * <ol>
     *   <li>ordinal 0 — the unpowered-rail brake: {@code multiply(0.5, 0.0, 0.5)}</li>
     *   <li>ordinal 1 — this slope-energy correction (our target)</li>
     * </ol>
     * Re-verify this ordinal against {@code AbstractMinecart.java} if a future
     * Forge/vanilla patch reorders these calls.
     *
     * <h3>Coverage requirement</h3>
     * Every maglev-family rail block MUST implement {@link IMaglevRail}, or
     * this guard silently does nothing for it and vanilla's unclamped math
     * runs unprotected on that block.
     */
    @Redirect(
            method = "moveAlongTrack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 1
            )
    )
    private Vec3 clampSlopeCorrectionOnMaglev(
            Vec3 velocity, double mx, double my, double mz,
            BlockPos pos, BlockState state) {

        if (!(state.getBlock() instanceof IMaglevRail)) {
            return velocity.multiply(mx, my, mz);
        }

        double safeMx = Math.max(0.0, mx);
        double safeMz = Math.max(0.0, mz);
        return velocity.multiply(safeMx, my, safeMz);
    }
}