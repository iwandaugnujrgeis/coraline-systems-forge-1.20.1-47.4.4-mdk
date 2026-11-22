package net.zharok01.coralinesystems.weather;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.network.SunshowerPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SunshowerManager {

    private static final float SUNSHOWER_CHANCE = 0.3F;
    private static final IntProvider SUNSHOWER_DURATION = UniformInt.of(1800, 4800);

    private static boolean wasRaining;
    private static boolean sunshowerActive;
    private static int rainTicksCut;

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        wasRaining = event.getServer().overworld().getLevelData().isRaining();
        sunshowerActive = false;
        rainTicksCut = 0;
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }

        ServerLevelData levelData = (ServerLevelData) level.getLevelData();
        boolean raining = levelData.isRaining();

        if (raining && !wasRaining && level.isDay() && !levelData.isThundering() && level.random.nextFloat() < SUNSHOWER_CHANCE) {
            int vanillaDuration = levelData.getRainTime() > 0 ? levelData.getRainTime() : ServerLevel.RAIN_DURATION.sample(level.random);
            int duration = Math.min(vanillaDuration, SUNSHOWER_DURATION.sample(level.random));
            rainTicksCut = vanillaDuration - duration;
            levelData.setRainTime(duration);
            broadcastSunshower(true);
        }

        if (wasRaining && !raining && rainTicksCut > 0) {
            if (!FogWeatherManager.getData(level).fogging) {
                levelData.setRainTime(rainTicksCut + ServerLevel.RAIN_DELAY.sample(level.random));
            }
            rainTicksCut = 0;
        }
        wasRaining = raining;

        if (sunshowerActive && (!raining || levelData.isThundering())) {
            broadcastSunshower(false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        CoralinePacketHandler.sendSunshowerToPlayer(new SunshowerPacket(sunshowerActive), (ServerPlayer) event.getEntity());
    }

    private static void broadcastSunshower(boolean active) {
        sunshowerActive = active;
        CoralinePacketHandler.sendSunshowerToAll(new SunshowerPacket(active));
    }
}