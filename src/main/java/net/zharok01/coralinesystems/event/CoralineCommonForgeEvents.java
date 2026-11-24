package net.zharok01.coralinesystems.event;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.legacy.rediscovered.registry.RediscoveredDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CoralineCommonForgeEvents {

    private static final SoundEvent SOUL_STEAL_SOUND =
            SoundEvent.createVariableRangeEvent(Objects.requireNonNull(ResourceLocation.tryBuild("gamma", "suspicious.soul_steal")));

    @SubscribeEvent
    public static void coraline$adjustFallDamageThreshold(LivingFallEvent event) {
        event.setDistance(Math.max(0.0F, event.getDistance() - 2.0F));
    }

    @SubscribeEvent
    public static void onWolfChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf)) return;
        if (!(event.getNewTarget() instanceof Sheep)) return;
        if (!wolf.level().dimension().equals(RediscoveredDimensions.skylandsKey())) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawnType() != MobSpawnType.SPAWNER) return;

        ServerLevelAccessor level = event.getLevel();
        if (level.isClientSide()) return;

        var random = level.getRandom();
        level.getLevel().playSound(
                null,
                event.getX(),
                event.getY(),
                event.getZ(),
                SOUL_STEAL_SOUND,
                SoundSource.PLAYERS,
                0.9f + random.nextFloat() * 0.1f,
                0.7f + random.nextFloat() * 0.4f
        );
    }

    @SubscribeEvent
    public static void onCombJellySpawnCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getEntityType() != AMEntityRegistry.COMB_JELLY.get()) return;

        ServerLevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        MobSpawnType spawnType = event.getSpawnType();

        if (spawnType == MobSpawnType.SPAWNER || spawnType == MobSpawnType.SPAWN_EGG) {
            event.setResult(Event.Result.ALLOW);
            return;
        }

        boolean inOpenWater = level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);
        event.setResult(inOpenWater ? Event.Result.ALLOW : Event.Result.DENY);
    }

    private static boolean isSquareOfSaplings(LevelAccessor level, BlockPos pos, int xOffset, int zOffset) {
        return level.getBlockState(pos.offset(xOffset, 0, zOffset)).is(Blocks.DARK_OAK_SAPLING) &&
                level.getBlockState(pos.offset(xOffset + 1, 0, zOffset)).is(Blocks.DARK_OAK_SAPLING) &&
                level.getBlockState(pos.offset(xOffset, 0, zOffset + 1)).is(Blocks.DARK_OAK_SAPLING) &&
                level.getBlockState(pos.offset(xOffset + 1, 0, zOffset + 1)).is(Blocks.DARK_OAK_SAPLING);
    }
}