package net.zharok01.coralinesystems.event;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineItems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class NameTagShearEvents {

    private static final int CRUMBLE_PARTICLE_COUNT = 12;
    private static final double CRUMBLE_PARTICLE_SPREAD = 0.25;
    private static final double CRUMBLE_PARTICLE_SPEED = 0.05;

    @SubscribeEvent
    public static void coraline$onShearNameTag(PlayerInteractEvent.EntityInteract event) {
        ItemStack heldStack = event.getItemStack();
        Entity target = event.getTarget();
        Player player = event.getEntity();

        if (!(heldStack.getItem() instanceof ShearsItem) || target instanceof Player || !target.hasCustomName()) {
            return;
        }

        boolean isShearableNow = target instanceof IForgeShearable shearable
                && shearable.isShearable(heldStack, event.getLevel(), target.blockPosition());
        if (isShearableNow && !player.isSecondaryUseActive()) {
            return;
        }

        if (!event.getLevel().isClientSide) {
            Component removedName = target.getCustomName();
            target.setCustomName(null);
            target.setCustomNameVisible(false);
            heldStack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(event.getHand()));
            event.getLevel().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0f, 1.0f);

            ItemStack droppedTag = new ItemStack(CoralineItems.NAME_TAG.get());
            droppedTag.setHoverName(removedName);

            if (!player.getAbilities().instabuild) {
                target.spawnAtLocation(droppedTag, target.getBbHeight() / 2.0f);
            }

            ((ServerLevel) event.getLevel()).sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(CoralineItems.NAME_TAG.get())),
                    target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(),
                    CRUMBLE_PARTICLE_COUNT,
                    CRUMBLE_PARTICLE_SPREAD, CRUMBLE_PARTICLE_SPREAD, CRUMBLE_PARTICLE_SPREAD,
                    CRUMBLE_PARTICLE_SPEED);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        event.setCanceled(true);
    }
}