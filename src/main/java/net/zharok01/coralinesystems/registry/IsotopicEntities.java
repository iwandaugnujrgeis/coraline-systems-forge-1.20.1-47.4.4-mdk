package net.zharok01.coralinesystems.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.*;

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

    public static final RegistryObject<EntityType<OrbPulseEntity>> ORB_PULSE =
            ENTITY_TYPES.register("orb_pulse", () -> EntityType.Builder
                    .<OrbPulseEntity>of(OrbPulseEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)   // matches SmallFireball's hitbox
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("orb_pulse"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}