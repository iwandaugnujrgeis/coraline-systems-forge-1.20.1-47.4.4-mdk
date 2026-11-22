package net.zharok01.coralinesystems.client.entity.skin;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.network.ServerboundSelectSkinPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientSkinEvents {

    private ClientSkinEvents() {}

    @SubscribeEvent
    public static void onLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        SkinVariant saved = CoralineSkinConfig.getSelectedVariant();

        if (saved != SkinVariant.DEFAULT) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                ClientSkinOverrideCache.put(mc.player.getUUID(), saved);
            }
        }

        CoralinePacketHandler.sendToServer(new ServerboundSelectSkinPacket(saved));
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            SkinVariant ours = ClientSkinOverrideCache.get(mc.player.getUUID());
            ClientSkinOverrideCache.clear();
            if (ours != null) {
                ClientSkinOverrideCache.put(mc.player.getUUID(), ours);
            }
        } else {
            ClientSkinOverrideCache.clear();
        }
    }
}