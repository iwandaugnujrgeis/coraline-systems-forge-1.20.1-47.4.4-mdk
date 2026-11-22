package net.zharok01.coralinesystems.mixin.entity.model.variant.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import net.zharok01.coralinesystems.mixin.entity.model.variant.CoralineVariantHooksMixin;
import net.zharok01.coralinesystems.client.entity.variant.CoralineMobVariant;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariantHolder;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieVariantMixin extends CoralineVariantHooksMixin implements CoralineVariantHolder {

    @Unique
    private static final EntityDataAccessor<String> coralineSystems$VARIANT =
            SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.STRING);

    @Override
    protected void coralineSystems$onDefineSynchedData(CallbackInfo ci) {
        ((Zombie) (Object) this).getEntityData().define(coralineSystems$VARIANT, CoralineVariants.DEFAULT_ID.toString());
    }

    @Override
    protected void coralineSystems$onAddAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putString("CoralineVariant", coralineSystems$getVariantId().toString());
    }

    @Override
    protected void coralineSystems$onReadAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("CoralineVariant")) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("CoralineVariant"));
            if (id != null) {
                coralineSystems$setVariant(CoralineVariants.getVariant(EntityType.ZOMBIE, id));
            }
        }
    }

    @Override
    protected void coralineSystems$onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CompoundTag dataTag, CallbackInfoReturnable<SpawnGroupData> cir) {
        coralineSystems$setVariant(CoralineVariants.getRandomVariant(EntityType.ZOMBIE, ((Zombie) (Object) this).getRandom()));
    }

    @Unique
    @Override
    public CoralineMobVariant coralineSystems$getVariant() {
        return CoralineVariants.getVariant(EntityType.ZOMBIE, coralineSystems$getVariantId());
    }

    @Unique
    @Override
    public void coralineSystems$setVariant(CoralineMobVariant variant) {
        ((Zombie) (Object) this).getEntityData().set(coralineSystems$VARIANT, variant.id().toString());
    }

    @Unique
    private ResourceLocation coralineSystems$getVariantId() {
        ResourceLocation id = ResourceLocation.tryParse(((Zombie) (Object) this).getEntityData().get(coralineSystems$VARIANT));
        return id != null ? id : CoralineVariants.DEFAULT_ID;
    }
}
