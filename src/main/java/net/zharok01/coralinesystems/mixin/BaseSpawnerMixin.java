package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin {

    @Shadow private int minSpawnDelay;
    @Shadow private int maxSpawnDelay;
    @Shadow private int spawnCount;
    @Shadow private int requiredPlayerRange;

    /**
     * Apply custom defaults once at load time, not every tick.
     * Only overrides delay/count if the spawner has vanilla (never customised) values —
     * respects spawners that were manually configured via NBT.
     */
    @Inject(method = "load", at = @At("RETURN"))
    private void coraline$applyCustomDefaults(Level level, BlockPos pos, CompoundTag tag, CallbackInfo ci) {
        // Expand activation range — Math.max respects manually set larger values
        this.requiredPlayerRange = Math.max(this.requiredPlayerRange, 32);

        // Only touch delays if the NBT didn't contain custom values
        if (!tag.contains("MinSpawnDelay", 99)) {
            this.minSpawnDelay = 100;
            this.maxSpawnDelay = 400;
            this.spawnCount = 6;
        }
    }

    /**
     * Bypass light level checks so spawners work in lit areas.
     */
    @Redirect(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/SpawnPlacements;checkSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z"
            )
    )
    private boolean coraline$bypassLightCheck(EntityType<?> entityType, ServerLevelAccessor serverLevel,
                                              MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return true;
    }
}