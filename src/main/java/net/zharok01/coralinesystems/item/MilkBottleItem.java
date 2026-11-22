package net.zharok01.coralinesystems.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MilkBottleItem extends Item {

    public MilkBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        super.finishUsingItem(itemStack, level, livingEntity);

        if (!level.isClientSide) {
            coraline$affectConsumer(itemStack, level, livingEntity);
        }

        if (livingEntity instanceof Player player && player.getAbilities().instabuild) {
            return itemStack;
        }

        itemStack.shrink(1);
        if (itemStack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }

        if (livingEntity instanceof Player player) {
            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().add(emptyBottle)) {
                player.drop(emptyBottle, false);
            }
        }

        return itemStack;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack itemStack) {
        return 32;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
        return UseAnim.DRINK;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    private void coraline$affectConsumer(ItemStack itemStack, Level level, LivingEntity consumer) {
        List<MobEffect> compatibleEffects = new ArrayList<>();

        for (MobEffectInstance effect : consumer.getActiveEffects()) {
            if (effect.isCurativeItem(new ItemStack(Items.MILK_BUCKET))) {
                compatibleEffects.add(effect.getEffect());
            }
        }

        if (!compatibleEffects.isEmpty()) {
            MobEffectInstance selectedEffect = consumer.getEffect(compatibleEffects.get(level.random.nextInt(compatibleEffects.size())));
            if (selectedEffect != null && !MinecraftForge.EVENT_BUS.post(new MobEffectEvent.Remove(consumer, selectedEffect))) {
                consumer.removeEffect(selectedEffect.getEffect());
            }
        }
    }
}