package net.zharok01.coralinesystems.quiver;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingGetProjectileEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QuiverEvents {
    @SubscribeEvent
    public static void onGetProjectile(LivingGetProjectileEvent event) {
        if (event.getEntity() instanceof Player player
                && event.getProjectileWeaponItemStack().getItem() instanceof ProjectileWeaponItem weapon
                && (weapon instanceof BowItem || weapon instanceof CrossbowItem)
                && !event.getProjectileItemStack().is(Items.FIREWORK_ROCKET)
                && QuiverContents.isWornBy(player)) {
            NonNullList<ItemStack> contents = QuiverContents.read(player.getItemBySlot(EquipmentSlot.CHEST));
            int slot = QuiverContents.pickRandomAmmoSlot(contents, weapon, player.getRandom());
            if (slot != -1) {
                ItemStack arrow = contents.get(slot).copy();
                arrow.setCount(1);
                event.setProjectileItemStack(arrow);
                if (!player.level().isClientSide) {
                    QuiverVolley.recordQuiverArrow(player, arrow);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof AbstractArrow arrow) {
            QuiverVolley.captureMainArrow(arrow);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            ItemStack worn = event.player.getItemBySlot(EquipmentSlot.CHEST);
            if (QuiverContents.isQuiver(worn)) {
                QuiverContents.syncLock(worn);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player player) {
            List<ItemEntity> spilledArrows = new ArrayList<>();
            for (ItemEntity drop : event.getDrops()) {
                if (QuiverContents.isQuiver(drop.getItem())) {
                    for (ItemStack arrow : QuiverContents.removeAll(drop.getItem())) {
                        spilledArrows.add(new ItemEntity(player.level(), drop.getX(), drop.getY(), drop.getZ(), arrow));
                    }
                }
            }
            event.getDrops().addAll(spilledArrows);
        }
    }

    @SubscribeEvent
    public static void coraline$onSkeletonDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Skeleton skeleton)) {
            return;
        }

        for (ItemEntity drop : event.getDrops()) {
            ItemStack quiverDrop = drop.getItem();
            if (QuiverContents.isQuiver(quiverDrop)) {
                QuiverContents.write(quiverDrop, NonNullList.withSize(QuiverContents.SLOT_COUNT, ItemStack.EMPTY));
                quiverDrop.setDamageValue(skeleton.getRandom().nextInt(QuiverContents.MAX_DURABILITY - 1) + 1);
            }
        }
    }
}