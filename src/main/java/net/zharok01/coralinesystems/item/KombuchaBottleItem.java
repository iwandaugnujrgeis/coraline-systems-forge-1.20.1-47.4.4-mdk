package net.zharok01.coralinesystems.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Bottle-form Kombucha. Single-strength — no strength tooltip.
 * <p>
 * Drinking sets the player's respawn point at the exact block position
 * where they stood when they drank it. Uses {@code forced = true} so
 * the spawn works in any dimension without needing a bed or anchor, and
 * {@code sendMessage = true} so the vanilla "Respawn point set" toast
 * appears automatically.
 */
public class KombuchaBottleItem extends AbstractCoralineDrinkItem {

    public KombuchaBottleItem(Properties properties) {
        super(properties, Items.GLASS_BOTTLE);
    }

    @Override
    protected void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof ServerPlayer serverPlayer)) return;

        serverPlayer.setRespawnPosition(
                level.dimension(),          // dimension the player is currently in
                serverPlayer.blockPosition(), // exact block the player is standing on
                serverPlayer.getYRot(),       // preserve facing angle
                true,                         // forced — works without a bed/anchor
                false                          // send the vanilla "Respawn point set" toast
        );
    }
}