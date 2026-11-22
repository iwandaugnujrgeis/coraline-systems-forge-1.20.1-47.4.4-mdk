package net.zharok01.coralinesystems.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.client.entity.skin.PlayerSkinAttachments;
import net.zharok01.coralinesystems.client.entity.skin.SkinVariant;
import net.zharok01.coralinesystems.event.DualWieldEvents;
import net.zharok01.coralinesystems.registry.CoralineAnimationTypes;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Unique
    private static final int RESPAWN_SEARCH_RADIUS = 3;
    @Unique
    private static final int RESPAWN_SEARCH_DOWN = 2;
    @Unique
    private static final int RESPAWN_SEARCH_UP = 16;

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void coraline$overrideHurtSound(DamageSource damageSource, CallbackInfoReturnable<SoundEvent> cir) {
        if (this.level().isClientSide) return;
        SkinVariant variant = PlayerSkinAttachments.getVariant((ServerPlayer) (Object) this);
        if (variant == SkinVariant.STEVE || variant == SkinVariant.GREEN_STEVE) {
            cir.setReturnValue(CoralineSounds.STEVE_HURT.get());
        }
    }

    @ModifyVariable(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z"), ordinal = 0, argsOnly = true)
    private float coraline$onSwordBlocking(float damage, DamageSource source) {
        Player player = (Player) (Object) this;
        if (!player.isInvulnerableTo(source) && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            if (this.coraline$isSwordBlocking() && damage > 0) {
                damage = (1F + damage) * 0.5F;
                if (DualWieldEvents.coraline$isDualWieldingSwords(player)) {
                    damage = (1F + damage) * 0.5F;
                }
            }
        }
        return damage;
    }
    @ModifyVariable(method = "getDigSpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)F",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffectUtil;hasDigSpeed(Lnet/minecraft/world/entity/LivingEntity;)Z"),
            ordinal = 0)

    private float coraline$combineToolMiningSpeed(float speed, BlockState state, BlockPos pos) {
        Player player = (Player) (Object) this;
        if (!DualWieldEvents.coraline$isDualWielding(player)) {
            return speed;
        }
        ItemStack offhandItem = player.getOffhandItem();
        float offhandSpeed = offhandItem.getDestroySpeed(state);
        if (offhandSpeed > 1.0F) {
            speed += offhandSpeed - 1.0F;
            int offhandEfficiency = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, offhandItem);
            if (offhandEfficiency > 0) {
                speed += (float) (offhandEfficiency * offhandEfficiency + 1);
            }
        }
        return speed;
    }
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"))

    private float coraline$combineOffhandEnchantmentDamage(ItemStack itemStack, MobType mobType,
            Operation<Float> original) {
        float damageBonus = original.call(itemStack, mobType);
        Player player = (Player) (Object) this;
        if (DualWieldEvents.coraline$isDualWielding(player)) {
            damageBonus += original.call(player.getOffhandItem(), mobType);
        }
        return damageBonus;
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void coraline$damageSwordWhenBlocking(DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (cir.getReturnValue() && amount > 0.0F && !source.is(DamageTypeTags.BYPASSES_ARMOR)
                && this.coraline$isSwordBlocking()) {
            InteractionHand blockingHand = this.getUsedItemHand();
            boolean dualWieldingSwords = DualWieldEvents.coraline$isDualWieldingSwords(player);
            DualWieldEvents.coraline$damageItemInHand(player, blockingHand, 1);
            if (dualWieldingSwords) {
                DualWieldEvents.coraline$damageItemInHand(player,
                        blockingHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, 1);
            }
        }
    }

    @Unique
    private boolean coraline$isSwordBlocking() {
        if (this.isUsingItem()) {
            ItemStack activeItem = this.getUseItem();
            if (!activeItem.isEmpty()) {
                return activeItem.getItem().getUseAnimation(activeItem) == CoralineAnimationTypes.SWORD_BLOCK;
            }
        }
        return false;
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("RETURN"), cancellable = true)
    private static void coraline$keepForcedRespawn(ServerLevel serverLevel, BlockPos spawnBlockPos, float playerOrientation, boolean isRespawnForced, boolean respawnAfterWinningTheGame, CallbackInfoReturnable<Optional<Vec3>> cir) {
        if (!isRespawnForced || cir.getReturnValue().isPresent()) return;
        BlockPos.MutableBlockPos feet = new BlockPos.MutableBlockPos();
        for (int radius = 0; radius <= RESPAWN_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    for (int y = spawnBlockPos.getY() - RESPAWN_SEARCH_DOWN; y <= spawnBlockPos.getY() + RESPAWN_SEARCH_UP; y++) {
                        feet.set(spawnBlockPos.getX() + dx, y, spawnBlockPos.getZ() + dz);
                        if (coraline$isSafeSpot(serverLevel, feet)) {
                            cir.setReturnValue(Optional.of(new Vec3(feet.getX() + 0.5, feet.getY() + 0.1, feet.getZ() + 0.5)));
                            return;
                        }
                    }
                }
            }
        }
    }

    @Unique
    private static boolean coraline$isSafeSpot(ServerLevel level, BlockPos feetPos) {
        BlockState feet = level.getBlockState(feetPos);
        BlockState head = level.getBlockState(feetPos.above());
        BlockState floor = level.getBlockState(feetPos.below());
        boolean roomToStand = feet.getCollisionShape(level, feetPos).isEmpty()
                && feet.getFluidState().isEmpty()
                && head.getCollisionShape(level, feetPos.above()).isEmpty()
                && head.getFluidState().isEmpty();
        boolean hasFloor = !floor.getCollisionShape(level, feetPos.below()).isEmpty()
                && !floor.getFluidState().is(FluidTags.LAVA);
        return roomToStand && hasFloor;
    }

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void coraline$startGlidingWhileLevitating(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!player.onGround() && !player.isFallFlying() && !player.isInWater() && player.hasEffect(MobEffects.LEVITATION)) {
            player.startFallFlying();
            cir.setReturnValue(true);
        }
    }
}
