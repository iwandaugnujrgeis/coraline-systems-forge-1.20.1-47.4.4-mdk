package net.zharok01.coralinesystems.event;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
// import net.minecraft.world.entity.item.FallingBlockEntity; // Used by skyDrop — uncomment alongside it
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineTags;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class StonePickaxeEvents {

    // ---------------------------------------------------------------------------
    // Tiny tick-based scheduler — lets us fire actions N ticks in the future
    // without any extra infrastructure. All access is on the server thread.
    // ---------------------------------------------------------------------------
    private record ScheduledTask(long targetTick, ServerLevel level, Runnable action) {}
    private static final List<ScheduledTask> PENDING_TASKS = new ArrayList<>();

    private static void schedule(ServerLevel level, int delayTicks, Runnable action) {
        PENDING_TASKS.add(new ScheduledTask(level.getGameTime() + delayTicks, level, action));
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        // Fast path: nothing scheduled → skip the iterator entirely.
        // This fires every tick for every loaded dimension, so the early exit matters.
        if (PENDING_TASKS.isEmpty()) return;

        long now = level.getGameTime();
        Iterator<ScheduledTask> it = PENDING_TASKS.iterator();
        while (it.hasNext()) {
            ScheduledTask task = it.next();
            if (task.level() == level && now >= task.targetTick()) {
                it.remove();
                task.action().run();
            }
        }
    }

    // Clean up dangling tasks if a world unloads mid-game (e.g. /reload, server stop)
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PENDING_TASKS.removeIf(task -> task.level() == level);
        }
    }

    // ---------------------------------------------------------------------------
    // Main event
    // ---------------------------------------------------------------------------
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        final Player player = event.getPlayer();
        if (!player.getMainHandItem().is(Items.STONE_PICKAXE)) return; //TODO: Check off-hand?

        // Creative players break blocks normally — this effect is for survival only,
        // and you don't want to lock a map-maker out of their own secret rooms.
        if (player.isCreative()) return;

        BlockState state = event.getState();

        // Guard: only trigger on stone-type blocks.
        // Swap BlockTags.BASE_STONE_OVERWORLD for your own custom tag if you want
        // tighter control over which blocks are affected.
        if (!state.is(CoralineTags.RETURNABLE)) return;
        if (state.is(Blocks.BEDROCK) || state.isAir()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // Capture an immutable copy — the original BlockPos may be reused by the engine
        final BlockPos pos = event.getPos().immutable();

        //TEST:
        player.getMainHandItem().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));

        // Cancel vanilla break: no drops, no XP, no sound from the engine
        event.setCanceled(true);
        level.removeBlock(pos, false);

        // Shared vanish: quick poof + your custom sound at the break spot
        level.sendParticles(AMParticleRegistry.STATIC_SPARK.get(),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                8, 0.2, 0.2, 0.2, 0.04);
        level.playSound(null, pos,
                CoralineSounds.MONSTER_VANISH.get(),
                SoundSource.BLOCKS, 1.0F,
                0.8F + level.getRandom().nextFloat() * 0.4F);

        growBack(level, pos, state);
        // skyDrop(level, pos, state, player); // Uncomment to re-enable the Sky Drop effect
    }

    // ---------------------------------------------------------------------------
    // Effect — GROW BACK
    //
    // The block vanishes, then slowly re-materialises on the exact same spot.
    // Two-phase animation:
    //   t+80 ticks  (~4 s) — Enchant + END_ROD particles spiral in, hinting
    //                         something is about to happen.
    //   t+100 ticks (~5 s) — FLASH burst, block placed, outward block-crumble
    //                         shower + END_ROD sparks give the "grew out of thin
    //                         air" impression. Capped with a magical sound.
    //
    // The 5-second window is long enough for a player to step through a hidden
    // passage before the block seals it behind them.
    // ---------------------------------------------------------------------------
    private static void growBack(ServerLevel level, BlockPos pos, BlockState state) {

        // Phase 1 — "charging" particles: enchant glyphs + faint END_ROD glow
        schedule(level, 80, () -> {
            level.sendParticles(AMParticleRegistry.STATIC_SPARK.get(),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    30, 0.5, 0.5, 0.5, 0.25);
            level.sendParticles(AMParticleRegistry.STATIC_SPARK.get(),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    10, 0.35, 0.35, 0.35, 0.02);
            level.playSound(null, pos,
                    CoralineSounds.STATIC_PORTAL_OPEN.get(),
                    SoundSource.BLOCKS, 0.8F,
                    1.1F + level.getRandom().nextFloat() * 0.3F);
        });

        // Phase 2 — materialisation: flash → block placed → outward burst
        schedule(level, 100, () -> {
            // If something else filled the spot in the meantime, bail gracefully
            if (!level.getBlockState(pos).isAir()) return;

            // Bright flash first — this is the "pop into existence" visual centrepiece
            level.sendParticles(ParticleTypes.FLASH,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    1, 0, 0, 0, 0);

            // Place the block
            level.setBlock(pos, state, 3);

            // Outward crumble + sparkle shower — simulates the block "expanding"
            // outward from nothing (heavier count than the vanish gives it weight)
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    50, 0.3, 0.3, 0.3, 0.18);
            /*
            level.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    18, 0.45, 0.45, 0.45, 0.06);
            */

            // Magical reappearance sound
            level.playSound(null, pos,
                    SoundEvents.STONE_PLACE,
                    SoundSource.BLOCKS, 1.0F,
                    0.7F + level.getRandom().nextFloat() * 0.3F);
        });
    }

    // ---------------------------------------------------------------------------
    // Effect 2 — SKY DROP (commented out — re-enable when needed)
    //
    // The block disappears from the break spot *instantly*, then reappears as a
    // FallingBlockEntity spawned ~25 blocks directly above the player.
    // It falls straight down with no horizontal drift — clean, inevitable.
    //
    // To re-enable:
    //   1. Uncomment this method.
    //   2. Uncomment the FallingBlockEntity import above.
    //   3. Replace the growBack() call in onBlockBreak with a random branch,
    //      or call skyDrop() directly — your choice.
    // ---------------------------------------------------------------------------
    /*
    private static void skyDrop(ServerLevel level, BlockPos breakPos, BlockState state, Player player) {
        // Clamp spawn height so we never exceed the world's build limit
        double spawnX = player.getX();
        double spawnY = Math.min(player.getY() + 25.0, level.getMaxBuildHeight() - 2);
        double spawnZ = player.getZ();

        // fall() creates the entity at the sky position, sets that block to AIR
        // (already air up there — no-op), and adds it to the level for us.
        FallingBlockEntity falling = FallingBlockEntity.fall(
                level, BlockPos.containing(spawnX, spawnY, spawnZ), state);

        // Reposition precisely over the player (fall() snaps to block-centre X/Z)
        falling.setPos(spawnX, spawnY, spawnZ);

        // Zero horizontal movement — falls vertically, straight at the player's head
        falling.setDeltaMovement(0, 0, 0);
        falling.time = 1;
        falling.dropItem = false; // no item drop on landing — block just settles in place

        // Cloud burst at the sky-spawn point so players eventually learn to look up
        level.sendParticles(ParticleTypes.CLOUD,
                spawnX, spawnY, spawnZ,
                14, 0.3, 0.3, 0.3, 0.05);

        // Small poof at the break spot too, so the block's origin is clearly "gone"
        level.sendParticles(ParticleTypes.POOF,
                breakPos.getX() + 0.5, breakPos.getY() + 0.5, breakPos.getZ() + 0.5,
                5, 0.15, 0.15, 0.15, 0.03);
    }
    */
}