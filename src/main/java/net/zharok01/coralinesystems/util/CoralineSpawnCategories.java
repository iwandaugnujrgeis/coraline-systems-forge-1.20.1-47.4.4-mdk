package net.zharok01.coralinesystems.util;

import net.minecraft.world.entity.EntityType;

public final class CoralineSpawnCategories {

    public static final String MOOSHROOM_ID = "minecraft:mooshroom";

    private CoralineSpawnCategories() {
    }

    public static boolean coraline$isMooshroom(EntityType<?> entityType) {
        return entityType == EntityType.MOOSHROOM;
    }
}
