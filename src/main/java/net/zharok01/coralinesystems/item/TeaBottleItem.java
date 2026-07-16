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
 * Bottle-form Tea. Same shape as {@link WineBottleItem}, including the
 * strength tooltip — Tea Leaves use the same 1-5 level-tracking pattern as
 * Mulberries (design doc Section 4, step 2). Note the Tea *recipe* itself
 * (Water + Tea Leaves only, no Dregs) is deferred to Session 5 per the
 * design doc Section 5 — this item exists now purely as part of 1.6's
 * container/strength-plumbing batch, ahead of the recipe that will
 * actually produce it.
 */
public class TeaBottleItem extends Item {
    private static final int DRINK_DURATION = 32;

    public TeaBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;

        // TODO (Session 5+): Tea's drink effect is not yet designed at all
        // (design doc Section 6, "Tea recipe details... effects, brew
        // time" explicitly parked). No placeholder guessed here.

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
        int strength = CoralineFluidUtils.getStrength(stack);
        tooltipComponents.add(Component.translatable("item.coraline_systems.tea_bottle.strength", strength)
                .withStyle(ChatFormatting.GREEN));
    }
}
