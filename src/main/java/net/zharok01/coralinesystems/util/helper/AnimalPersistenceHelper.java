package net.zharok01.coralinesystems.util.helper;

import mod.adrenix.nostalgic.helper.gameplay.AnimalSpawnHelper;
import mod.adrenix.nostalgic.tweak.config.GameplayTweak;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.fml.ModList;

public class AnimalPersistenceHelper {

    private static final boolean NOSTALGIC_TWEAKS_LOADED = ModList.get().isLoaded("nostalgic_tweaks");

    public static boolean coraline$shouldPersist(Mob mob) {
        return NOSTALGIC_TWEAKS_LOADED
                && !mob.isPersistenceRequired()
                && GameplayTweak.OLD_ANIMAL_SPAWNING.get()
                && AnimalSpawnHelper.isInList(mob.getType());
    }
}