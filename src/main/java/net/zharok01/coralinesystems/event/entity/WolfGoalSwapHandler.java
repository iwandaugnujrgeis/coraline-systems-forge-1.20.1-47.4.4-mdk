package net.zharok01.coralinesystems.event.entity;

import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.ai.SprintingFollowOwnerGoal;
import net.zharok01.coralinesystems.entity.ai.WolfSurvivalPanicGoal;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WolfGoalSwapHandler {

    private static final int FIGHT_OR_FLIGHT_PRIORITY = 1;

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Wolf wolf)) return;

        WrappedGoal vanillaWrapped = wolf.goalSelector.getAvailableGoals()
                .stream()
                .filter(wrapped -> wrapped.getGoal() instanceof FollowOwnerGoal
                        && !(wrapped.getGoal() instanceof SprintingFollowOwnerGoal))
                .findFirst()
                .orElse(null);

        if (vanillaWrapped != null) {
            int priority = vanillaWrapped.getPriority();
            wolf.goalSelector.removeGoal(vanillaWrapped.getGoal());
            wolf.goalSelector.addGoal(priority, new SprintingFollowOwnerGoal(wolf));
        }

        boolean hasSurvivalGoal = wolf.goalSelector.getAvailableGoals()
                .stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof WolfSurvivalPanicGoal);

        if (!hasSurvivalGoal) {
            wolf.goalSelector.addGoal(FIGHT_OR_FLIGHT_PRIORITY, new WolfSurvivalPanicGoal(wolf, 10.0F, 40.0F, 1.3, 1.0));
        }
    }
}