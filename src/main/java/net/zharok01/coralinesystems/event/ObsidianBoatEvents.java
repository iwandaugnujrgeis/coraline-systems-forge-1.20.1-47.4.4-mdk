package net.zharok01.coralinesystems.event;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.ObsidianBoatEntity;
import net.zharok01.coralinesystems.mixin.accessor.BoatAccessor;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ObsidianBoatEvents {

    private static boolean coraline$isShieldedFromFire(Entity passenger) {
        if (!(passenger.getVehicle() instanceof ObsidianBoatEntity boat)) return false;

        Boat.Status status = ((BoatAccessor) boat).coraline$getStatus();
        return status != Boat.Status.UNDER_WATER && status != Boat.Status.UNDER_FLOWING_WATER;
    }

    @SubscribeEvent
    public static void onPassengerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().isOnFire() && coraline$isShieldedFromFire(event.getEntity())) {
            event.getEntity().clearFire();
        }
    }

    @SubscribeEvent
    public static void onPassengerAttack(LivingAttackEvent event) {
        if (event.getSource().is(DamageTypeTags.IS_FIRE) && coraline$isShieldedFromFire(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}