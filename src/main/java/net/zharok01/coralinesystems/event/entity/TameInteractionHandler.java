package net.zharok01.coralinesystems.event.entity;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import einstein.subtle_effects.init.ModSounds;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.mixin.accessor.EntityGrizzlyBearAccessor;
import net.zharok01.coralinesystems.registry.CoralineTags;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TameInteractionHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (event.getTarget() instanceof EntityGrizzlyBear bear) {

            if (!bear.isHoneyed() && stack.is(CoralineTags.BEAR_TAMEABLES)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));

                if (!player.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);

                    bear.setHoneyed(true);
                    ((EntityGrizzlyBearAccessor) bear).coraline$setHoneyedTime(700);

                    bear.gameEvent(GameEvent.EAT, bear);
                    bear.playSound(ModSounds.TURTLE_EAT.get(), 1.0F, 1.0F);
                    bear.level().broadcastEntityEvent(bear, (byte) 7);
                }
                return;
            }

            if (bear.isHoneyed() && stack.isEmpty() && !bear.isVehicle() && !bear.isBaby() && !player.isShiftKeyDown()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));

                if (!player.level().isClientSide()) {
                    player.startRiding(bear);
                }
            }
        }
    }
}