package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.client.CoralineCapeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerCapeMixin {

    // @Shadow allows us to use methods that exist in the vanilla class we are mixing into
    @Shadow public abstract boolean isCapeLoaded();
    @Shadow public abstract boolean isElytraLoaded();

    @Inject(method = "getCloakTextureLocation", at = @At("RETURN"), cancellable = true)
    private void coraline$overrideCloakTexture(CallbackInfoReturnable<ResourceLocation> cir) {
        // Use the vanilla method to check if the player data is loaded yet
        if (!this.isCapeLoaded()) return;

        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        if (CoralineCapeRegistry.hasCape(self)) {
            cir.setReturnValue(CoralineCapeRegistry.getCape(self));
        }
    }

    @Inject(method = "getElytraTextureLocation", at = @At("RETURN"), cancellable = true)
    private void coraline$overrideElytraTexture(CallbackInfoReturnable<ResourceLocation> cir) {
        // Use the vanilla method to check if the player data is loaded yet
        if (!this.isElytraLoaded()) return;

        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        if (CoralineCapeRegistry.hasCape(self)) {
            cir.setReturnValue(CoralineCapeRegistry.getCape(self));
        }
    }
}