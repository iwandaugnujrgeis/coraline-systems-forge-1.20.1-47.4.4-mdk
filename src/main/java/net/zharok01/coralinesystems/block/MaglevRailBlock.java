package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.NotNull;

/**
 * Maglev Rail — a curve-capable rail that allows carts to travel at twice
 * vanilla's maximum speed (0.8 m/tick vs 0.4 m/tick).
 *
 * The speed is advertised via {@link #getRailMaxSpeed}, the Forge hook
 * consumed by {@code AbstractMinecart#getMaxSpeedWithRail()}. A companion
 * Mixin on {@code AbstractMinecart} (see {@code AbstractMinecartMixin})
 * guards the slope-energy correction in {@code moveAlongTrack} for any block
 * implementing {@link IMaglevRail}; without that guard, carts moving fast
 * enough on an ascending maglev rail get their velocity direction flipped
 * by vanilla's own math and are thrown backward down the slope.
 *
 * Unlike {@code PoweredRailBlock}/{@code CopperRailBlock}, this rail uses
 * {@code isStraight = false} and the full {@code RAIL_SHAPE} property (not
 * {@code RAIL_SHAPE_STRAIGHT}), so it CAN form curves — confirmed against
 * {@code RailState.java}, where curve-shape assignment
 * ({@code SOUTH_EAST}/{@code SOUTH_WEST}/{@code NORTH_WEST}/{@code NORTH_EAST})
 * is gated behind {@code if (!this.isStraight)}.
 */
public class MaglevRailBlock extends BaseRailBlock implements IMaglevRail {

    /** Full shape set (straight + curves), matching vanilla RailBlock. */
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

    /** Double vanilla's 0.4 f/tick rail cap. */
    public static final float MAGLEV_MAX_SPEED = 0.6f;

    public MaglevRailBlock(BlockBehaviour.Properties properties) {
        super(false, properties); // isStraight = false -> curves allowed
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(SHAPE, RailShape.NORTH_SOUTH)
                        .setValue(WATERLOGGED, false)
        );
    }

    // -------------------------------------------------------------------------
    // Forge speed hook
    // -------------------------------------------------------------------------

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return MAGLEV_MAX_SPEED;
    }

    // -------------------------------------------------------------------------
    // Shape property (required abstract)
    // -------------------------------------------------------------------------

    @Override
    public @NotNull Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    // -------------------------------------------------------------------------
    // State definition
    // -------------------------------------------------------------------------

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
    }

    // -------------------------------------------------------------------------
    // Rotate / mirror — full curve-aware mapping, copied from vanilla RailBlock
    // -------------------------------------------------------------------------

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180:
                switch (state.getValue(SHAPE)) {
                    case ASCENDING_EAST:  return state.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_WEST:  return state.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    case ASCENDING_NORTH: return state.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_SOUTH: return state.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    case SOUTH_EAST:      return state.setValue(SHAPE, RailShape.NORTH_WEST);
                    case SOUTH_WEST:      return state.setValue(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_WEST:      return state.setValue(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_EAST:      return state.setValue(SHAPE, RailShape.SOUTH_WEST);
                    default:              return state; // NORTH_SOUTH, EAST_WEST
                }
            case COUNTERCLOCKWISE_90:
                switch (state.getValue(SHAPE)) {
                    case ASCENDING_EAST:  return state.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    case ASCENDING_WEST:  return state.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_NORTH: return state.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_SOUTH: return state.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    case SOUTH_EAST:      return state.setValue(SHAPE, RailShape.NORTH_EAST);
                    case SOUTH_WEST:      return state.setValue(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_WEST:      return state.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case NORTH_EAST:      return state.setValue(SHAPE, RailShape.NORTH_WEST);
                    case NORTH_SOUTH:     return state.setValue(SHAPE, RailShape.EAST_WEST);
                    case EAST_WEST:       return state.setValue(SHAPE, RailShape.NORTH_SOUTH);
                }
            case CLOCKWISE_90:
                switch (state.getValue(SHAPE)) {
                    case ASCENDING_EAST:  return state.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_WEST:  return state.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    case ASCENDING_NORTH: return state.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    case ASCENDING_SOUTH: return state.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    case SOUTH_EAST:      return state.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case SOUTH_WEST:      return state.setValue(SHAPE, RailShape.NORTH_WEST);
                    case NORTH_WEST:      return state.setValue(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_EAST:      return state.setValue(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_SOUTH:     return state.setValue(SHAPE, RailShape.EAST_WEST);
                    case EAST_WEST:       return state.setValue(SHAPE, RailShape.NORTH_SOUTH);
                }
            default:
                return state;
        }
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        RailShape shape = state.getValue(SHAPE);
        switch (mirror) {
            case LEFT_RIGHT:
                switch (shape) {
                    case ASCENDING_NORTH: return state.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_SOUTH: return state.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    case SOUTH_EAST:      return state.setValue(SHAPE, RailShape.NORTH_EAST);
                    case SOUTH_WEST:      return state.setValue(SHAPE, RailShape.NORTH_WEST);
                    case NORTH_WEST:      return state.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case NORTH_EAST:      return state.setValue(SHAPE, RailShape.SOUTH_EAST);
                    default:              return super.mirror(state, mirror);
                }
            case FRONT_BACK:
                switch (shape) {
                    case ASCENDING_EAST: return state.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_WEST: return state.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    case SOUTH_EAST:     return state.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case SOUTH_WEST:     return state.setValue(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_WEST:     return state.setValue(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_EAST:     return state.setValue(SHAPE, RailShape.NORTH_WEST);
                    default:             return super.mirror(state, mirror);
                }
            default:
                return super.mirror(state, mirror);
        }
    }
}