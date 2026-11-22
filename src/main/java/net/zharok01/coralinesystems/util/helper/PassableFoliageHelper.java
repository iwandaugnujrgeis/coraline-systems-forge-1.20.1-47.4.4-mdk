package net.zharok01.coralinesystems.util.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zharok01.coralinesystems.block.PassableFoliageBlock;
import net.zharok01.coralinesystems.registry.CoralineTags;

public final class PassableFoliageHelper {

    public static final VoxelShape SUNKEN_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

    public static final float SLOWDOWN_FACTOR = 0.4f;

    private PassableFoliageHelper() {}

    public static boolean isPassable(BlockState state) {
        return ((PassableFoliageBlock) state.getBlock()).coraline$isPassable();
    }

    public static boolean hasLeafWalker(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS);
    }

    public static boolean isLeafPercher(LivingEntity entity) {
        return entity.getType().is(CoralineTags.LEAF_PERCHERS);
    }

    public static void slowSinkingEntity(BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity living) || !pos.equals(entity.blockPosition())) {
            return;
        }

        if (entity instanceof Player player && player.getAbilities().flying) {
            return;
        }

        if (hasLeafWalker(living) || isLeafPercher(living)) {
            return;
        }

        entity.setDeltaMovement(entity.getDeltaMovement().multiply(SLOWDOWN_FACTOR, 1.0, SLOWDOWN_FACTOR));
    }
}