package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.event.DualWieldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "hurtEnemy", at = @At("HEAD"))
    private void coraline$damageOffhandOnHit(LivingEntity target, Player player, CallbackInfo ci) {
        if (player.getMainHandItem() == (Object) this && DualWieldEvents.coraline$isDualWielding(player)) {
            boolean offhandIsSword = player.getOffhandItem().getItem() instanceof SwordItem;
            DualWieldEvents.coraline$damageOffhand(player, offhandIsSword ? 1 : 2);
        }
    }

    @Inject(method = "mineBlock", at = @At("HEAD"))
    private void coraline$damageOffhandOnMine(Level level, BlockState state, BlockPos pos, Player player, CallbackInfo ci) {
        if (player.getMainHandItem() == (Object) this
                && DualWieldEvents.coraline$isDualWielding(player)
                && state.getDestroySpeed(level, pos) != 0.0F) {
            boolean offhandIsSword = player.getOffhandItem().getItem() instanceof SwordItem;
            DualWieldEvents.coraline$damageOffhand(player, offhandIsSword ? 2 : 1);
        }
    }
}
