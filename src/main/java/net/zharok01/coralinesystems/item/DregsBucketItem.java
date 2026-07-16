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
 * Bucket-form Dregs. Same shape as {@link WineBucketItem} minus the
 * strength tooltip (single-strength, no {@link CoralineFluidUtils} usage).
 * Drinkability is unresolved — see {@link DregsBottleItem}'s javadoc for
 * the same open question. No {@code initCapabilities} — see
 * {@link WineBucketItem}'s javadoc for rationale.
 */
public class DregsBucketItem extends Item {
    private static final int DRINK_DURATION = 32;

    public DregsBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;

        // TODO (Session 4): decide + implement Dregs' drink effect (or lack
        // thereof) — see DregsBottleItem's javadoc.

        if (player != null) {
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild) {
            if (stack.isEmpty()) {
                return new ItemStack(Items.BUCKET);
            }

            if (player != null) {
                player.getInventory().add(new ItemStack(Items.BUCKET));
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
