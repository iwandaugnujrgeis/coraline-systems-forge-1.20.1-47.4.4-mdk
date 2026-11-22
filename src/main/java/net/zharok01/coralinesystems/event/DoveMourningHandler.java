package net.zharok01.coralinesystems.event;

import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DoveMourningHandler { // Unused for now as I can't model custom entities
    /*
    private static final double MOURNING_RADIUS = 15.0;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dying = event.getEntity();
        if (dying.level().isClientSide) return;
        if (dying instanceof DoveEntity) return;

        AABB searchBox = dying.getBoundingBox().inflate(MOURNING_RADIUS);
        List<DoveEntity> nearbyDoves = dying.level().getEntitiesOfClass(
                DoveEntity.class, searchBox, dove -> true);

        for (DoveEntity dove : nearbyDoves) {
            dove.onNearbyDeath();
        }
    }
     */
}