package net.zharok01.coralinesystems.client.entity.skin;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public final class ClientSkinOverrideCache {

    private static final Map<UUID, SkinVariant> CACHE = new ConcurrentHashMap<>();

    private ClientSkinOverrideCache() {}

    public static void put(UUID playerUuid, SkinVariant variant) {
        CACHE.put(playerUuid, variant);
    }

    public static SkinVariant get(UUID playerUuid) {
        return CACHE.get(playerUuid);
    }

    public static void clear() {
        CACHE.clear();
    }
}
