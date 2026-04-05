package net.zharok01.coralinesystems.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.block.ContainerBlockEntity;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import net.zharok01.coralinesystems.registry.CoralineItems;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class ContainerEvents {

    // Tracks players who died while actually wearing the Cobalt Pants
    private static final Set<UUID> WORE_COBALT_PANTS = new HashSet<>();

    // STEP 1: Check equipment right at the moment of death
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getItemBySlot(EquipmentSlot.LEGS).is(CoralineItems.COBALT_PANTS.get())) {
                WORE_COBALT_PANTS.add(player.getUUID());
            }
        }
    }

    // STEP 2: Handle the drops and spawn the Container
    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isCanceled()) return;

        // If they weren't wearing the pants, do nothing
        if (!WORE_COBALT_PANTS.contains(player.getUUID())) return;

        // Clear the flag
        WORE_COBALT_PANTS.remove(player.getUUID());
        if (event.getDrops().isEmpty()) return;

        Level level = player.level();
        BlockPos deathPos = player.blockPosition();

        // Safe placement: don't spawn below the world floor
        if (deathPos.getY() < level.getMinBuildHeight()) {
            deathPos = new BlockPos(deathPos.getX(), level.getMinBuildHeight() + 1, deathPos.getZ());
        }

        // Don't replace indestructible blocks
        if (level.getBlockState(deathPos).getDestroySpeed(level, deathPos) < 0) {
            deathPos = deathPos.above();
        }

        // Find and consume the Cobalt Pants from the drops list so they aren't saved
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

        // Place the Container block
        level.setBlockAndUpdate(deathPos, CoralineBlocks.CONTAINER_BLOCK.get().defaultBlockState());

        // Store all remaining drops into the Container's inventory
        if (level.getBlockEntity(deathPos) instanceof ContainerBlockEntity containerEntity) {
            for (ItemEntity drop : event.getDrops()) {
                containerEntity.addItem(drop.getItem().copy());
            }

            containerEntity.setOwnerName(player.getGameProfile().getName());

            // --- ANIMATION & SOUND EFFECTS ---
            if (level instanceof ServerLevel serverLevel) {
                // Play a heavy, resonant sound indicating soul entrapment
                serverLevel.playSound(null, deathPos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.0F, 0.5F);
                serverLevel.playSound(null, deathPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.8F);

                // Spawn dripping/soul particles to represent the Cobalt encasing the items
                serverLevel.sendParticles(ParticleTypes.SOUL,
                        deathPos.getX() + 0.5, deathPos.getY() + 0.5, deathPos.getZ() + 0.5,
                        30, 0.5, 0.5, 0.5, 0.1);
                /*
                serverLevel.sendParticles(ParticleTypes.DRIPPING_WATER, // Or custom dripping cobalt particle!
                        deathPos.getX() + 0.5, deathPos.getY() + 1.0, deathPos.getZ() + 0.5,
                        50, 0.5, 0.5, 0.5, 0.5); */
            }

            // Cancel vanilla drop spill — the Container holds everything now
            event.setCanceled(true);
        }
    }
}