package net.zharok01.coralinesystems.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.entity.projectile.ThrownModdedEgg;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class ModdedEggItem extends Item {

    private final BiFunction<Level, LivingEntity, ThrownModdedEgg> thrownEggFactory;

    public ModdedEggItem(BiFunction<Level, LivingEntity, ThrownModdedEgg> thrownEggFactory, Item.Properties properties) {
        super(properties);
        this.thrownEggFactory = thrownEggFactory;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        level.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if (!level.isClientSide) {
            ThrownModdedEgg thrownEgg = this.thrownEggFactory.apply(level, player);
            thrownEgg.setItem(itemStack);
            thrownEgg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(thrownEgg);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}