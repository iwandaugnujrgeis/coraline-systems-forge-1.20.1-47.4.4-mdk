package net.zharok01.coralinesystems.item;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractCoralineDrinkItem extends Item {
    private static final int DRINK_DURATION = 32;
    private final Item emptyContainerItem;

    /**
     * Whether this drink carries a meaningful 1-5 strength (Wine, Tea,
     * Mulberry Juice) or is single-strength/inert (Kombucha, Dregs). When
     * true, {@link #appendHoverText} adds a strength line automatically —
     * subclasses that want a strength tooltip only need to pass true here
     * and a translation key; they don't need to override appendHoverText.
     */
    private final boolean hasStrength;
    private final String strengthTranslationKey;
    private final ChatFormatting strengthTooltipColor;

    protected AbstractCoralineDrinkItem(Properties properties, Item emptyContainerItem) {
        this(properties, emptyContainerItem, null, null);
    }

    /**
     * @param strengthTranslationKey full translation key for the strength
     *                                tooltip line (e.g.
     *                                "item.coraline_systems.wine_bottle.strength"),
     *                                or null if this drink has no strength
     *                                concept at all.
     * @param strengthTooltipColor   color to style the strength line with;
     *                                ignored if strengthTranslationKey is null.
     */
    protected AbstractCoralineDrinkItem(Properties properties, Item emptyContainerItem,
                                        @Nullable String strengthTranslationKey,
                                        @Nullable ChatFormatting strengthTooltipColor) {
        super(properties);
        this.emptyContainerItem = emptyContainerItem;
        this.hasStrength = strengthTranslationKey != null;
        this.strengthTranslationKey = strengthTranslationKey;
        this.strengthTooltipColor = strengthTooltipColor;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        // Defer the actual drink logic to the specific child class
        if (!level.isClientSide) {
            this.applyDrinkEffect(stack, level, livingEntity);
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild) {
            if (stack.isEmpty()) {
                return new ItemStack(this.emptyContainerItem);
            }

            if (player != null) {
                // Safely handle inventory insertion, dropping the item if the inventory is full
                if (!player.getInventory().add(new ItemStack(this.emptyContainerItem))) {
                    player.drop(new ItemStack(this.emptyContainerItem), false);
                }
            }
        }

        livingEntity.gameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return DRINK_DURATION;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        return ItemUtils.startUsingInstantly(level, player, usedHand);
    }

    /**
     * Maps a 1-5 strength level to its named translation key.
     * Keys are defined in en_us.json as "tooltip.coraline_systems.strength.N".
     */
    private static Component strengthName(int strength) {
        int clamped = Math.max(1, Math.min(5, strength));
        return Component.translatable("tooltip.coraline_systems.strength." + clamped);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        if (this.hasStrength) {
            int strength = CoralineFluidUtils.getStrength(stack);
            Component nameComponent = strengthName(strength);
            Component line = Component.translatable(this.strengthTranslationKey, nameComponent);
            if (this.strengthTooltipColor != null) {
                line = line.copy().withStyle(this.strengthTooltipColor);
            }
            tooltipComponents.add(line);
        }
    }

    /**
     * Override this method in subclasses to apply the specific effects of the drink
     * (e.g., setting a respawn point, applying potion effects based on NBT strength).
     */
    protected abstract void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity);
}