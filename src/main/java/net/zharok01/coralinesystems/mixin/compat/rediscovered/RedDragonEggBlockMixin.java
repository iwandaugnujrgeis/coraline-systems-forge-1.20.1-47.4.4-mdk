package net.zharok01.coralinesystems.mixin.compat.rediscovered;

import com.legacy.rediscovered.block.RedDragonEggBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RedDragonEggBlock.class, remap = false)
public class RedDragonEggBlockMixin {

    @Redirect(
            method = "m_6227_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;m_8767_(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"
            )
    )
    private int coraline$redirectParticle(
            ServerLevel level,
            ParticleOptions originalType,
            double x, double y, double z,
            int count,
            double dx, double dy, double dz,
            double speed) {
        return level.sendParticles(
                CoralineParticles.TIMEDUST_SPARKLE.get(),
                x, y, z, count, dx, dy, dz, speed
        );
    }

    @Redirect(
            method = "m_6227_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;m_150930_(Lnet/minecraft/world/item/Item;)Z"
            )
    )
    private boolean coraline$redirectItemCheck(ItemStack stack, Item ignored) {
        return stack.is(CoralineItems.TIMEDUST.get());
    }
}