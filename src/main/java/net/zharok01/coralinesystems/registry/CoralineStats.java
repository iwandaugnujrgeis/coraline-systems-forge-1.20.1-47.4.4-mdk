package net.zharok01.coralinesystems.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;

public class CoralineStats {
    public static final DeferredRegister<ResourceLocation> STATS =
            DeferredRegister.create(Registries.CUSTOM_STAT, CoralineSystems.MOD_ID);

    public static final RegistryObject<ResourceLocation> PORTALS_CORRUPTED = register("portals_corrupted");
    public static final RegistryObject<ResourceLocation> MONSTER_SIGHTINGS = register("monster_sightings");

    public static void register(IEventBus eventBus) {
        STATS.register(eventBus);
    }

    private static RegistryObject<ResourceLocation> register(String id) {
        return STATS.register(id, () -> CoralineSystems.of(id));
    }
}