package net.zharok01.coralinesystems.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;
import net.zharok01.coralinesystems.CoralineSystems;

import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoralineResourcePackHandler {

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            registerBuiltinPack(event, Component.translatable("coraline_systems.resource_pack.core_assets"), "core_pack", true);
            registerBuiltinPack(event, Component.translatable("coraline_systems.resource_pack.legacy_look"), "legacy_pack", false);
        }
    }

    private static void registerBuiltinPack(AddPackFindersEvent event, Component displayName, String folderName, boolean required) {
        event.addRepositorySource(consumer -> {
            IModFile modFile = ModList.get().getModFileById(CoralineSystems.MOD_ID).getFile();
            Path packPath = modFile.findResource("assets", CoralineSystems.MOD_ID, folderName);
            PathPackResources packResources = new PathPackResources(CoralineSystems.MOD_ID + ":" + folderName, true, packPath);

            Pack pack = Pack.readMetaAndCreate(
                    CoralineSystems.MOD_ID + ":" + folderName,
                    displayName,
                    required,
                    id -> packResources,
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    PackSource.BUILT_IN
            );

            if (pack != null) {
                consumer.accept(pack);
            }
        });
    }
}