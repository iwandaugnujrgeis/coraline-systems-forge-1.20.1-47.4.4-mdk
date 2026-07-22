package net.zharok01.coralinesystems.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineItems;

/**
 * Client-side FORGE bus listener that triggers the Stamina bar drink-flash
 * when the local player finishes drinking Tea or Mulberry Juice.
 */
@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public class CoralineClientHudEvents {

    // ── CORALINE: Drink-flash state ───────────────────────────────────────────

    /**
     * Set to 4 (two blinks = 4 half-cycles at 150 ms each) when Tea or
     * Mulberry Juice is drunk.
     */
    public static int drinkFlashRemaining = 0;

    /**
     * Called when the drink items finish their use animation.
     */
    public static void triggerDrinkFlash() {
        drinkFlashRemaining = 4; // 4 half-cycles = 2 full blinks
    }

    // ── Event Listener ────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onItemFinishUse(LivingEntityUseItemEvent.Finish event) {
        // Only act for the local player.
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