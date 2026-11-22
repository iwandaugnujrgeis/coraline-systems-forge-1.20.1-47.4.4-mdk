package net.zharok01.coralinesystems.event;

import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.CoralineFishingTuning;
import net.zharok01.coralinesystems.registry.CoralineTags;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FishHitboxEvents {
    private FishHitboxEvents() {
    }

    @SuppressWarnings("removal")
    @SubscribeEvent
    public static void onFishSize(EntityEvent.Size event) {
        if (event.getEntity().getType().is(CoralineTags.FISH)) {
            event.setNewSize(event.getNewSize().scale(CoralineFishingTuning.FISH_HITBOX_SCALE), true);
        }
    }

    @SubscribeEvent
    public static void onFishJoin(EntityJoinLevelEvent event) {
        if (event.getEntity().getType().is(CoralineTags.FISH)) {
            event.getEntity().refreshDimensions();
        }
    }
}