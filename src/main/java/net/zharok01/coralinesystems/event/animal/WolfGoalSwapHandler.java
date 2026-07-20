package net.zharok01.coralinesystems.event.animal;

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

    /**
     * Priority for the fight-or-flight goals. Must be lower (higher
     * priority) than SitWhenOrderedToGoal (2) and SprintingFollowOwnerGoal
     * (6) so GoalSelector's flag-locking lets flee/roam preempt them.
     * Shares tier 1 with vanilla FloatGoal/WolfPanicGoal — both of those
     * are unrelated flag sets (FloatGoal has no MOVE flag conflict here
     * in a way that matters) and firing alongside panic is fine, since
     * "on fire and also fighting for its life" should still let it flee.
     */
    private static final int FIGHT_OR_FLIGHT_PRIORITY = 1;

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Server-side only — goals are server logic
        if (event.getLevel().isClientSide()) return;

        if (!(event.getEntity() instanceof Wolf wolf)) return;

        // Find the vanilla FollowOwnerGoal entry in the animal selector
        WrappedGoal vanillaWrapped = wolf.goalSelector.getAvailableGoals()
                .stream()
                .filter(wrapped -> wrapped.getGoal() instanceof FollowOwnerGoal)
                .findFirst()
                .orElse(null);

        if (vanillaWrapped != null) {
            int priority = vanillaWrapped.getPriority(); // preserve vanilla priority (6)

            // Remove the vanilla animal
            wolf.goalSelector.removeGoal(vanillaWrapped.getGoal());

            // Add our sprinting replacement at the same priority
            wolf.goalSelector.addGoal(priority, new SprintingFollowOwnerGoal(wolf));
        }

        // WolfSurvivalPanicGoal(wolf, healthThreshold, safeDistance, fleeSpeed, roamSpeed)
        // HP threshold: 10.0F (5 hearts). Safe distance: 40.0F (Beats Zombie tracking).
        wolf.goalSelector.addGoal(FIGHT_OR_FLIGHT_PRIORITY, new WolfSurvivalPanicGoal(wolf, 10.0F, 40.0F, 1.3, 1.0));
    }
}