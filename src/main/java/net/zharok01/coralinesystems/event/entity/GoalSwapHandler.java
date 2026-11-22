package net.zharok01.coralinesystems.event.entity;

import com.ninni.spawn.server.entity.Hamster;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.ai.SprintingFollowOwnerGoal;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GoalSwapHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (!(event.getEntity() instanceof TamableAnimal tamable)) return;

        boolean isTargetPet = false;
        boolean canFly = false;

        if (tamable instanceof Cat) {
            isTargetPet = true;

        } else if (tamable instanceof Parrot) {
            isTargetPet = true;
            canFly = true;

        } else if (tamable instanceof Hamster) {
            isTargetPet = true;
        }

        if (!isTargetPet) return;

        WrappedGoal vanillaWrapped = tamable.goalSelector.getAvailableGoals()
                .stream()
                .filter(wrapped -> wrapped.getGoal() instanceof FollowOwnerGoal
                        && !(wrapped.getGoal() instanceof SprintingFollowOwnerGoal))
                .findFirst()
                .orElse(null);

        if (vanillaWrapped != null) {
            int priority = vanillaWrapped.getPriority();
            tamable.goalSelector.removeGoal(vanillaWrapped.getGoal());
            tamable.goalSelector.addGoal(priority, new SprintingFollowOwnerGoal(tamable, canFly));
        }
    }
}
