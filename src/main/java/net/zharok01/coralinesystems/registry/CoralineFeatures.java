package net.zharok01.coralinesystems.registry;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.worldgen.feature.*;
import net.zharok01.coralinesystems.worldgen.feature.config.*;

@SuppressWarnings("unused")
public class CoralineFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =

            DeferredRegister.create(ForgeRegistries.FEATURES, CoralineSystems.MOD_ID);
    public static final RegistryObject<Feature<FallenTreeConfiguration>> FALLEN_TREE =

            FEATURES.register("fallen_tree", () -> new FallenTreeFeature(FallenTreeConfiguration.CODEC));
    public static final RegistryObject<Feature<MixedMushroomConfiguration>> MIXED_MUSHROOM =

            FEATURES.register("mixed_mushroom", () -> new MixedMushroomFeature(MixedMushroomConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> BLOODSTONE_VEIN =

            FEATURES.register("bloodstone_vein", () -> new BloodstoneVeinFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<ShoreLilypadConfiguration>> SHORE_LILYPAD =

            FEATURES.register("shore_lilypad", () -> new ShoreLilypadFeature(ShoreLilypadConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CEMENT_HARDENING =

            FEATURES.register("cement_hardening", () -> new CementHardeningFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> MAGMA_CONVERSION =

            FEATURES.register("magma_conversion", () -> new MagmaConversionFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> EXCAVATION_SITE =

            FEATURES.register("excavation_site", () -> new ExcavationSiteFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> LABYRINTH =

            FEATURES.register("labyrinth", () -> new LabyrinthFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<RockSaltPatchConfiguration>> ROCK_SALT_PATCH =

            FEATURES.register("rock_salt_patch", () -> new RockSaltPatchFeature(RockSaltPatchConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> PHANTOM_TUNNEL =

            FEATURES.register("phantom_tunnel", () -> new PhantomTunnelFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
