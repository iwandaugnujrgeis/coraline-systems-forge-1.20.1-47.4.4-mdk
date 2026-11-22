package net.zharok01.coralinesystems.mixin.compat;

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
                    target = "Lnet/minecraft/util/RandomSource;m_188501_()F",
                    ordinal = 0
            )
    )
    private float coraline$suppressVariants(RandomSource random) {
        return Float.MAX_VALUE;
    }
}