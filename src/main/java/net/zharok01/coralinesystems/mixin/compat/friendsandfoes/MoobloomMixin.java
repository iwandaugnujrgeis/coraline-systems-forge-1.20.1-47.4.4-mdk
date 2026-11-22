package net.zharok01.coralinesystems.mixin.compat.friendsandfoes;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.zharok01.coralinesystems.util.interfaces.CowMilkDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.faboslav.friendsandfoes.common.entity.MoobloomEntity", remap = false)
public abstract class MoobloomMixin {

    @Inject(method = "m_6071_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$limitStewMilking(Player player, InteractionHand hand,
                                           CallbackInfoReturnable<InteractionResult> cir) {
        Animal moobloom = (Animal) (Object) this;
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(Items.BOWL) && !moobloom.isBaby()) {
            CowMilkDuck duck = (CowMilkDuck) (Object) this;
            if (!duck.coraline$canReceiveStewServing()) {
                cir.setReturnValue(InteractionResult.FAIL);
            } else {
                cir.setReturnValue(InteractionResult.sidedSuccess(moobloom.level().isClientSide));
                if (!moobloom.level().isClientSide) {
                    var effectHolders = SuspiciousEffectHolder.getAllEffectHolders();
                    SuspiciousEffectHolder effectHolder = effectHolders.get(moobloom.getRandom().nextInt(effectHolders.size()));
                    ItemStack suspiciousStew = new ItemStack(Items.SUSPICIOUS_STEW);
                    SuspiciousStewItem.saveMobEffect(suspiciousStew, effectHolder.getSuspiciousEffect(), effectHolder.getEffectDuration());
                    player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, suspiciousStew, false));
                    moobloom.playSound(SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY, 2.0F, 1.0F);
                    duck.coraline$recordStewServing();
                }
            }
        }
    }
}
