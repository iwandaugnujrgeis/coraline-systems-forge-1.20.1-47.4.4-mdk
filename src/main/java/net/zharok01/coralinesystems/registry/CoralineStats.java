package net.zharok01.coralinesystems.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoralineStats {
    public static final DeferredRegister<ResourceLocation> STATS = DeferredRegister.create(Registries.CUSTOM_STAT, CoralineSystems.MOD_ID);

    public static void register(IEventBus eventBus) {
        STATS.register(eventBus);
    }

    public static final RegistryObject<ResourceLocation> PORTALS_CORRUPTED = register("portals_corrupted", StatFormatter.DEFAULT);
    public static final RegistryObject<ResourceLocation> MONSTER_SIGHTINGS = register("monster_sightings", StatFormatter.DEFAULT);

    private static RegistryObject<ResourceLocation> register(String id, StatFormatter formatter) {
        ResourceLocation identifier = new ResourceLocation(CoralineSystems.MOD_ID, id);
        return STATS.register(id, () -> identifier);
    }
}