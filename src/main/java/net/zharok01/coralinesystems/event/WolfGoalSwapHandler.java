package net.zharok01.coralinesystems.event;

import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.ai.SprintingFollowOwnerGoal;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WolfGoalSwapHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Server-side only — goals are server logic
        if (event.getLevel().isClientSide()) return;

        if (!(event.getEntity() instanceof Wolf wolf)) return;

        // Find the vanilla FollowOwnerGoal entry in the goal selector
        WrappedGoal vanillaWrapped = wolf.goalSelector.getAvailableGoals()
                .stream()
                .filter(wrapped -> wrapped.getGoal() instanceof FollowOwnerGoal)
                .findFirst()
                .orElse(null);

        if (vanillaWrapped == null) return;

        int priority = vanillaWrapped.getPriority(); // preserve vanilla priority (6)

        // Remove the vanilla goal
        wolf.goalSelector.removeGoal(vanillaWrapped.getGoal());

        // Add our sprinting replacement at the same priority
        wolf.goalSelector.addGoal(priority, new SprintingFollowOwnerGoal(wolf));
    }
}