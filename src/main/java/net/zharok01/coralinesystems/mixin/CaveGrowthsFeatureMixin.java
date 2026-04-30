package net.zharok01.coralinesystems.mixin;

import com.teamabnormals.caverns_and_chasms.common.levelgen.feature.CaveGrowthsFeature;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CaveGrowthsFeature.class, remap = false)
public class CaveGrowthsFeatureMixin {

    @Redirect(
            method = "m_142674_",
            at = @At(
                    value = "INVOKE",
                    // RandomSource.nextFloat() — SRG name required with remap = false
                    target = "Lnet/minecraft/util/RandomSource;m_188501_()F",
                    ordinal = 0  // targets ONLY the first nextFloat() call — the variant chance check
                    // ordinal = 1 would be the moschatel chance check, which we leave alone
            )
    )
    private float coraline$suppressVariants(RandomSource random) {
        // Return a value larger than the maximum possible variantChance (1.0F).
        // This makes "random.nextFloat() < variantChance" permanently false,
        // so the variant selection block is never entered.
        return Float.MAX_VALUE;
    }
}