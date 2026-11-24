package net.zharok01.coralinesystems;

import com.mojang.logging.LogUtils;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.github.alexthe666.alexsmobs.entity.EntityElephant;
import net.zharok01.coralinesystems.block.CentrifugeBlockEntity;
import net.zharok01.coralinesystems.client.entity.skin.CoralineSkinConfig;
import net.zharok01.coralinesystems.mixin.accessor.PotionBrewingAccessor;
import net.zharok01.coralinesystems.registry.*;
import net.zharok01.coralinesystems.util.MinecartGameRules;
import net.zharok01.coralinesystems.util.block.BrewingCauldronInteractions;
import org.slf4j.Logger;

@Mod(CoralineSystems.MOD_ID)
public class CoralineSystems {
    public static final String MOD_ID = "coraline_systems";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation of(String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }

    @SuppressWarnings("removal")
    public CoralineSystems() {
        CoralineIntegrityChecker.verifyIntegrity();

        MinecartGameRules.bootstrap();

        @SuppressWarnings("unused")
        Rarity unusedRarityInitTrigger = CoralineRarity.SPECIAL;

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        CoralineBlocks.register(bus);
        CoralineItems.register(bus);
        CoralineRecipeSerializers.register(bus);
        IsotopicEntities.register(bus);
        CoralineSounds.register(bus);
        CoralineBlockEntities.register(bus);
        CoralineMenuTypes.register(bus);
        CoralineStats.register(bus);
        CoralineSerializers.register(bus);
        CoralineEffects.register(bus);
        CoralinePotions.register(bus);
        CoralineParticles.register(bus);
        CoralineFeatures.register(bus);
        CoralineTreeDecoratorTypes.register(bus);
        CoralineStructures.init(bus);
        CoralineFoliagePlacers.register(bus);
        CoralineTrunkPlacers.register(bus);

        CoralineSkinConfig.register();

        bus.addListener(this::commonSetup);
        bus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        CoralinePacketHandler.register();

        event.enqueueWork(() -> {
            CoralineTriggers.init();
            BrewingCauldronInteractions.bootstrap();
            CauldronInteraction.WATER.put(Items.WRITABLE_BOOK, CauldronInteraction.DYED_ITEM);
            CauldronInteraction.WATER.put(Items.WRITTEN_BOOK, CauldronInteraction.DYED_ITEM);
            CentrifugeBlockEntity.registerDispenserBehavior(CoralineItems.ORB.get());
            CoralineBlocks.registerFlammables();

            PotionBrewingAccessor.coraline$invokeAddMix(Potions.WATER, CoralineItems.DIMENSIONAL_SHARD.get(), CoralinePotions.CLARITY.get());
            PotionBrewingAccessor.coraline$invokeAddMix(CoralinePotions.CLARITY.get(), Items.REDSTONE, CoralinePotions.LONG_CLARITY.get());

            CoralineBlocks.CARPET.forEach((dyeColor, carpet) ->
                    EntityElephant.DYE_COLOR_ITEM_MAP.put(dyeColor, carpet.get().asItem()));

            CoralineStats.STATS.getEntries().forEach(stat ->
                    Stats.CUSTOM.get(stat.get(), StatFormatter.DEFAULT));
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.getEntries().putAfter(
                    Items.TORCH.getDefaultInstance(),
                    CoralineBlocks.TORCH.get().asItem().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
    }
}