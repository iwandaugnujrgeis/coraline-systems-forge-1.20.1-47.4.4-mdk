package net.zharok01.coralinesystems.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineTags;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodParticleHandler {

    private static final int MAX_DROPLETS = 6;
    private static final double REFERENCE_HITBOX_VOLUME = 0.6 * 0.6 * 1.8;

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

        if (!entity.getType().is(CoralineTags.BLEEDS)) return;

        float damage = event.getAmount();
        float maxHp = entity.getMaxHealth();

        double hitboxVolume = (double) entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight();
        int maxDroplets = Math.max(1, Mth.ceil(MAX_DROPLETS * hitboxVolume / REFERENCE_HITBOX_VOLUME));
        int count = Mth.clamp(Math.round((damage / maxHp) * maxDroplets), 1, maxDroplets);

        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.6;
        double z = entity.getZ();

        serverLevel.sendParticles(
                CoralineParticles.BLOOD_DROPLET.get(),
                x, y, z,
                count,
                entity.getBbWidth() * 0.3,
                entity.getBbHeight() * 0.2,
                entity.getBbWidth() * 0.3,
                0.15
        );
    }
}