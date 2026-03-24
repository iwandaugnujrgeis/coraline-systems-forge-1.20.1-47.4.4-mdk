package net.zharok01.coralinesystems.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;
import net.zharok01.coralinesystems.content.entity.custom.MonsterEntity;

public class IsotopicEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CoralineSystems.MOD_ID);

    public static final RegistryObject<EntityType<HelperEntity>> HELPER =
            ENTITY_TYPES.register("helper", () -> EntityType.Builder.of(HelperEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F) // Standard human size
                    .build("helper"));

    public static final RegistryObject<EntityType<MonsterEntity>> MONSTER =
            ENTITY_TYPES.register("monster", () -> EntityType.Builder.of(MonsterEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F) // Classic humanoid hitboxes
                    .build("monster"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}