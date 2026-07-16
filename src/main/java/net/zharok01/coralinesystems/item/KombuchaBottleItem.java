package net.zharok01.coralinesystems.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Bottle-form Kombucha. Same shape as {@link WineBottleItem} minus the
 * strength tooltip — Kombucha is single-strength per the design doc
 * (design doc Section 4), so {@link CoralineFluidUtils} is never invoked
 * for this item.
 * <p>
 * The respawn-point-setting drink effect (design doc Section 4,
 * "Collection") is explicitly out of scope for Session 1.6 — see
 * {@link #finishUsingItem}'s TODO. This is Session 4's job.
 */
public class KombuchaBottleItem extends Item {
    private static final int DRINK_DURATION = 32;

    public KombuchaBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;

        // TODO (Session 4): set the player's respawn point at their current
        // position on drink, per design doc Section 4 ("Effect: sets the
        // player's respawn point at the location where the Kombucha was
        // drunk"). Deliberately not guessed here — fallback-on-obstruction
        // behavior explicitly inherits vanilla bed/anchor logic wholesale
        // per the design doc, but the actual respawn-set call site belongs
        // in Session 4 alongside the rest of drink-effect dispatch.

        if (player != null) {
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild) {
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }

            if (player != null) {
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        livingEntity.gameEvent(net.minecraft.world.level.gameevent.GameEvent.DRINK);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return DRINK_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return ItemUtils.startUsingInstantly(level, player, usedHand);
    }
}
