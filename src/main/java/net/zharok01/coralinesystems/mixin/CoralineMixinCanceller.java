package net.zharok01.coralinesystems.mixin;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public class CoralineMixinCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        if (mixinClassName.equals("com.ordana.spelunkery.mixins.MineshaftMixin")) {
            return true;
        }
        if (mixinClassName.equals("net.mehvahdjukaar.supplementaries.mixins.StrongholdRoomSconceMixin")) {
            return true;
        }
        if (mixinClassName.equals("net.mehvahdjukaar.supplementaries.mixins.StrongholdCrossingSconceMixin")) {
            return true;
        }
        if (mixinClassName.equals("com.teamabnormals.caverns_and_chasms.core.mixin.CreeperMixin")) {
            return true;
        }
        return false;
    }
}