package net.zharok01.coralinesystems.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.zharok01.coralinesystems.event.InstrumentNameOverlay;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

public class TuningForkItem extends Item {

    private static final List<NoteBlockInstrument> CYCLE_INSTRUMENTS = Arrays.stream(NoteBlockInstrument.values())
            .filter(NoteBlockInstrument::isTunable)
            .toList();

    public TuningForkItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getAttributeModifiers(slot, stack);
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers = ImmutableMultimap.builder();
        modifiers.putAll(super.getAttributeModifiers(slot, stack));
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 1.0, AttributeModifier.Operation.ADDITION));
        modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4, AttributeModifier.Operation.ADDITION));
        return modifiers.build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof Player player) || !player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide && !state.is(BlockTags.FIRE)
                && (!(miningEntity instanceof Player player) || !player.getAbilities().instabuild)) {
            stack.hurtAndBreak(1, miningEntity, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return super.mineBlock(stack, level, state, pos, miningEntity);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(Blocks.NOTE_BLOCK)) {
            return InteractionResult.PASS;
        }

        NoteBlockInstrument current = state.getValue(NoteBlock.INSTRUMENT);

        if (current.worksAboveNoteBlock()) {
            return InteractionResult.PASS;
        }

        int nextIndex = (CYCLE_INSTRUMENTS.indexOf(current) + 1) % CYCLE_INSTRUMENTS.size();
        NoteBlockInstrument next = CYCLE_INSTRUMENTS.get(nextIndex);

        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstrumentNameOverlay.show(next));
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        level.setBlock(pos, state.setValue(NoteBlock.INSTRUMENT, next), 3);
        level.blockEvent(pos, state.getBlock(), 0, 0);
        level.gameEvent(player, GameEvent.NOTE_BLOCK_PLAY, pos);

        if (player != null && !player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(context.getHand()));
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(Stats.TUNE_NOTEBLOCK);
            CoralineTriggers.TUNE_NOTE_BLOCK.trigger(serverPlayer);
        }
        return InteractionResult.CONSUME;
    }
}