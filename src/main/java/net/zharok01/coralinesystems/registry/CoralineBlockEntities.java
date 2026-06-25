package net.zharok01.coralinesystems.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.block.BasketBlockEntity;
import net.zharok01.coralinesystems.block.VibrationSensorBlockEntity;

public class CoralineBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CoralineSystems.MOD_ID);

    public static final RegistryObject<BlockEntityType<net.zharok01.coralinesystems.block.ContainerBlockEntity>> CONTAINER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("container_block_entity", () ->
                    BlockEntityType.Builder.of(net.zharok01.coralinesystems.block.ContainerBlockEntity::new,
                            CoralineBlocks.CONTAINER_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<VibrationSensorBlockEntity>> VIBRATION_SENSOR =
            BLOCK_ENTITIES.register("vibration_sensor", () ->
                    BlockEntityType.Builder.of(VibrationSensorBlockEntity::new,
                            CoralineBlocks.VIBRATION_SENSOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<BasketBlockEntity>> BASKET =
            BLOCK_ENTITIES.register("basket", () ->
                    BlockEntityType.Builder.of(
                            BasketBlockEntity::new,
                            CoralineBlocks.BASKET.get()   // the single block this BE is valid for
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}