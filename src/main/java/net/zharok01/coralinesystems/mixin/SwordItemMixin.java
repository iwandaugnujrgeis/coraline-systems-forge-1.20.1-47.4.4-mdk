package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.util.AnimationTypes;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin extends TieredItem {

    public SwordItemMixin(Tier tier, Properties props) {
        super(tier, props);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return AnimationTypes.SWORD_BLOCK;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(InteractionHand.OFF_HAND);
        boolean flag = stack.getItem().getUseAnimation(stack) != UseAnim.NONE;
        if (hand == InteractionHand.MAIN_HAND && !flag) {
            player.startUsingItem(hand);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        if (hand == InteractionHand.OFF_HAND || flag) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return super.use(world, player, hand);
    }

}