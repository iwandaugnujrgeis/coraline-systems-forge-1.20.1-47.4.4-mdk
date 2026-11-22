package net.zharok01.coralinesystems.weather;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.network.FogWeatherPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FogWeatherManager {

    private static final float FOG_CHANCE = 0.3F;

    private static boolean wasRaining;

    public static FogWeatherData getData(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(FogWeatherData::load, FogWeatherData::new, FogWeatherData.DATA_NAME);
    }

    public static void startFog(ServerLevel overworld, int duration) {
        overworld.setWeatherParameters(0, duration, false, false);
        setFogging(getData(overworld), true);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        wasRaining = event.getServer().overworld().getLevelData().isRaining();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }

        ServerLevelData levelData = (ServerLevelData) level.getLevelData();
        FogWeatherData data = getData(level);
        boolean raining = levelData.isRaining();

        if (data.fogging) {
            if (raining && levelData.getRainTime() == 0) {
                levelData.setRaining(false);
                setFogging(data, false);
            } else if (levelData.getClearWeatherTime() > 0 || raining || levelData.getRainTime() == 0) {
                setFogging(data, false);
            }
        } else if (raining && !wasRaining && levelData.getRainTime() == 0 && !levelData.isThundering() && level.random.nextFloat() < FOG_CHANCE) {
            levelData.setRaining(false);
            levelData.setRainTime(ServerLevel.RAIN_DURATION.sample(level.random));
            setFogging(data, true);
        }
        wasRaining = levelData.isRaining();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        boolean fogging = getData(player.server.overworld()).fogging;
        CoralinePacketHandler.sendFogWeatherToPlayer(new FogWeatherPacket(fogging), player);
    }

    private static void setFogging(FogWeatherData data, boolean fogging) {
        data.fogging = fogging;
        data.setDirty();
        CoralinePacketHandler.sendFogWeatherToAll(new FogWeatherPacket(fogging));
    }
}