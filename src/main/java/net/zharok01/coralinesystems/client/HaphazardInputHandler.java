package net.zharok01.coralinesystems.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineEffects;

/**
 * Listens to MovementInputUpdateEvent on the FORGE bus (client-only dist).
 *
 * This fires from ForgeHooksClientMixin.onMovementInputUpdate(), which is called
 * inside LocalPlayer.aiStep() immediately after input.tick() — meaning the
 * Input object is already populated with this tick's raw WASD values.
 * We simply negate forward and strafe impulses when the local player has Haphazard.
 *
 * Camera rotation is intentionally NOT touched: inverting mouse look
 * would be unfair and disorienting beyond the point of fun.
 */
@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public class HaphazardInputHandler {

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();

        // Guard: only act on the local player. In a LAN/server context there
        // will be other player entities in the level; we must not touch them.
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) {
            return;
        }

        if (player.hasEffect(CoralineEffects.HAPHAZARD.get())) {
            // event.getInput() returns the live Input object being built this tick.
            // Negating forwardImpulse reverses W/S; negating leftImpulse reverses A/D.
            event.getInput().forwardImpulse = -event.getInput().forwardImpulse;
            event.getInput().leftImpulse   = -event.getInput().leftImpulse;
        }
    }
}