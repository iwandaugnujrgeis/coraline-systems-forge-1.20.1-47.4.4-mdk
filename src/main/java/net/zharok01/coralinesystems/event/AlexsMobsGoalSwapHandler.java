package net.zharok01.coralinesystems.event;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.ai.GrizzlyBearProtectCubGoal;
import net.zharok01.coralinesystems.entity.ai.SprintingFollowOwnerGoal;

import java.util.List;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AlexsMobsGoalSwapHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof EntityGrizzlyBear bear) {

            // 1. Remove Unprovoked Attack & Item Foraging Goals
            List<WrappedGoal> goalsToRemove = bear.targetSelector.getAvailableGoals().stream()
                    .filter(wrapped -> {
                        String name = wrapped.getGoal().getClass().getSimpleName();
                        return name.equals("AttackPlayerGoal") || name.equals("CreatureAITargetItems");
                    })
                    .toList();

            goalsToRemove.forEach(wrapped -> bear.targetSelector.removeGoal(wrapped.getGoal()));

            // 2. Add Vanilla Polar Bear style "Protect the Cubs" Goal
            bear.targetSelector.addGoal(3, new GrizzlyBearProtectCubGoal(bear));

            // 3. Swap AM's FollowOwnerGoal for our Custom Sprinting & Teleportation Goal
            WrappedGoal amFollowOwner = bear.goalSelector.getAvailableGoals().stream()
                    .filter(wrapped -> wrapped.getGoal().getClass().getSimpleName().equals("TameableAIFollowOwner"))
                    .findFirst()
                    .orElse(null);

            if (amFollowOwner != null) {
                int priority = amFollowOwner.getPriority();
                bear.goalSelector.removeGoal(amFollowOwner.getGoal());
                bear.goalSelector.addGoal(priority, new SprintingFollowOwnerGoal(bear));
            }
        }
    }
}