package net.zharok01.coralinesystems.mixin.compat.nostalgic_tweaks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import mod.adrenix.nostalgic.helper.gameplay.AnimalSpawnHelper;
import mod.adrenix.nostalgic.tweak.config.GameplayTweak;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Stream;

@Pseudo
@Mixin(value = AnimalSpawnHelper.class, remap = false)
public abstract class AnimalSpawnHelperMixin {

    @Unique
    private static final int POLL_RADIUS_CHUNKS = 8;

    @Unique
    private static volatile Set<EntityType<?>> coraline$spawnTypes = Set.of();
    @Unique
    private static volatile Set<String> coraline$lastSpawnList = Set.of();
    @Unique
    private static volatile String[] coraline$spawnArray = new String[0];
    @Unique
    private static volatile boolean coraline$cacheReady = false;

    @Inject(method = "isInList", at = @At("HEAD"), cancellable = true)
    private static void coraline$cachedIsInList(EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir) {
        if (coraline$cacheReady) {
            cir.setReturnValue(coraline$spawnTypes.contains(entityType));
        }
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "tickLevel", at = @At("HEAD"))
    private static void coraline$rebuildCaches(ServerLevel level, boolean spawnFriendlies, CallbackInfo ci) {
        if (!spawnFriendlies)
            return;

        Set<String> current = GameplayTweak.OLD_ANIMAL_SPAWN_LIST.get().getSet();
        if (coraline$cacheReady && current.equals(coraline$lastSpawnList))
            return;

        coraline$lastSpawnList = new HashSet<>(current);
        coraline$spawnArray = current.toArray(String[]::new);

        Set<EntityType<?>> types = new HashSet<>(current.size());
        for (String key : current) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id != null) {
                BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(types::add);
            }
        }
        coraline$spawnTypes = types;
        coraline$cacheReady = true;
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Redirect(
            method = "tickLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getAllEntities()Ljava/lang/Iterable;",
                    remap = true
            ),
            remap = false
    )
    private static Iterable<Entity> coraline$getPolledAnimals(ServerLevel level) {
        IntOpenHashSet seenIds = new IntOpenHashSet();
        List<Entity> animals = new ArrayList<>();

        for (ServerPlayer player : level.players()) {
            int chunkX = Mth.floor(player.getX() / 16.0D);
            int chunkZ = Mth.floor(player.getZ() / 16.0D);
            AABB pollArea = new AABB(
                    (chunkX - POLL_RADIUS_CHUNKS) * 16.0D, level.getMinBuildHeight(),
                    (chunkZ - POLL_RADIUS_CHUNKS) * 16.0D,
                    (chunkX + POLL_RADIUS_CHUNKS + 1) * 16.0D, level.getMaxBuildHeight(),
                    (chunkZ + POLL_RADIUS_CHUNKS + 1) * 16.0D);

            for (Animal animal : level.getEntitiesOfClass(Animal.class, pollArea)) {
                if (seenIds.add(animal.getId())) {
                    animals.add(animal);
                }
            }
        }
        return animals;
    }

    @Inject(method = "tickChunk", at = @At("HEAD"), cancellable = true)
    private static void coraline$skipWhenSpawnListEmpty(LevelChunk chunk, ServerLevel level, boolean spawnFriendlies,
                                                        CallbackInfo ci) {
        if (coraline$spawnArray.length == 0) {
            ci.cancel();
        }
    }

    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toArray(Ljava/util/function/IntFunction;)[Ljava/lang/Object;"), require = 0)
    private static Object[] coraline$useCachedSpawnArray(Stream<?> stream, IntFunction<Object[]> generator) {
        return coraline$spawnArray;
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner;isSpawnPositionOk(Lnet/minecraft/world/entity/SpawnPlacements$Type;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntityType;)Z",
                    remap = true
            ),
            remap = false,
            require = 0
    )
    private static boolean coraline$checkJitteredPosition(
            SpawnPlacements.Type placementType, LevelReader level, BlockPos origin, EntityType<?> entityType,
            Operation<Boolean> original, @Local(name = "x") int x, @Local(name = "y") int y, @Local(name = "z") int z) {
        return original.call(placementType, level, new BlockPos(x, y, z), entityType);
    }
}