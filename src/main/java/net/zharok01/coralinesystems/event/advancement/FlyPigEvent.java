package net.zharok01.coralinesystems.event.advancement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FlyPigEvent {

    @SubscribeEvent
    public static void onPigFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Pig pig) {
            if (event.getDistance() > 5.0F) {
                if (pig.getFirstPassenger() instanceof ServerPlayer serverPlayer) {
                    CoralineTriggers.FLY_PIG.trigger(serverPlayer);
                }
            }
        }
    }
}