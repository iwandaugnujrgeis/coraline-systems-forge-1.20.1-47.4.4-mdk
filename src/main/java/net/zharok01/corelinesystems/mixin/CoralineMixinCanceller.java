package net.zharok01.coralinesystems.mixin;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import net.minecraftforge.fml.loading.LoadingModList;

import java.util.List;

public class CoralineMixinCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {

        if (mixinClassName.equals("com.ordana.spelunkery.mixins.MineshaftMixin")) {
            System.out.println("[CoralineSystems] Cancelling Mixin: " + mixinClassName);
            return true;
        }

        if (mixinClassName.equals("com.ordana.spelunkery.worldgen.structures.MineshaftDustCorridor")) {
            System.out.println("[CoralineSystems] Cancelling Mixin: " + mixinClassName);
            return true;
        }

        // Return false for everything else
        return false;
    }
}