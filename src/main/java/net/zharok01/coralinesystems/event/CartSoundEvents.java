package net.zharok01.coralinesystems.event;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineSounds;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CartSoundEvents {

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof ChestMenu menu && menu.getSlot(0).container instanceof MinecartChest cart) {
            coraline$playCartSound(event.getEntity().level(), cart.blockPosition(), CoralineSounds.CART_STORAGE_OPEN.get());
        }
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof ChestMenu menu && menu.getSlot(0).container instanceof MinecartChest cart) {
            coraline$playCartSound(event.getEntity().level(), cart.blockPosition(), CoralineSounds.CART_STORAGE_CLOSE.get());
        }
    }

    @SubscribeEvent
    public static void onMinecartAttack(AttackEntityEvent event) {
        if (event.getTarget() instanceof AbstractMinecart cart && !event.getEntity().isCreative()) {
            coraline$playCartSound(event.getEntity().level(), cart.blockPosition(), CoralineSounds.CART_STORAGE_HIT.get());
        }
    }

    @SubscribeEvent
    public static void onMinecartRemove(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof AbstractMinecart cart) {
            Entity.RemovalReason reason = cart.getRemovalReason();
            if (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED) {
                coraline$playCartSound(event.getLevel(), cart.blockPosition(), CoralineSounds.CART_STORAGE_BREAK.get());
            }
        }
    }

    private static void coraline$playCartSound(Level level, BlockPos pos, SoundEvent sound) {
        if (level.isClientSide()) return;
        level.playSound(null, pos, sound, SoundSource.NEUTRAL, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }
}