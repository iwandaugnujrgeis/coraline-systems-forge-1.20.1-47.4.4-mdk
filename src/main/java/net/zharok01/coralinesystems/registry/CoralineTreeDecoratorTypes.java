package net.zharok01.coralinesystems.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.worldgen.feature.HangingFruitDecorator;
import net.zharok01.coralinesystems.worldgen.feature.LeafLichenDecorator;

public class CoralineTreeDecoratorTypes {

    public static final DeferredRegister<TreeDecoratorType<?>> REGISTRY =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, CoralineSystems.MOD_ID);

    public static final RegistryObject<TreeDecoratorType<HangingFruitDecorator>> HANGING_FRUIT =
            REGISTRY.register("hanging_fruit",
                    () -> new TreeDecoratorType<>(HangingFruitDecorator.CODEC));

    public static final RegistryObject<TreeDecoratorType<LeafLichenDecorator>> LEAF_LICHEN =
            REGISTRY.register("leaf_lichen",
                    () -> new TreeDecoratorType<>(LeafLichenDecorator.CODEC));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}