package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.zharok01.coralinesystems.util.interfaces.SneakAccessor;

/**
 * Vanilla exposes only a boolean crouch/sneak state (Player#isCrouching,
 * Player#isShiftKeyDown) with no interpolated progress float. This mixin
 * builds that float ourselves, ticking it toward 0/1 once per game tick so
 * CrosshairRenderMixin can lerp it against partialTick at render time.
 *
 * Injects into Player#tick, which both LocalPlayer and every other Player
 * subtype run through every tick regardless of side, so this stays correct
 * even though only the client render path consumes the value.
 */
@Mixin(Player.class)
public abstract class PlayerSneakTickMixin implements SneakAccessor {

    @Unique
    private float coralinesystems$prevSneakAmount = 0.0F;

    @Unique
    private float coralinesystems$sneakAmount = 0.0F;

    // How quickly the tracked value approaches the target each tick.
    // Mirrors the "smoothing factor" pattern already used elsewhere in
    // vanilla for similar lerped values (e.g. LocalPlayer's xBob/yBob).
    @Unique
    private static final float coralinesystems$SNEAK_LERP_SPEED = 0.5F;

    @Inject(method = "tick", at = @At("TAIL"))
    private void coralinesystems$tickSneakAmount(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        this.coralinesystems$prevSneakAmount = this.coralinesystems$sneakAmount;

        float target = self.isShiftKeyDown() ? 1.0F : 0.0F;
        this.coralinesystems$sneakAmount +=
                (target - this.coralinesystems$sneakAmount) * coralinesystems$SNEAK_LERP_SPEED;

        // Snap once close enough so it doesn't asymptotically creep forever.
        if (Math.abs(target - this.coralinesystems$sneakAmount) < 0.001F) {
            this.coralinesystems$sneakAmount = target;
        }
    }

    @Unique
    @Override
    public float coralinesystems$getSneakAmount(float partialTick) {
        return this.coralinesystems$prevSneakAmount
                + (this.coralinesystems$sneakAmount - this.coralinesystems$prevSneakAmount) * partialTick;
    }
}