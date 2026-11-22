package net.zharok01.coralinesystems.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.zharok01.coralinesystems.registry.CoralineClientHooks;
import org.jetbrains.annotations.NotNull;

public class CoralineNameTagItem extends NameTagItem {

    public CoralineNameTagItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, @NotNull Player player,
                                                           @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (!itemStack.hasCustomHoverName() || target instanceof Player) {
            return InteractionResult.PASS;
        }

        Level level = player.level();
        if (!level.isClientSide && target.isAlive()) {
            target.setCustomName(itemStack.getHoverName());
            target.setCustomNameVisible(true);
            if (target instanceof Mob mob) {
                mob.setPersistenceRequired();
            }
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 1.0f, 1.0f);
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CoralineClientHooks.openNameTagScreen(itemStack, hand));
        }
        return InteractionResultHolder.success(itemStack);
    }
}