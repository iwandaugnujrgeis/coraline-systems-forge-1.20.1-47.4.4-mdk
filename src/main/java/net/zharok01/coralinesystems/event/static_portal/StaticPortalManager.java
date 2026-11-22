package net.zharok01.coralinesystems.event.static_portal;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.block.StaticPortalBlock;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "coraline_systems", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StaticPortalManager {

    private static final Map<UUID, PortalState> SERVER_STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, PortalState> CLIENT_STATES = new ConcurrentHashMap<>();

    private static final int PORTAL_COOLDOWN_TICKS = 60;

    public static class PortalState {
        public boolean inPortalThisTick = false;
        public int ticksInPortal = 0;
        public boolean justTeleported = false;
    }

    private static PortalState getState(Player player) {
        Map<UUID, PortalState> states = player.level().isClientSide() ? CLIENT_STATES : SERVER_STATES;
        return states.computeIfAbsent(player.getUUID(), id -> new PortalState());
    }

    public static void setInPortal(Player player) {
        getState(player).inPortalThisTick = true;
    }

    public static int getTicksInPortal(Player player) {
        return getState(player).ticksInPortal;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (player.level().isClientSide() && !player.isLocalPlayer()) return;
        PortalState state = getState(player);

        if (state.inPortalThisTick) {
            if (!player.level().isClientSide() && player.isOnPortalCooldown()) {
                player.setPortalCooldown(PORTAL_COOLDOWN_TICKS);
            } else if (!state.justTeleported) {
                state.ticksInPortal++;

                if (state.ticksInPortal >= StaticPortalBlock.PORTAL_TICKS_REQUIRED) {
                    state.justTeleported = true;

                    if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
                        StaticPortalTeleporter.teleportToFarlands(player, serverLevel, player.blockPosition());
                        player.setPortalCooldown(PORTAL_COOLDOWN_TICKS);
                    }
                }
            }
        } else {
            state.justTeleported = false;
            if (state.ticksInPortal > 0) {
                state.ticksInPortal = Math.max(state.ticksInPortal - 4, 0);
            }
        }

        state.inPortalThisTick = false;
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        SERVER_STATES.remove(id);
        CLIENT_STATES.remove(id);
    }
}