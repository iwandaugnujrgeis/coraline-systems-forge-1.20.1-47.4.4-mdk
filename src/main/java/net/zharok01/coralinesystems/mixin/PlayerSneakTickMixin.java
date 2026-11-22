package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.zharok01.coralinesystems.util.interfaces.SneakAccessor;

@Mixin(Player.class)
public abstract class PlayerSneakTickMixin implements SneakAccessor {

    @Unique
    private float coraline$prevSneakAmount = 0.0F;

    @Unique
    private float coraline$sneakAmount = 0.0F;

    @Unique
    private static final float coraline$SNEAK_LERP_SPEED = 0.5F;

    @Inject(method = "tick", at = @At("TAIL"))
    private void coraline$tickSneakAmount(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        this.coraline$prevSneakAmount = this.coraline$sneakAmount;

        float target = self.isShiftKeyDown() ? 1.0F : 0.0F;
        this.coraline$sneakAmount += (target - this.coraline$sneakAmount) * coraline$SNEAK_LERP_SPEED;
    }

    @Unique
    @Override
    public float coraline$getSneakAmount(float partialTick) {
        return this.coraline$prevSneakAmount
                + (this.coraline$sneakAmount - this.coraline$prevSneakAmount) * partialTick;
    }
}