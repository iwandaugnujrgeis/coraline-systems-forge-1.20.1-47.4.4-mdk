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
 * Bucket-form Wine. Mirrors {@code MilkBucketItem}'s shape (32-tick drink,
 * {@code UseAnim.DRINK}, empties to {@code Items.BUCKET} on finish) with
 * one deliberate divergence: <strong>no {@code initCapabilities} override,
 * no {@code FluidBucketWrapper}</strong>.
 * <p>
 * Per roadmap Section 1d.1/1d.1b: that capability exists so other mods'
 * fluid automation (pipes, tanks) can detect bucket contents, which
 * requires a backing {@code Fluid} object we're deliberately not
 * registering (see Section 1d.0 — none of these four substances need to
 * be placeable). Attaching {@code FluidBucketWrapper} to a bucket with no
 * real fluid behind it is a likely NPE/undefined-behavior source
 * (its {@code getFluid()} does hardcoded {@code instanceof BucketItem}/
 * {@code MilkBucketItem} checks and would return {@code FluidStack.EMPTY}
 * for us anyway), not a nice-to-have we're skipping for convenience.
 * <p>
 * The "filled bucket -&gt; empty bucket" crafting-remainder behavior
 * doesn't need this capability either — that's handled independently via
 * {@code Item.Properties.craftRemainder(Items.BUCKET)} at registration
 * time (see {@code CoralineItems.WINE_BUCKET}).
 */
public class WineBucketItem extends Item {
    private static final int DRINK_DURATION = 32;

    public WineBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;

        // TODO (Session 4): same level-tied drink effect dispatch as
        // WineBottleItem — see that class's javadoc.

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

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        int strength = CoralineFluidUtils.getStrength(stack);
        tooltipComponents.add(Component.translatable("item.coraline_systems.wine_bucket.strength", strength)
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
