package net.zharok01.coralinesystems.event;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.mixin.accessors.EntityGrizzlyBearAccessor;
import net.zharok01.coralinesystems.registry.CoralineTags;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UniversalTameInteractionHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (event.getTarget() instanceof EntityGrizzlyBear bear) {

            // 1. Untamed: Right-Click to Tame
            if (!bear.isTame() && stack.is(CoralineTags.BEAR_TAMEABLES)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));

                if (!player.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);

                    if (bear.getRandom().nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(bear, player)) {
                        bear.tame(player);
                        bear.getNavigation().stop();
                        bear.setTarget(null);
                        bear.setCommand(2);
                        bear.setOrderedToSit(true);
                        bear.level().broadcastEntityEvent(bear, (byte) 7);
                    } else {
                        bear.level().broadcastEntityEvent(bear, (byte) 6);
                    }
                }
                return;
            }

            // 2. Tamed: Right-Click to Feed Honey (Enables Riding Overlay)
            if (bear.isTame() && stack.is(CoralineTags.BEAR_TAMEABLES)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));

                if (!player.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);

                    // Triggers the visual "Honey Smudged" AM overlay
                    bear.setHoneyed(true);

                    // Sets the timer for 35 seconds (700 ticks) of riding
                    ((EntityGrizzlyBearAccessor) bear).coraline$setHoneyedTime(700);

                    bear.gameEvent(GameEvent.EAT, bear);
                    bear.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                    bear.level().broadcastEntityEvent(bear, (byte) 7);
                }
                return;
            }

            // 3. Tamed: Right-Click Healing (Fishes, Meat, etc.)
            if (bear.isTame() && bear.getHealth() < bear.getMaxHealth() && stack.is(CoralineTags.BEAR_HEALABLES)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));

                if (!player.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);

                    float healAmount = stack.getFoodProperties(bear) != null ? Objects.requireNonNull(stack.getFoodProperties(bear)).getNutrition() : 4.0F;
                    bear.heal(healAmount);

                    bear.gameEvent(GameEvent.EAT, bear);
                    bear.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                    bear.level().broadcastEntityEvent(bear, (byte) 7);
                }
                // Removed `return;` as it's the end of the block anyway, but good practice.
            }
        }
    }
}