package net.zharok01.coralinesystems.mixin.client.gui.screens;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreativeModeTabRegistry.class, remap = false)
public abstract class CreativeModeTabRegistryMixin {
    @Shadow
    @Final
    private static List<CreativeModeTab> DEFAULT_TABS;

    @Inject(method = "sortTabs", at = @At("RETURN"))
    private static void coraline$putOperatorTabInHotbarSlot(CallbackInfo ci) {
        Collections.swap(DEFAULT_TABS, 0, 2);
    }
}