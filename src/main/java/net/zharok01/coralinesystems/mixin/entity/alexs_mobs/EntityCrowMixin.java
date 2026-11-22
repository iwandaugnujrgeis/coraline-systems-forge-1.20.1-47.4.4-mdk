package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = EntityCrow.class, remap = false)
public class EntityCrowMixin {

    @Shadow private UUID seedThrowerID;

    @Unique private static final float FOOD_HEAL_AMOUNT = 4.0F;
    @Unique private static final int TAME_CHANCE_DENOMINATOR = 3;
    @Unique private static final int COMMAND_FOLLOW = 1;
    @Unique private static final int COMMAND_COLLECT = 3;

    @Inject(method = "m_6071_", at = @At("HEAD"), cancellable = true)
    private void coraline$simpleFeeding(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        EntityCrow crow = (EntityCrow) (Object) this;
        ItemStack stack = player.getItemInHand(hand);
        boolean clientSide = crow.level().isClientSide();

        if (!crow.isTame() && stack.is(AMTagRegistry.CROW_TAMEABLES)) {
            if (!clientSide) {
                this.coraline$consumeFeedItem(player, stack, crow);
                if (crow.getRandom().nextInt(TAME_CHANCE_DENOMINATOR) == 0) {
                    crow.tame(player);
                    crow.setCommand(COMMAND_FOLLOW);
                    crow.level().broadcastEntityEvent(crow, (byte) 7);
                } else {
                    crow.level().broadcastEntityEvent(crow, (byte) 6);
                }
            }
            cir.setReturnValue(InteractionResult.sidedSuccess(clientSide));
        } else if (crow.isTame()
                && !crow.isFood(stack)
                && (stack.getItem().isEdible() || stack.is(AMTagRegistry.CROW_FOODSTUFFS))
                && crow.getHealth() < crow.getMaxHealth()) {
            if (crow.level() instanceof ServerLevel serverLevel) {
                double particleX = crow.getX();
                double particleY = crow.getY() + crow.getBbHeight() * 0.75D;
                double particleZ = crow.getZ();

                serverLevel.sendParticles(ParticleTypes.HEART, particleX, particleY, particleZ, 3, 0.25D, 0.25D, 0.25D, 0.0D);
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), particleX, particleY, particleZ, 8, 0.15D, 0.15D, 0.15D, 0.1D);

                this.coraline$consumeFeedItem(player, stack, crow);
                crow.heal(FOOD_HEAL_AMOUNT);
            }
            cir.setReturnValue(InteractionResult.sidedSuccess(clientSide));
        }
    }

    @Inject(method = "onGetItem", at = @At("TAIL"))
    private void coraline$preventDropTaming(ItemEntity targetEntity, CallbackInfo ci) {
        this.seedThrowerID = null;
    }

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntityCrow;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/entity/EntityCrow;setCommand(I)V")
    )
    private void coraline$cycleFollowAndCollectOnly(EntityCrow instance, int command) {
        if (command == 2) {
            instance.setCommand(COMMAND_COLLECT);
        } else if (command == 0 || command == 4) {
            instance.setCommand(COMMAND_FOLLOW);
        } else {
            instance.setCommand(command);
        }
    }

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntityCrow;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;m_5661_(Lnet/minecraft/network/chat/Component;Z)V")
    )
    private void coraline$hideModeMessage(Player instance, Component message, boolean overlay) {}

    @Inject(method = "m_8119_", at = @At("TAIL"))
    private void coraline$migrateRemovedModes(CallbackInfo ci) {
        EntityCrow crow = (EntityCrow) (Object) this;
        if (crow.level().isClientSide() || !crow.isTame()) return;

        int command = crow.getCommand();
        if (command == 0 || command == 2) {
            crow.setCommand(command == 0 ? COMMAND_FOLLOW : COMMAND_COLLECT);
            crow.setOrderedToSit(false);
        }
    }

    @Unique
    private void coraline$consumeFeedItem(Player player, ItemStack stack, EntityCrow crow) {
        if (!player.getAbilities().instabuild) {
            if (stack.hasCraftingRemainingItem()) {
                crow.spawnAtLocation(stack.getCraftingRemainingItem());
            }
            stack.shrink(1);
        }
        crow.gameEvent(GameEvent.EAT);
        crow.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
    }

    @Inject(method = "canTargetItem", at = @At("HEAD"), cancellable = true)
    private void coraline$onlyScavengeWhenCollecting(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        EntityCrow crow = (EntityCrow) (Object) this;
        cir.setReturnValue(crow.isTame() && crow.getCommand() == COMMAND_COLLECT);
    }
}