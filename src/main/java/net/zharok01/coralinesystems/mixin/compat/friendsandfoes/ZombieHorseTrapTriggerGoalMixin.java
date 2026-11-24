package net.zharok01.coralinesystems.mixin.compat.friendsandfoes;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.faboslav.friendsandfoes.common.entity.ai.goal.zombiehorse.ZombieHorseTrapTriggerGoal", remap = false)
public abstract class ZombieHorseTrapTriggerGoalMixin {

    @Redirect(
            method = "getZombie",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;enchantItem(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/item/ItemStack;IZ)Lnet/minecraft/world/item/ItemStack;",
                    remap = true
            ),
            remap = true
    )
    private ItemStack coraline$keepTrapEquipmentUnenchanted(RandomSource random, ItemStack itemStack, int level, boolean allowTreasure) {
        return itemStack;
    }
}
