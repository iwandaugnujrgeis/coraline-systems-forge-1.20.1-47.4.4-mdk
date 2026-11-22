package net.zharok01.coralinesystems.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.worldgen.tree.MegaOakTrunkPlacer;
import net.zharok01.coralinesystems.worldgen.tree.RedwoodTrunkPlacer;
import net.zharok01.coralinesystems.worldgen.tree.TwistedTrunkPlacer;

public class CoralineTrunkPlacers {

    public static final DeferredRegister<TrunkPlacerType<?>> REGISTRY =
            DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, CoralineSystems.MOD_ID);

    public static final RegistryObject<TrunkPlacerType<MegaOakTrunkPlacer>> MEGA_OAK =
            REGISTRY.register("mega_oak_trunk_placer",
                    () -> new TrunkPlacerType<>(MegaOakTrunkPlacer.CODEC));

    public static final RegistryObject<TrunkPlacerType<TwistedTrunkPlacer>> TWISTED =
            REGISTRY.register("twisted_trunk_placer",
                    () -> new TrunkPlacerType<>(TwistedTrunkPlacer.CODEC));

    public static final RegistryObject<TrunkPlacerType<RedwoodTrunkPlacer>> REDWOOD =
            REGISTRY.register("redwood_trunk_placer",
                    () -> new TrunkPlacerType<>(RedwoodTrunkPlacer.CODEC));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}