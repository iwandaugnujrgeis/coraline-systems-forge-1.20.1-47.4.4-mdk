package net.zharok01.coralinesystems.item;

import einstein.subtle_effects.init.ModParticles;
import einstein.subtle_effects.particle.option.ColorAndIntegerParticleOptions;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.quark.api.IRuneColorProvider;
import org.violetmoon.quark.base.capability.QuarkForgeCapabilities;
import org.violetmoon.quark.content.tools.base.RuneColor;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractCoralineDrinkItem extends Item {

    private static final int DRINK_DURATION = 32;
    private final Item emptyContainerItem;

    @Nullable
    private final Function<ItemStack, Component> tooltipProvider;

    public AbstractCoralineDrinkItem(Properties properties, Item emptyContainerItem) {
        this(properties, emptyContainerItem, null);
    }

    public AbstractCoralineDrinkItem(Properties properties, Item emptyContainerItem, @Nullable Function<ItemStack, Component> tooltipProvider) {
        super(properties);
        this.emptyContainerItem = emptyContainerItem;
        this.tooltipProvider = tooltipProvider;
    }

    protected abstract int getDrinkColor(ItemStack stack);

    protected boolean shouldSpawnDrinkParticles() {
        return true;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IRuneColorProvider> provider = LazyOptional.of(() -> itemStack -> RuneColor.LIME);

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == QuarkForgeCapabilities.RUNE_COLOR) {
                    return provider.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }

        if (!level.isClientSide) {
            this.applyDrinkEffect(stack, level, livingEntity);
        } else if (shouldSpawnDrinkParticles()) {
            level.addParticle(
                    new ColorAndIntegerParticleOptions(ModParticles.POTION_EMITTER.get(), getDrinkColor(stack), livingEntity.getId()),
                    livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                    0.0, 0.0, 0.0
            );
        }

        if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                return new ItemStack(this.emptyContainerItem);
            }

            if (!player.getInventory().add(new ItemStack(this.emptyContainerItem))) {
                player.drop(new ItemStack(this.emptyContainerItem), false);
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

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        if (tooltipProvider != null) {
            Component line = tooltipProvider.apply(stack);
            if (line != null) {
                tooltipComponents.add(line);
            }
        }
    }

    protected static Component blue(Component text) {
        return text.copy().withStyle(ChatFormatting.BLUE);
    }

    protected static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    protected abstract void applyDrinkEffect(ItemStack stack, Level level, LivingEntity livingEntity);
}