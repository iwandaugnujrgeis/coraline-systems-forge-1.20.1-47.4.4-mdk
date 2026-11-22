package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.zharok01.coralinesystems.util.CoralineStatBlacklist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Stats.class)
public abstract class StatsMixin {

    @SuppressWarnings("removal")
    @Inject(method = "makeCustomStat", at = @At("HEAD"), cancellable = true)
    private static void onMakeCustomStat(String key, StatFormatter formatter, CallbackInfoReturnable<ResourceLocation> cir) {
        if (CoralineStatBlacklist.IDS.contains(key)) {
            ResourceLocation resourceLocation = new ResourceLocation(key);
            Registry.register(BuiltInRegistries.CUSTOM_STAT, key, resourceLocation);
            cir.setReturnValue(resourceLocation);
        }
    }
}