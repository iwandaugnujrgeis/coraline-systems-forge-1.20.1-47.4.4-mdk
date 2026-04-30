package net.zharok01.coralinesystems.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.block.StaticBlockEntity;
import net.zharok01.coralinesystems.content.block.VibrationSensorBlockEntity;

public class CoralineBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CoralineSystems.MOD_ID);

    public static final RegistryObject<BlockEntityType<StaticBlockEntity>> STATIC_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("static_block_entity", () ->
                    BlockEntityType.Builder.of(StaticBlockEntity::new,
                            CoralineBlocks.STATIC_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<net.zharok01.coralinesystems.content.block.ContainerBlockEntity>> CONTAINER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("container_block_entity", () ->
                    BlockEntityType.Builder.of(net.zharok01.coralinesystems.content.block.ContainerBlockEntity::new,
                            CoralineBlocks.CONTAINER_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<VibrationSensorBlockEntity>> VIBRATION_SENSOR =
            BLOCK_ENTITIES.register("vibration_sensor", () ->
                    BlockEntityType.Builder.of(VibrationSensorBlockEntity::new,
                            CoralineBlocks.VIBRATION_SENSOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}