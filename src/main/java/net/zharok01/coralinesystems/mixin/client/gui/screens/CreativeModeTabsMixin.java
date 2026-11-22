package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTabs;
import net.zharok01.coralinesystems.client.gui.creative_tabs.CoralineCreativeTabLoader;
import net.zharok01.coralinesystems.mixin.accessor.CreativeModeTabsAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeTabs.class)
public abstract class CreativeModeTabsMixin {

    @Inject(method = "tryRebuildTabContents", at = @At("HEAD"))
    private static void coraline$onTryRebuildTabContents(
            FeatureFlagSet enabledFeatures,
            boolean hasPermissions,
            HolderLookup.Provider holders,
            CallbackInfoReturnable<Boolean> cir) {

        if (CoralineCreativeTabLoader.INSTANCE.wasReloaded()) {
            CreativeModeTabsAccessor.coraline$setCachedParameters(null);
            CoralineCreativeTabLoader.INSTANCE.clearReloadedFlag();
        }
    }
}
