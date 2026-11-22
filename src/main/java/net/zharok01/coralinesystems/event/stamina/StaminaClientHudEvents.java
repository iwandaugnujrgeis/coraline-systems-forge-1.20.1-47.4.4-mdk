package net.zharok01.coralinesystems.event.stamina;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineItems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public class StaminaClientHudEvents {

    public static long drinkFlashEndTime = 0;

    public static void triggerDrinkFlash() {
        drinkFlashEndTime = Util.getMillis() + 600;
    }

    @SubscribeEvent
    public static void onItemFinishUse(LivingEntityUseItemEvent.Finish event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player player)) return;
        if (player != mc.player) return;

        ItemStack stack = event.getItem();

        boolean isTea = stack.is(CoralineItems.TEA_BOTTLE.get());
        boolean isMulberryJuice = stack.is(CoralineItems.MULBERRY_JUICE_BOTTLE.get());

        if (isTea || isMulberryJuice) {
            triggerDrinkFlash();
        }
    }
}