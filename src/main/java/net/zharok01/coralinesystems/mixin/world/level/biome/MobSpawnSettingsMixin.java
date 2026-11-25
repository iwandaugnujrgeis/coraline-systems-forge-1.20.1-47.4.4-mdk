package net.zharok01.coralinesystems.mixin.world.level.biome;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.zharok01.coralinesystems.util.CoralineSpawnCategories;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(MobSpawnSettings.class)
public abstract class MobSpawnSettingsMixin {

    @Shadow
    @Final
    @Mutable
    private Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawners;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void coraline$moveMooshroomsToMonsterSpawns(CallbackInfo ci) {
        WeightedRandomList<MobSpawnSettings.SpawnerData> creatureSpawnList =
                this.spawners.getOrDefault(MobCategory.CREATURE, MobSpawnSettings.EMPTY_MOB_LIST);
        List<MobSpawnSettings.SpawnerData> remainingCreatures = new ArrayList<>();
        List<MobSpawnSettings.SpawnerData> mooshroomSpawns = new ArrayList<>();

        for (MobSpawnSettings.SpawnerData spawnerData : creatureSpawnList.unwrap()) {
            if (CoralineSpawnCategories.coraline$isMooshroom(spawnerData.type)) {
                mooshroomSpawns.add(spawnerData);
            } else {
                remainingCreatures.add(spawnerData);
            }
        }

        if (mooshroomSpawns.isEmpty()) {
            return;
        }

        WeightedRandomList<MobSpawnSettings.SpawnerData> monsterSpawnList =
                this.spawners.getOrDefault(MobCategory.MONSTER, MobSpawnSettings.EMPTY_MOB_LIST);
        Set<EntityType<?>> existingMooshrooms = new HashSet<>();
        List<MobSpawnSettings.SpawnerData> monsters = new ArrayList<>(monsterSpawnList.unwrap());
        for (MobSpawnSettings.SpawnerData spawnerData : monsters) {
            if (CoralineSpawnCategories.coraline$isMooshroom(spawnerData.type)) {
                existingMooshrooms.add(spawnerData.type);
            }
        }
        for (MobSpawnSettings.SpawnerData spawnerData : mooshroomSpawns) {
            if (!existingMooshrooms.contains(spawnerData.type)) {
                monsters.add(spawnerData);
            }
        }

        Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> updatedSpawners =
                new EnumMap<>(MobCategory.class);
        updatedSpawners.putAll(this.spawners);
        updatedSpawners.put(MobCategory.CREATURE, WeightedRandomList.create(remainingCreatures));
        updatedSpawners.put(MobCategory.MONSTER, WeightedRandomList.create(monsters));
        this.spawners = ImmutableMap.copyOf(updatedSpawners);
    }
}
