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

    // ── CORALINE: Absolute Time Drink-flash state ─────────────────────────────

    /**
     * Stores the exact millisecond timestamp when the flash should stop.
     */
    public static long drinkFlashEndTime = 0;

    /**
     * Called when the drink items finish their use animation.
     * Schedules 600ms of flashing (two 150ms ON / 150ms OFF cycles).
     */
    public static void triggerDrinkFlash() {
        drinkFlashEndTime = Util.getMillis() + 600;
    }

    // ── Event Listener ────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onItemFinishUse(LivingEntityUseItemEvent.Finish event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player player)) return;
        if (player != mc.player) return;

        ItemStack stack = event.getItem();

        boolean isTea = stack.is(CoralineItems.TEA_BOTTLE.get())
                || stack.is(CoralineItems.TEA_BUCKET.get());
        boolean isMulberryJuice = stack.is(CoralineItems.MULBERRY_JUICE_BOTTLE.get())
                || stack.is(CoralineItems.MULBERRY_JUICE_BUCKET.get());

        if (isTea || isMulberryJuice) {
            triggerDrinkFlash();
        }
    }
}