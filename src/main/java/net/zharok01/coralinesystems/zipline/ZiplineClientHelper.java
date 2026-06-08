package net.zharok01.coralinesystems.zipline;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "coraline_systems", value = Dist.CLIENT)
public class ZiplineClientHelper {

    public static void playZiplineSound(Player player) {
        Minecraft.getInstance().getSoundManager().play(new ZiplineSoundInstance(player));
    }
}