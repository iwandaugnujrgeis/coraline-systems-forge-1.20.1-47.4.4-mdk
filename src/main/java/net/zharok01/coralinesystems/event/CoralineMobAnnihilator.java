package net.zharok01.coralinesystems.event;

import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.CoralineBannedMobs;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public final class CoralineMobAnnihilator {

    private CoralineMobAnnihilator() {
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {

        if (!event.getEntity().level().isClientSide && CoralineBannedMobs.isBanned(event.getEntity().getType())) {

            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {

        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide && CoralineBannedMobs.isBanned(mob.getType())) {

            event.setCanceled(true);
            mob.discard();
        }
    }
}