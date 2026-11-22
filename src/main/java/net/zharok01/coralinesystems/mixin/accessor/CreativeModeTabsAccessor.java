package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTabs.class)
public interface CreativeModeTabsAccessor {

    @Accessor("HOTBAR")
    static ResourceKey<CreativeModeTab> coraline$getHotbarTab() {
        throw new AssertionError();
    }

    @Accessor("SEARCH")
    static ResourceKey<CreativeModeTab> coraline$getSearchTab() {
        throw new AssertionError();
    }

    @Accessor("INVENTORY")
    static ResourceKey<CreativeModeTab> coraline$getInventoryTab() {
        throw new AssertionError();
    }

    @Accessor("OP_BLOCKS")
    static ResourceKey<CreativeModeTab> coraline$getOpBlocksTab() {
        throw new AssertionError();
    }

    @Accessor("CACHED_PARAMETERS")
    @org.spongepowered.asm.mixin.Mutable
    static void coraline$setCachedParameters(CreativeModeTab.ItemDisplayParameters params) {
        throw new AssertionError();
    }
}
