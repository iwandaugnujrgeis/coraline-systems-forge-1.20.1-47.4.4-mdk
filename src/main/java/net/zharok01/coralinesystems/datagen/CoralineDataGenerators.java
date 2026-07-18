package net.zharok01.coralinesystems.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

/**
 * First datagen entry point for Coraline Systems (per Session 3 --
 * previously nonexistent; see cauldron_brewing_coding_roadmap_handoff.md
 * Section 1.6, "Datagen for items ... proposed but not yet added to the
 * project"). Currently wires up only {@link ModItemModelProvider}, the
 * flat item-model generator for the 10 drink items -- see that class'
 * javadoc for why hand-writing 10 nearly-identical two-layer JSON files
 * was worth avoiding.
 * <p>
 * Registered on the MOD event bus (GatherDataEvent is an IModBusEvent,
 * same bus category as FMLCommonSetupEvent/FMLClientSetupEvent -- see
 * CoralineSystems' own bus.addListener(...) calls in its constructor for
 * the existing precedent) via a plain static @SubscribeEvent, mirroring
 * this mod's existing @Mod.EventBusSubscriber pattern used by
 * CoralineSystems.ClientModEvents rather than an instance listener.
 */
@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CoralineDataGenerators {

    private CoralineDataGenerators() {
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        if (event.includeClient()) {
            generator.addProvider(true, new ModItemModelProvider(packOutput, event.getExistingFileHelper()));
        }
    }
}