package net.zharok01.coralinesystems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Bottle-form Wine. Mirrors {@code PotionItem}'s shape exactly: 32-tick
 * drink duration, {@code UseAnim.DRINK}, empties to {@code Items.GLASS_BOTTLE}
 * on finish, {@code use()} delegates to {@code ItemUtils.startUsingInstantly}.
 * <p>
 * Drink *effects* are explicitly out of scope for Session 1.6 (see roadmap
 * Section 1d.3) — {@link #finishUsingItem} only handles the container swap.
 * Effect dispatch tied to {@link CoralineFluidUtils#getStrength(ItemStack)}
 * is Session 4's job.
 */
public class WineBottleItem extends Item {
    private static final int DRINK_DURATION = 32;

    public WineBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;

        // TODO (Session 4): dispatch Wine's level-tied drink effect here,
        // keyed off CoralineFluidUtils.getStrength(stack). Deliberately not
        // guessed in 1.6 — effect design isn't finalized (see design doc
        // Section 6, "Wine drink effects... parked for a future brainstorm
        // session").

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

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        // Placeholder strength tooltip — cosmetic only, so Session 2/3
        // playtesting doesn't look broken in-inventory before real drink
        // effects (and their own tooltip text) are designed in Session 4.
        int strength = CoralineFluidUtils.getStrength(stack);
        tooltipComponents.add(Component.translatable("item.coraline_systems.wine_bottle.strength", strength)
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
