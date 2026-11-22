package net.zharok01.coralinesystems.event.zipline;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ZiplineClientHelper {

    public static void playZiplineSound(Player player) {
        Minecraft.getInstance().getSoundManager().play(new ZiplineSoundInstance(player));
    }

    public static void coraline$setObserverZipline(UUID playerUuid, boolean ziplining) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        if (!ziplining) {
            ZiplineHandler.coraline$clientObserverZiplines.remove(playerUuid);
            return;
        }

        Player player = minecraft.level.getPlayerByUUID(playerUuid);
        if (player == null || player == minecraft.player) return;
        if (ZiplineHandler.coraline$clientObserverZiplines.add(playerUuid)) {
            minecraft.getSoundManager().play(new ZiplineSoundInstance(player, true));
        }
    }
}