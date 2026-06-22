package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredMaglevRailBlock extends PoweredRailBlock {

    public static final float MAGLEV_MAX_SPEED = 0.8f;

    public PoweredMaglevRailBlock(Properties properties) {
        super(properties, true); // true = isPoweredRail → isActivator = false
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return MAGLEV_MAX_SPEED;
    }
}