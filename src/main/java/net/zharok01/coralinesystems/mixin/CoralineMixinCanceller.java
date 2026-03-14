package net.zharok01.coralinesystems.mixin;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public class CoralineMixinCanceller implements MixinCanceller {

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
		return switch (mixinClassName) {
			case "com.ordana.spelunkery.mixins.MineshaftMixin",
				 "net.mehvahdjukaar.supplementaries.mixins.StrongholdRoomSconceMixin",
				 "net.mehvahdjukaar.supplementaries.mixins.StrongholdCrossingSconceMixin",
				 "com.teamabnormals.caverns_and_chasms.core.mixin.CreeperMixin",
				 "net.lerariemann.infinity.mixin.core.NetherPortalBlockMixin",
				 "net.lerariemann.infinity.mixin.core.NetherPortalMixin" -> true;
			default -> false;
		};
	}

}
