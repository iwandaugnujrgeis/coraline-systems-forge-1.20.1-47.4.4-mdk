package net.zharok01.coralinesystems.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.BrumeEntity;
import net.zharok01.coralinesystems.entity.HelperEntity;
import net.zharok01.coralinesystems.entity.MonsterEntity;
import net.zharok01.coralinesystems.entity.OrbEntity;

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

    public static final RegistryObject<EntityType<OrbEntity>> ORB =
            ENTITY_TYPES.register("orb", () -> EntityType.Builder.of(OrbEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.0F) // Roughly the size of a full block
                    .clientTrackingRange(8)
                    .build("orb"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}