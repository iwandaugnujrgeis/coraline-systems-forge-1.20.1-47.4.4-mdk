package net.zharok01.coralinesystems.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineTags;

import java.util.Map;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CoralineSpawnGuardEvents {

    private static final long DAY_LENGTH_TICKS = 24000L;
    private static final long SPAWN_WINDOW_START_TICK = 200L;
    private static final long SPAWN_WINDOW_END_TICK = 12000L;

    private static final Map<ResourceKey<Level>, Integer> MIN_SPAWN_Y_BY_DIMENSION = Map.of(Level.OVERWORLD, 50);

    @SuppressWarnings("null")
    @SubscribeEvent
    public static void coraline$onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL || event.getEntityType().getCategory() != MobCategory.CREATURE) {
            return;
        }

        ServerLevelAccessor level = event.getLevel();

        int minSpawnY = MIN_SPAWN_Y_BY_DIMENSION.getOrDefault(level.getLevel().dimension(), Integer.MIN_VALUE);
        if (event.getPos().getY() < minSpawnY && !event.getEntityType().is(CoralineTags.UNDERGROUND_SPAWN_WHITELIST)) {
            event.setResult(Event.Result.DENY);
            return;
        }

        if (!level.dimensionType().hasFixedTime() && !coraline$isInSpawnWindow(level.dayTime())) {
            event.setResult(Event.Result.DENY);
        }
    }

    private static boolean coraline$isInSpawnWindow(long dayTime) {
        long timeOfDay = dayTime % DAY_LENGTH_TICKS;
        return timeOfDay >= SPAWN_WINDOW_START_TICK && timeOfDay < SPAWN_WINDOW_END_TICK;
    }
}