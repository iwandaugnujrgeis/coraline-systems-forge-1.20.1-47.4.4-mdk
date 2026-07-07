package net.zharok01.coralinesystems.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CoralineRegistryRemapper {

    private static final ResourceLocation RADIANT_LEAVES_ID =
            new ResourceLocation(CoralineSystems.MOD_ID, "radiant_leaves");

    private static final ResourceLocation KUBEJS_SILVER_ORE_ID =
            new ResourceLocation("kubejs", "silver_ore");

    private static final ResourceLocation KUBEJS_SILVER_INGOT_ID =
            new ResourceLocation("kubejs", "silver_ingot");

    private static final ResourceLocation KUBEJS_SILVER_NUGGET_ID =
            new ResourceLocation("kubejs", "silver_nugget");

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        remapBlocks(event);
        remapItems(event);
    }

    private static void remapBlocks(MissingMappingsEvent event) {

        // Block Remapper ---------------------------------------

        for (MissingMappingsEvent.Mapping<Block> mapping :
                event.getMappings(ForgeRegistries.BLOCKS.getRegistryKey(), "autumnity")) {
            if (mapping.getKey().getPath().equals("maple_leaves")) {
                Block replacement = ForgeRegistries.BLOCKS.getValue(RADIANT_LEAVES_ID);
                if (replacement != null) {
                    mapping.remap(replacement);
                }
            }
        }

        for (MissingMappingsEvent.Mapping<Block> mapping :
                event.getMappings(ForgeRegistries.BLOCKS.getRegistryKey(), "galosphere")) {
            if (mapping.getKey().getPath().equals("silver_ore")) {
                Block replacement = ForgeRegistries.BLOCKS.getValue(KUBEJS_SILVER_ORE_ID);
                if (replacement != null) {
                    mapping.remap(replacement);
                }
            }
        }
    }

    private static void remapItems(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Item> mapping :
                event.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), "autumnity")) {
            if (mapping.getKey().getPath().equals("maple_leaves")) {
                Item replacement = ForgeRegistries.ITEMS.getValue(RADIANT_LEAVES_ID);
                if (replacement != null) {
                    mapping.remap(replacement);
                }
            }
        }

        for (MissingMappingsEvent.Mapping<Item> mapping :
                event.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), "galosphere")) {
            if (mapping.getKey().getPath().equals("silver_ore")) {
                Item replacement = ForgeRegistries.ITEMS.getValue(KUBEJS_SILVER_ORE_ID);
                if (replacement != null) {
                    mapping.remap(replacement);
                }
            }
        }

        for (MissingMappingsEvent.Mapping<Item> mapping :
                event.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), "galosphere")) {
            if (mapping.getKey().getPath().equals("silver_ingot")) {
                Item replacement = ForgeRegistries.ITEMS.getValue(KUBEJS_SILVER_INGOT_ID);
                if (replacement != null) {
                    mapping.remap(replacement);
                }
            }
        }

        for (MissingMappingsEvent.Mapping<Item> mapping :
                event.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), "galosphere")) {
            if (mapping.getKey().getPath().equals("silver_nugget")) {
                Item replacement = ForgeRegistries.ITEMS.getValue(KUBEJS_SILVER_NUGGET_ID);
                if (replacement != null) {
                    mapping.remap(replacement);
                }
            }
        }
    }
}