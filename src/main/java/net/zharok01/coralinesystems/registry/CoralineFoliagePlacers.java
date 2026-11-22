package net.zharok01.coralinesystems.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.worldgen.tree.GloomyFoliagePlacer;
import net.zharok01.coralinesystems.worldgen.tree.MegaOakFoliagePlacer;
import net.zharok01.coralinesystems.worldgen.tree.RedwoodFoliagePlacer;

public class CoralineFoliagePlacers {
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPES =
            DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, CoralineSystems.MOD_ID);

    public static final RegistryObject<FoliagePlacerType<RedwoodFoliagePlacer>> REDWOOD_FOLIAGE_PLACER =
            FOLIAGE_PLACER_TYPES.register("redwood_foliage_placer", () -> new FoliagePlacerType<>(RedwoodFoliagePlacer.CODEC));

    public static final RegistryObject<FoliagePlacerType<MegaOakFoliagePlacer>> MEGA_OAK_FOLIAGE_PLACER =
            FOLIAGE_PLACER_TYPES.register("mega_oak_foliage_placer", () -> new FoliagePlacerType<>(MegaOakFoliagePlacer.CODEC));

    public static final RegistryObject<FoliagePlacerType<GloomyFoliagePlacer>> GLOOMY_FOLIAGE_PLACER =
            FOLIAGE_PLACER_TYPES.register("gloomy_foliage_placer", () -> new FoliagePlacerType<>(GloomyFoliagePlacer.CODEC));

    public static void register(IEventBus eventBus) {
        FOLIAGE_PLACER_TYPES.register(eventBus);
    }
}