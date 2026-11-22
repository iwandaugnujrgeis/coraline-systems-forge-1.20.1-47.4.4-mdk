package net.zharok01.coralinesystems.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.*;
import net.zharok01.coralinesystems.entity.projectile.*;

public class IsotopicEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CoralineSystems.MOD_ID);

    public static final RegistryObject<EntityType<HelperEntity>> HELPER =
            ENTITY_TYPES.register("helper", () -> EntityType.Builder.of(HelperEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build("helper"));

    public static final RegistryObject<EntityType<MonsterEntity>> MONSTER =
            ENTITY_TYPES.register("monster", () -> EntityType.Builder.of(MonsterEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build("monster"));

    public static final RegistryObject<EntityType<BrumeEntity>> BRUME =
            ENTITY_TYPES.register("brume", () -> EntityType.Builder.of(BrumeEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build("brume"));

    public static final RegistryObject<EntityType<CryptidEntity>> CRYPTID =
            ENTITY_TYPES.register("cryptid", () -> EntityType.Builder.of(CryptidEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.1F)
                    .clientTrackingRange(10)
                    .build("cryptid"));

    public static final RegistryObject<EntityType<OrbEntity>> ORB =
            ENTITY_TYPES.register("orb", () -> EntityType.Builder.of(OrbEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(8)
                    .build("orb"));

    public static final RegistryObject<EntityType<StarEntity>> STAR =
            ENTITY_TYPES.register("star", () -> EntityType.Builder.of(StarEntity::new, MobCategory.AMBIENT)
                    .sized(0.8F, 0.8F)
                    .clientTrackingRange(8)
                    .build("star"));

    public static final RegistryObject<EntityType<OrbPulseEntity>> ORB_PULSE =
            ENTITY_TYPES.register("orb_pulse", () -> EntityType.Builder.<OrbPulseEntity>of(OrbPulseEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("orb_pulse"));

    public static final RegistryObject<EntityType<ThrownPlatypusEgg>> THROWN_PLATYPUS_EGG =
            thrownItem("thrown_platypus_egg", ThrownPlatypusEgg::new);

    public static final RegistryObject<EntityType<ThrownCaimanEgg>> THROWN_CAIMAN_EGG =
            thrownItem("thrown_caiman_egg", ThrownCaimanEgg::new);

    public static final RegistryObject<EntityType<ThrownTurtleEgg>> THROWN_TURTLE_EGG =
            thrownItem("thrown_turtle_egg", ThrownTurtleEgg::new);

    public static final RegistryObject<EntityType<ThrownDimensionalShard>> THROWN_DIMENSIONAL_SHARD =
            thrownItem("thrown_dimensional_shard", ThrownDimensionalShard::new);

    public static final RegistryObject<EntityType<ThrownStick>> THROWN_STICK =
            thrownItem("thrown_stick", ThrownStick::new);

    public static final RegistryObject<EntityType<ObsidianBoatEntity>> OBSIDIAN_BOAT =
            ENTITY_TYPES.register("obsidian_boat", () -> EntityType.Builder.<ObsidianBoatEntity>of(ObsidianBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build("obsidian_boat"));

    private static <T extends Entity> RegistryObject<EntityType<T>> thrownItem(String id, EntityType.EntityFactory<T> factory) {
        return ENTITY_TYPES.register(id, () -> EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build(id));
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}