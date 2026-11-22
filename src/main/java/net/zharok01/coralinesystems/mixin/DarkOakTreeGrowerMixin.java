package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.DarkOakTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.zharok01.coralinesystems.CoralineSystems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DarkOakTreeGrower.class)
public class DarkOakTreeGrowerMixin {

    @Inject(method = "getConfiguredFeature", at = @At("HEAD"), cancellable = true)
    private void coraline$soloDarkOakFeature(RandomSource random, boolean hasFlowers, CallbackInfoReturnable<ResourceKey<ConfiguredFeature<?, ?>>> cir) {
        cir.setReturnValue(ResourceKey.create(Registries.CONFIGURED_FEATURE, CoralineSystems.of("dark_fancy_oak")));
    }
}