package net.zharok01.coralinesystems.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraftforge.event.entity.living.EnderManAngerEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.helper.StealthHelper;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class InvisibilityStealthEvents {

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();
        if (!(event.getEntity() instanceof Mob) || newTarget == null || !StealthHelper.coraline$isHidden(newTarget)) {
            return;
        }

        event.setCanceled(true);

        if (event.getEntity() instanceof EnderMan enderMan) {
            enderMan.setTarget(null);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide() && mob.getTarget() != null && StealthHelper.coraline$isHidden(mob.getTarget())) {
            mob.setTarget(null);
        }
    }

    @SubscribeEvent
    public static void onEnderManAnger(EnderManAngerEvent event) {
        if (StealthHelper.coraline$isHidden(event.getPlayer())) {
            event.setCanceled(true);
        }
    }
}