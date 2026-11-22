package net.zharok01.coralinesystems.util;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public final class CoralineWorldInfo {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = "coraline_world.properties";
    private static final String WORLD_TYPE_KEY = "world_type";

    public static ResourceLocation coraline$pendingWorldType;

    private CoralineWorldInfo() {
    }

    @SubscribeEvent
    public static void coraline$writeWorldInfo(ServerStartingEvent event) {
        ResourceLocation worldType = coraline$pendingWorldType;
        coraline$pendingWorldType = null;

        MinecraftServer server = event.getServer();
        if (worldType == null || server.getWorldData().overworldData().getGameTime() != 0L) {
            return;
        }

        Properties properties = new Properties();
        properties.setProperty(WORLD_TYPE_KEY, worldType.toString());
        try (Writer writer = Files.newBufferedWriter(server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME))) {
            properties.store(writer, "Coraline Systems world info");
        } catch (IOException exception) {
            LOGGER.error("Failed to write {}", FILE_NAME, exception);
        }
    }

    public static ResourceLocation coraline$readWorldType(Path worldDir) {
        Path file = worldDir.resolve(FILE_NAME);
        if (!Files.isRegularFile(file)) {
            return null;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        } catch (IOException exception) {
            LOGGER.error("Failed to read {}", file, exception);
            return null;
        }

        String worldType = properties.getProperty(WORLD_TYPE_KEY);
        return worldType == null ? null : ResourceLocation.tryParse(worldType);
    }
}