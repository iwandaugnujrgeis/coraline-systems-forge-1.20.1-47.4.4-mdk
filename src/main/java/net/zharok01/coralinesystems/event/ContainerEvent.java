package net.zharok01.coralinesystems.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.block.ContainerBlockEntity;
import net.zharok01.coralinesystems.util.compat.quark.BackpackCompat;
import net.zharok01.coralinesystems.util.compat.quark.BackpackDurability;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class ContainerEvent {

    private static final Set<UUID> WORE_COBALT_PANTS = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getItemBySlot(EquipmentSlot.LEGS).is(CoralineItems.COBALT_PANTS.get())) {
                WORE_COBALT_PANTS.add(player.getUUID());
            } else if (ModList.get().isLoaded(BackpackCompat.QUARK_ODDITIES_MOD_ID)) {
                BackpackDurability.coraline$forgetBrokenBackpackDrops(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isCanceled()) {
            WORE_COBALT_PANTS.remove(player.getUUID());
            if (ModList.get().isLoaded(BackpackCompat.QUARK_ODDITIES_MOD_ID)) {
                BackpackDurability.coraline$forgetBrokenBackpackDrops(player);
            }
            return;
        }
        if (!WORE_COBALT_PANTS.contains(player.getUUID())) return;
        WORE_COBALT_PANTS.remove(player.getUUID());
        boolean hasBrokenBackpackDrops = ModList.get().isLoaded(BackpackCompat.QUARK_ODDITIES_MOD_ID)
                && BackpackDurability.coraline$hasBrokenBackpackDrops(player);
        if (event.getDrops().isEmpty() && !hasBrokenBackpackDrops) return;

        Level level = player.level();
        BlockPos deathPos = player.blockPosition();

        if (deathPos.getY() < level.getMinBuildHeight()) {
            deathPos = new BlockPos(deathPos.getX(), level.getMinBuildHeight() + 1, deathPos.getZ());
        }

        if (level.getBlockState(deathPos).getDestroySpeed(level, deathPos) < 0) {
            deathPos = deathPos.above();
        }

        BlockState replacedState = level.getBlockState(deathPos);
        level.setBlockAndUpdate(deathPos, CoralineBlocks.CONTAINER_BLOCK.get().defaultBlockState());

        if (!(level.getBlockEntity(deathPos) instanceof ContainerBlockEntity containerEntity)) {
            level.setBlockAndUpdate(deathPos, replacedState);
            return;
        }

        ItemEntity pantsDrop = null;
        for (ItemEntity drop : event.getDrops()) {
            if (drop.getItem().is(CoralineItems.COBALT_PANTS.get())) {
                pantsDrop = drop;
                break;
            }
        }
        if (pantsDrop != null) {
            event.getDrops().remove(pantsDrop);
        }

        List<ItemStack> backpackContents = ModList.get().isLoaded(BackpackCompat.QUARK_ODDITIES_MOD_ID)
                ? BackpackCompat.coraline$emptyBackpacks(event.getDrops())
                : List.of();
        List<ItemStack> brokenBackpackContents = hasBrokenBackpackDrops
                ? BackpackDurability.coraline$collectBrokenBackpackDrops(player)
                : List.of();

        for (ItemEntity drop : event.getDrops()) {
            containerEntity.addItem(drop.getItem().copy());
        }
        backpackContents.forEach(containerEntity::addItem);
        brokenBackpackContents.forEach(containerEntity::addItem);

        containerEntity.setOwnerName(player.getGameProfile().getName());
        CoralineTriggers.COBALT_PANTS.trigger(player);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, deathPos, CoralineSounds.DEATH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.FLASH,
                    deathPos.getX() + 0.5, deathPos.getY() + 0.5, deathPos.getZ() + 0.5,
                    1, 0, 0, 0, 0);
        }

        event.setCanceled(true);
    }
}