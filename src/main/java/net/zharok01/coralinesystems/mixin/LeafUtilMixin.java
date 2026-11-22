package net.zharok01.coralinesystems.mixin;

import de.cheaterpaul.fallingleaves.util.LeafUtil;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeafUtil.class)
public class LeafUtilMixin {

    @Inject(
            method = "trySpawnLeafParticle",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void coraline$disableOnFastGraphics(CallbackInfo ci) {
        if (Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FAST) {
            ci.cancel();
        }
    }
}