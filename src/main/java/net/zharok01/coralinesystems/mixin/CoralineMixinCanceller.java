package net.zharok01.coralinesystems.mixin;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public class CoralineMixinCanceller implements MixinCanceller {

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        return switch (mixinClassName) {
            case "com.ordana.spelunkery.mixins.MineshaftMixin",
                 "me.jellysquid.mods.sodium.mixin.features.gui.hooks.settings.OptionsScreenMixin",
                 "net.mehvahdjukaar.supplementaries.mixins.StrongholdRoomSconceMixin",
                 "net.mehvahdjukaar.supplementaries.mixins.StrongholdCrossingSconceMixin",
                 "com.teamabnormals.caverns_and_chasms.core.mixin.entity.CreeperMixin",
                 "com.teamabnormals.caverns_and_chasms.core.mixin.structure.MineshaftCorridorMixin",
                 "com.teamabnormals.caverns_and_chasms.core.mixin.entity.MobMixin",
                 "net.mehvahdjukaar.amendments.mixins.LargeFireballMixin",
                 "net.mehvahdjukaar.amendments.mixins.FireballMixin",
                 "com.teamabnormals.caverns_and_chasms.core.mixin.brewing.PotionBrewingMixin",
                 "net.mehvahdjukaar.amendments.mixins.ExplosionMixin",
                 "com.ninni.spawn.mixin.LeavesBlockMixin",
                 "net.mehvahdjukaar.amendments.mixins.SmallFireballMixin",
                 "com.teamabnormals.caverns_and_chasms.core.mixin.entity.WitherSkeletonMixin",
                 "com.faboslav.friendsandfoes.common.mixin.StrongholdGeneratorMixin" -> true;
            default -> false;
        };
    }
}