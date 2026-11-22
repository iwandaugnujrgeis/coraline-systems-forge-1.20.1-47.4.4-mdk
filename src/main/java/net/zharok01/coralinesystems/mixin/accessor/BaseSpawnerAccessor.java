package net.zharok01.coralinesystems.mixin.accessor;

import javax.annotation.Nullable;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccessor {
    @Nullable
    @Accessor("nextSpawnData")
    SpawnData coraline$getNextSpawnData();

    @Accessor("spawnPotentials")
    SimpleWeightedRandomList<SpawnData> coraline$getSpawnPotentials();
}