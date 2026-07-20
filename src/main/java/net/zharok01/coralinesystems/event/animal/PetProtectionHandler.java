package net.zharok01.coralinesystems.event.animal;

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

/**
 * Provides friendly-fire protection for pets and baby mobs.
 *
 * Hardcoded behaviour:
 *   - Owners cannot hurt their own pets (entity type must be in PROTECTED_PETS tag).
 *   - Pets with the same owner cannot hurt each other.
 *   - Players cannot hurt baby non-hostile mobs.
 *   - Crouching always bypasses all protection.
 */
@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PetProtectionHandler {

    /**
     * Entity types that are subject to pet-owner and pet-sibling protection.
     * Populate via: data/coraline_systems/tags/entity_types/protected_pets.json
     */
    private static final TagKey<EntityType<?>> PROTECTED_PETS =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(CoralineSystems.MOD_ID, "protected_pets"));

    // -------------------------------------------------------------------------
    // Event listeners
    // -------------------------------------------------------------------------

    /** Pre-armor calculation guard — the primary, cheapest cancel point. */
    @SubscribeEvent
    public static void onEntityAttack(LivingAttackEvent event) {
        if (shouldCancel(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
            clearCombatTracking(event.getEntity(), event.getSource());
        }
    }

    /** Post-armor calculation guard — catches anything that slipped past the Attack event. */
    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        if (shouldCancel(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
            event.setAmount(0f);
            clearCombatTracking(event.getEntity(), event.getSource());
        }
    }

    // -------------------------------------------------------------------------
    // Core logic
    // -------------------------------------------------------------------------

    private static boolean shouldCancel(Entity target, DamageSource source) {
        if (source == null) {
            return false;
        }
        return isProtected(target, source.getEntity());
    }

    private static boolean isProtected(Entity target, @Nullable Entity attacker) {

        // Environmental / indirect damage (no attacker entity) is never blocked.
        if (attacker == null) {
            return false;
        }

        // Crouching always bypasses protection.
        if (attacker.isCrouching()) {
            return false;
        }

        // Pet protection: only applies to entity types listed in the tag.
        if (target.getType().is(PROTECTED_PETS)) {
            final UUID targetOwner = getOwnerUUID(target);
            if (targetOwner != null) {

                // Owner hurting their own pet.
                if (targetOwner.equals(attacker.getUUID())) {
                    return true;
                }

                // A sibling pet (same owner) hurting this pet.
                if (targetOwner.equals(getOwnerUUID(attacker))) {
                    return true;
                }
            }
        }

        // Baby protection: players cannot kill baby non-hostile mobs.
        if (attacker instanceof Player
                && !(target instanceof Enemy)
                && target instanceof AgeableMob baby
                && baby.isBaby()) {
            return true;
        }

        return false;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Clears last-hurt-by tracking so a cancelled hit leaves no combat state
     * artifacts (e.g. a wolf turning hostile after a blocked hit).
     */
    private static void clearCombatTracking(Entity target, DamageSource source) {
        if (target instanceof LivingEntity living) {
            living.setLastHurtByMob(null);
        }
        if (source.getEntity() instanceof LivingEntity attacker) {
            attacker.setLastHurtByMob(null);
        }
    }

    /**
     * Returns the owner UUID for any ownable entity.
     * AbstractHorse is a special case — Mojang didn't implement OwnableEntity for it.
     */
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