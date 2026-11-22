package net.zharok01.coralinesystems.event.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

import javax.annotation.Nullable;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PetProtectionHandler {

    @SuppressWarnings("removal")
    private static final TagKey<EntityType<?>> PROTECTED_PETS =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(CoralineSystems.MOD_ID, "protected_pets"));

    @SubscribeEvent
    public static void onEntityAttack(LivingAttackEvent event) {
        if (shouldCancel(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
            clearCombatTracking(event.getEntity(), event.getSource());
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        if (shouldCancel(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
            event.setAmount(0f);
            clearCombatTracking(event.getEntity(), event.getSource());
        }
    }

    private static boolean shouldCancel(Entity target, DamageSource source) {
        if (source == null) {
            return false;
        }
        return isProtected(target, source.getEntity());
    }

    private static boolean isProtected(Entity target, @Nullable Entity attacker) {

        if (attacker == null) {
            return false;
        }

        if (attacker.isCrouching()) {
            return false;
        }

        if (target.getType().is(PROTECTED_PETS)) {
            final UUID targetOwner = getOwnerUUID(target);
            if (targetOwner != null) {

                if (targetOwner.equals(attacker.getUUID())) {
                    return true;
                }

                if (targetOwner.equals(getOwnerUUID(attacker))) {
                    return true;
                }
            }
        }

        return attacker instanceof Player
                && !(target instanceof Enemy)
                && target instanceof AgeableMob baby
                && baby.isBaby();
    }

    private static void clearCombatTracking(Entity target, DamageSource source) {
        if (target instanceof LivingEntity living) {
            living.setLastHurtByMob(null);
        }
        if (source.getEntity() instanceof LivingEntity attacker) {
            attacker.setLastHurtByMob(null);
        }
    }

    @Nullable
    private static UUID getOwnerUUID(Entity entity) {
        if (entity instanceof OwnableEntity ownable) {
            return ownable.getOwnerUUID();
        }
        if (entity instanceof AbstractHorse horse) {
            return horse.getOwnerUUID();
        }
        return null;
    }
}