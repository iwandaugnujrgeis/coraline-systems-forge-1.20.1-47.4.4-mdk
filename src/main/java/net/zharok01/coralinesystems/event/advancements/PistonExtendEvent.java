package net.zharok01.coralinesystems.event.advancements;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PistonExtendEvent {

    // Radius within which a player must be standing to receive the trigger
    private static final double TRIGGER_RADIUS = 16.0;

    @SubscribeEvent
    public static void onPistonExtend(PistonEvent.Post event) {
        // Only care about extensions, not retractions
        if (event.getPistonMoveType() != PistonEvent.PistonMoveType.EXTEND) {
            return;
        }

        // PistonEvent fires on both sides — only trigger server-side
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Build an AABB centered on the piston block and scan for nearby players
        AABB searchBox = AABB.ofSize(
                event.getPos().getCenter(),
                TRIGGER_RADIUS,
                TRIGGER_RADIUS,
                TRIGGER_RADIUS
        );

        serverLevel.getEntitiesOfClass(ServerPlayer.class, searchBox)
                .forEach(CoralineTriggers.PISTON_EXTEND::trigger);
    }
}