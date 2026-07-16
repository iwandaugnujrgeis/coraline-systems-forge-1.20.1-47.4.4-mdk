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
 * Bottle-form Dregs — the collectible product of a spoiled Wine batch
 * (design doc Section 3) and the required culture input for Kombucha
 * (design doc Section 4, step 3). Single-strength, no
 * {@link CoralineFluidUtils} usage, same shape as {@link WineBottleItem}.
 * <p>
 * Drinkability itself is unresolved per the roadmap
 * ("Dregs presumably non-drinkable or a joke/negative effect", Section
 * 1d.3) — {@link #finishUsingItem} keeps the same generic drink-and-empty
 * shape as the other three for now so the item is functional and
 * consistent during Session 2/3 playtesting; Session 4 decides whether
 * Dregs should even be drinkable at all, or whether {@code use()} should
 * be blocked/repurposed instead (e.g. drink-to-add-to-Kombucha-cauldron
 * being the only real interaction, not consumption).
 */
public class DregsBottleItem extends Item {
    private static final int DRINK_DURATION = 32;

    public DregsBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;

        // TODO (Session 4): decide + implement Dregs' drink effect (or lack
        // thereof) — see class javadoc. Not guessed here.

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
