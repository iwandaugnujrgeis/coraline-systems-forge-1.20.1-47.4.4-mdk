package net.zharok01.coralinesystems.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventBusEvents {

    @SubscribeEvent
    public static void onPigFall(LivingFallEvent event) {
        // 1. Check if the falling entity is a Pig
        if (event.getEntity() instanceof Pig pig) {

            // 2. Ensure the fall distance is greater than 5.0 blocks (mirroring the Beta var1 check)
            if (event.getDistance() > 5.0F) {

                // 3. Check if the first passenger is a ServerPlayer
                // Using ServerPlayer ensures this only triggers on the logical server, preventing desyncs.
                if (pig.getFirstPassenger() instanceof ServerPlayer serverPlayer) {

                    // 4. Fire the custom Alex's Mobs trigger
                    CoralineTriggers.FLY_PIG.trigger(serverPlayer);
                }
            }
        }
    }
}