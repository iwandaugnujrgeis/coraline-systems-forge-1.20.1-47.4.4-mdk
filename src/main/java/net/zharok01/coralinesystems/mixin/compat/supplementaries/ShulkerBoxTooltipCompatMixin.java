package net.zharok01.coralinesystems.mixin.compat.supplementaries;

import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry;
import net.mehvahdjukaar.supplementaries.integration.ShulkerBoxTooltipCompat;
import net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip.SafePreviewProvider;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxTooltipCompat.class)
public abstract class ShulkerBoxTooltipCompatMixin {

    @SuppressWarnings("removal")
    @Inject(method = "registerProviders", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$registerSafeOnly(PreviewProviderRegistry registry, CallbackInfo ci) {
        registry.register(new ResourceLocation("supplementaries", "safe"), new SafePreviewProvider(), new Item[]{ModRegistry.SAFE_ITEM.get()});
        ci.cancel();
    }
}