package net.zharok01.coralinesystems.registry;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.gui.ContainerBlockMenu;
import net.zharok01.coralinesystems.quiver.QuiverInventoryMenu;

public class CoralineMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, CoralineSystems.MOD_ID);

    public static final RegistryObject<MenuType<ContainerBlockMenu>> CONTAINER_BLOCK = MENU_TYPES.register("container_block",
            () -> new MenuType<>(ContainerBlockMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistryObject<MenuType<QuiverInventoryMenu>> QUIVER_INVENTORY = MENU_TYPES.register("quiver_inventory",
            () -> new MenuType<>((containerId, inventory) -> new QuiverInventoryMenu(containerId, inventory.player), FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
    }
}