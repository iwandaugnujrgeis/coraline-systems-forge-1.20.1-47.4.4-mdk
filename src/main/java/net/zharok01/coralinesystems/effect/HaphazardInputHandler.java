package net.zharok01.coralinesystems.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineEffects;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public class HaphazardInputHandler {

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();

        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) {
            return;
        }

        if (player.hasEffect(CoralineEffects.HAPHAZARD.get())) {
            event.getInput().forwardImpulse = -event.getInput().forwardImpulse;
            event.getInput().leftImpulse   = -event.getInput().leftImpulse;
        }
    }
}