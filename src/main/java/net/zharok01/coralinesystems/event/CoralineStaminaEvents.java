package net.zharok01.coralinesystems.event;

import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaData;
import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaHelper;
import mod.adrenix.nostalgic.tweak.config.GameplayTweak;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.mixin.accessors.StaminaDataAccessor;
import net.zharok01.coralinesystems.registry.CoralineEffects;

/**
 * Server-side (FORGE bus) tick listener that implements the Persistence
 * effect's drain-halving mechanic.
 *
 * HOW DRAIN-HALVING WORKS:
 * NT's StaminaData#tick() decrements tickTimer by 1 every game tick while
 * the player is sprinting (net: −1/tick). This listener fires AFTER NT's
 * tick (PlayerTickEvent.Phase.END) and, on every other tick, adds +1 back,
 * yielding a net drain of −0.5/tick averaged over two ticks. This is the
 * only approach that doesn't require modifying NT's tick loop directly.
 *
 * The alternation is tracked per-player via a simple boolean flag stored as
 * a thread-local-style toggle on the event class itself (static is fine here
 * because server tick is single-threaded).
 *
 * BLIND SPOT: This listener fires on both the integrated-server thread (SP)
 * and the dedicated server. It deliberately does NOT run on the client side
 * (PlayerTickEvent is server-only for server players). If NT changes the
 * timing of its own tick relative to PlayerTickEvent this may need adjustment.
 */
@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CoralineStaminaEvents {

    // Per-player alternating toggle for the half-drain mechanic.
    // We key it off the player UUID string so it's isolated per player.
    // A simple java.util.HashMap is fine — server tick is single-threaded.
    private static final java.util.HashMap<String, Boolean> SKIP_TICK =
            new java.util.HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only run at the END of the tick, after NT has already run its own
        // stamina tick, and only server-side.
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (player.level().isClientSide()) return;

        // Only act if Persistence is active on this player.
        if (!player.hasEffect(CoralineEffects.PERSISTENCE.get())) {
            // Clean up the toggle map when the effect is gone.
            SKIP_TICK.remove(player.getStringUUID());
            return;
        }

        // Only relevant when NT's stamina system is actually enabled.
        if (!(Boolean) GameplayTweak.STAMINA_SPRINT.get()) return;

        // Only counteract drain while the player is actively sprinting.
        // During regen or cooldown phases we don't interfere.
        if (!player.isSprinting()) return;

        StaminaData data = StaminaHelper.get(player);
        StaminaDataAccessor accessor = (StaminaDataAccessor) data;

        // If already exhausted, no drain is happening — nothing to counteract.
        if (accessor.coraline$getIsExhausted()) return;

        // Alternate: add +1 back every other tick → net −0.5/tick.
        String uuid = player.getStringUUID();
        boolean skip = SKIP_TICK.getOrDefault(uuid, false);
        SKIP_TICK.put(uuid, !skip);

        if (!skip) {
            int current = accessor.coraline$getTickTimer();
            int max = accessor.coraline$getDurationInTicks();
            // Clamp to max — we never want to exceed full stamina.
            accessor.coraline$setTickTimer(Math.min(current + 1, max));
        }
    }
}
