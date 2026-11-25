package net.zharok01.coralinesystems.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineTags;
import net.zharok01.coralinesystems.util.data.StonePickaxeRegrowData;

import java.util.Iterator;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class StonePickaxeEvents {

    private static final int REGROW_EFFECT_DELAY_TICKS = 80;
    private static final int REGROW_RESTORE_DELAY_TICKS = 100;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

        StonePickaxeRegrowData data = StonePickaxeRegrowData.get(level);
        long now = level.getGameTime();

        Iterator<StonePickaxeRegrowData.Entry> it = data.entries().iterator();
        while (it.hasNext()) {
            StonePickaxeRegrowData.Entry entry = it.next();

            if (!entry.effectPlayed && now >= entry.effectTick) {
                playRegrowStartEffects(level, entry.pos);
                entry.effectPlayed = true;
                data.setDirty();
            }

            if (now >= entry.restoreTick && level.isLoaded(entry.pos)) {
                restoreBlock(level, entry.pos, entry.state);
                it.remove();
                data.setDirty();
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        final Player player = event.getPlayer();
        if (!player.getMainHandItem().is(Items.STONE_PICKAXE)) return;

        if (player.isCreative()) return;

        BlockState state = event.getState();

        if (!state.is(CoralineTags.RETURNABLE)) return;
        if (state.is(Blocks.BEDROCK) || state.isAir()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        final BlockPos pos = event.getPos().immutable();

        player.getMainHandItem().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));

        event.setCanceled(true);
        level.removeBlock(pos, false);

        level.sendParticles(CoralineParticles.DARK_SMOKE.get(),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                8, 0.2, 0.2, 0.2, 0.04);
        level.playSound(null, pos,
                CoralineSounds.STONE_PICKAXE_BLOCK_REMOVE.get(),
                SoundSource.BLOCKS, 1.0F,
                0.8F + level.getRandom().nextFloat() * 0.4F);

        growBack(level, pos, state);
    }

    private static void growBack(ServerLevel level, BlockPos pos, BlockState state) {
        long now = level.getGameTime();
        StonePickaxeRegrowData.get(level).schedule(pos, state, now + REGROW_EFFECT_DELAY_TICKS, now + REGROW_RESTORE_DELAY_TICKS);
    }

    private static void playRegrowStartEffects(ServerLevel level, BlockPos pos) {
        level.sendParticles(CoralineParticles.DARK_SMOKE.get(),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                30, 0.5, 0.5, 0.5, 0.25);
        level.sendParticles(CoralineParticles.DARK_SMOKE.get(),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                10, 0.35, 0.35, 0.35, 0.02);
        level.playSound(null, pos,
                CoralineSounds.STONE_PICKAXE_REGROW_START.get(),
                SoundSource.BLOCKS, 0.8F,
                1.1F + level.getRandom().nextFloat() * 0.3F);
    }

    private static void restoreBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos).isAir()) return;

        level.sendParticles(ParticleTypes.FLASH,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                1, 0, 0, 0, 0);

        level.setBlock(pos, state, 3);

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                50, 0.3, 0.3, 0.3, 0.18);

        level.playSound(null, pos,
                CoralineSounds.STONE_PICKAXE_BLOCK_RESTORE.get(),
                SoundSource.BLOCKS, 1.0F,
                0.7F + level.getRandom().nextFloat() * 0.3F);
    }

}