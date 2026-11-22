package net.zharok01.coralinesystems.mixin.entity.model.variant;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class CoralineVariantHooksMixin {

    @Inject(method = "defineSynchedData", at = @At("RETURN"))
    protected void coralineSystems$onDefineSynchedData(CallbackInfo ci) {
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    protected void coralineSystems$onAddAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    protected void coralineSystems$onReadAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    protected void coralineSystems$onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CompoundTag dataTag, CallbackInfoReturnable<SpawnGroupData> cir) {
    }
}
