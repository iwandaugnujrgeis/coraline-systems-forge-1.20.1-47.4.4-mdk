package net.zharok01.coralinesystems.event.stamina;

import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaData;
import mod.adrenix.nostalgic.helper.gameplay.stamina.StaminaHelper;
import mod.adrenix.nostalgic.tweak.config.GameplayTweak;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.mixin.accessor.StaminaDataAccessor;
import net.zharok01.coralinesystems.registry.CoralineEffects;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StaminaEvents {

    private static final java.util.HashMap<String, Boolean> SKIP_TICK =
            new java.util.HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (player.level().isClientSide()) return;

        if (!player.hasEffect(CoralineEffects.PERSISTENCE.get())) {
            SKIP_TICK.remove(player.getStringUUID());
            return;
        }

        if (!(Boolean) GameplayTweak.STAMINA_SPRINT.get()) return;

        if (!player.isSprinting()) return;

        StaminaData data = StaminaHelper.get(player);
        StaminaDataAccessor accessor = (StaminaDataAccessor) data;

        if (accessor.coraline$getIsExhausted()) return;

        String uuid = player.getStringUUID();
        boolean skip = SKIP_TICK.getOrDefault(uuid, false);
        SKIP_TICK.put(uuid, !skip);

        if (!skip) {
            int current = accessor.coraline$getTickTimer();
            int max = accessor.coraline$getDurationInTicks();
            accessor.coraline$setTickTimer(Math.min(current + 1, max));
        }
    }
}
