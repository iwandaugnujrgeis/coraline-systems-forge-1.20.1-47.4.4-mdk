package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Powered variant of the Maglev Rail.
 *
 * Extends {@link PoweredRailBlock} with {@code isPoweredRail = true}
 * (isActivator = false), inheriting redstone-signal propagation, the
 * POWERED block-state property, and vanilla's built-in acceleration path in
 * {@code AbstractMinecart#moveAlongTrack} (the {@code instanceof
 * PoweredRailBlock} boost logic applies automatically since this class
 * extends it directly — no custom {@code onMinecartPass} override needed).
 *
 * Structurally cannot form curves: {@code PoweredRailBlock}'s constructor
 * hardcodes {@code super(true, arg)} (isStraight = true), which is
 * unreachable from this subclass. This mirrors vanilla Powered Rail, which
 * also cannot curve.
 *
 * Implements {@link IMaglevRail} so {@code AbstractMinecartMixin}'s
 * slope-correction guard applies to this block too. This must never be
 * omitted — its previous absence here was the exact cause of the "invisible
 * wall" bug where carts moving fast enough on lap 2+ were reversed the
 * instant they stepped from a flat/ascending MaglevRailBlock onto an
 * ascending PoweredMaglevRailBlock.
 */
public class PoweredMaglevRailBlock extends PoweredRailBlock implements IMaglevRail {

    public static final float MAGLEV_MAX_SPEED = 0.6f;

    public PoweredMaglevRailBlock(Properties properties) {
        super(properties, true); // true = isPoweredRail -> isActivator = false
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return MAGLEV_MAX_SPEED;
    }
}