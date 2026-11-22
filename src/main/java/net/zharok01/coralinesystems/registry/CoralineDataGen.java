package net.zharok01.coralinesystems.registry;

import net.minecraft.DetectedVersion;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoralineDataGen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        boolean includeClient = event.includeClient();

        PackOutput corePackOutput = generator.getPackOutput("assets/" + CoralineSystems.MOD_ID + "/core_pack");
        generator.addProvider(includeClient, createPackMetadata(corePackOutput, "Core assets and default resources for Coraline Systems"));

        PackOutput legacyPackOutput = generator.getPackOutput("assets/" + CoralineSystems.MOD_ID + "/legacy_pack");
        generator.addProvider(includeClient, createPackMetadata(legacyPackOutput, "Original look for Coraline Systems"));
    }

    private static PackMetadataGenerator createPackMetadata(PackOutput output, String description) {
        Map<PackType, Integer> packVersionMap = Arrays.stream(PackType.values())
                .collect(Collectors.toMap(Function.identity(), DetectedVersion.BUILT_IN::getPackVersion));

        int packVersion = packVersionMap.get(PackType.CLIENT_RESOURCES);
        return new PackMetadataGenerator(output)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(Component.literal(description), packVersion, packVersionMap));
    }
}