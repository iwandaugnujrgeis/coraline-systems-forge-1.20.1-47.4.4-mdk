package net.zharok01.coralinesystems.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.world.structure.ForgottenStructure;

public class CoralineStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, CoralineSystems.MOD_ID);

    // FIX: Removed ".json" from the registry name
    public static final RegistryObject<StructureType<ForgottenStructure>> FORGOTTEN =
            STRUCTURE_TYPES.register("forgotten_dungeon", () -> () -> ForgottenStructure.CODEC);

    public static void register(IEventBus bus) {
        STRUCTURE_TYPES.register(bus);
    }
}