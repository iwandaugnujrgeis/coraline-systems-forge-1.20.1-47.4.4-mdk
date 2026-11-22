package net.zharok01.coralinesystems.event;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PaintingTooltipHandler {

    private static final String RANDOM_VARIANT_KEY = "painting.random";

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(Items.PAINTING)) return;

        event.getToolTip().removeIf(line ->
                line.getContents() instanceof TranslatableContents translatable
                        && translatable.getKey().equals(RANDOM_VARIANT_KEY));
    }
}