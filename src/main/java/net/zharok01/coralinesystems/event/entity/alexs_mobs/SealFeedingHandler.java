package net.zharok01.coralinesystems.event.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntitySeal;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SealFeedingHandler {

    private static final float FEED_HEAL_AMOUNT = 10.0F;
    private static final int BOND_CHANCE_DENOMINATOR = 3;

    @SubscribeEvent
    public static void onSealInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof EntitySeal seal)) return;

        ItemStack stack = event.getItemStack();
        if (!stack.is(AMTagRegistry.SEAL_OFFERINGS) || seal.feederUUID != null) return;

        if (seal.isFood(stack) && seal.getAge() == 0 && seal.canFallInLove()) return;

        Level level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            Player player = event.getEntity();
            double particleX = seal.getX();
            double particleY = seal.getY() + seal.getBbHeight() * 0.75D;
            double particleZ = seal.getZ();

            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), particleX, particleY, particleZ, 8, 0.15D, 0.15D, 0.15D, 0.1D);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            seal.gameEvent(GameEvent.EAT);
            seal.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            seal.heal(FEED_HEAL_AMOUNT);

            if (seal.getRandom().nextInt(BOND_CHANCE_DENOMINATOR) == 0) {
                seal.feederUUID = player.getUUID();
                serverLevel.sendParticles(ParticleTypes.HEART, particleX, particleY, particleZ, 3, 0.25D, 0.25D, 0.25D, 0.0D);
            }
        }

        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        event.setCanceled(true);
    }
}