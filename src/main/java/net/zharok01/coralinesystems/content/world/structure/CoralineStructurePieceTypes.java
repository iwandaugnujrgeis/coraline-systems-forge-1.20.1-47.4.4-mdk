package net.zharok01.coralinesystems.content.world.structure;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.zharok01.coralinesystems.CoralineSystems;

/**
 * Registers all Forgotten Dungeon piece types into the vanilla
 * BuiltInRegistries.STRUCTURE_PIECE registry.
 *
 * Call CoralineStructurePieceTypes.register() from your mod's CommonSetup
 * (or directly in the mod constructor before world loading begins).
 */
public class CoralineStructurePieceTypes {

    public static StructurePieceType FORGOTTEN_START_ROOM;
    public static StructurePieceType FORGOTTEN_CORRIDOR;
    public static StructurePieceType FORGOTTEN_WIDE_CORRIDOR;
    public static StructurePieceType FORGOTTEN_TURN_LEFT;
    public static StructurePieceType FORGOTTEN_TURN_RIGHT;
    public static StructurePieceType FORGOTTEN_CROSSROAD;
    public static StructurePieceType FORGOTTEN_SMALL_ROOM;
    public static StructurePieceType FORGOTTEN_LARGE_HALL;
    public static StructurePieceType FORGOTTEN_SPAWNER_ROOM;
    public static StructurePieceType FORGOTTEN_TREASURE_ROOM;
    public static StructurePieceType FORGOTTEN_PRISON_BLOCK;
    public static StructurePieceType FORGOTTEN_STAIRCASE_DOWN;
    public static StructurePieceType FORGOTTEN_STAIRCASE_UP;

    public static void register() {
        FORGOTTEN_START_ROOM     = reg("forgotten_start_room",     ForgottenPieces.StartRoom::new);
        FORGOTTEN_CORRIDOR       = reg("forgotten_corridor",       ForgottenPieces.Corridor::new);
        FORGOTTEN_WIDE_CORRIDOR  = reg("forgotten_wide_corridor",  ForgottenPieces.WideCorridor::new);
        FORGOTTEN_TURN_LEFT      = reg("forgotten_turn_left",      ForgottenPieces.TurnLeft::new);
        FORGOTTEN_TURN_RIGHT     = reg("forgotten_turn_right",     ForgottenPieces.TurnRight::new);
        FORGOTTEN_CROSSROAD      = reg("forgotten_crossroad",      ForgottenPieces.CrossroadRoom::new);
        FORGOTTEN_SMALL_ROOM     = reg("forgotten_small_room",     ForgottenPieces.SmallRoom::new);
        FORGOTTEN_LARGE_HALL     = reg("forgotten_large_hall",     ForgottenPieces.LargeHall::new);
        FORGOTTEN_SPAWNER_ROOM   = reg("forgotten_spawner_room",   ForgottenPieces.SpawnerRoom::new);
        FORGOTTEN_TREASURE_ROOM  = reg("forgotten_treasure_room",  ForgottenPieces.TreasureRoom::new);
        FORGOTTEN_PRISON_BLOCK   = reg("forgotten_prison_block",   ForgottenPieces.PrisonBlock::new);
        FORGOTTEN_STAIRCASE_DOWN = reg("forgotten_staircase_down", ForgottenPieces.StaircaseDown::new);
        FORGOTTEN_STAIRCASE_UP   = reg("forgotten_staircase_up",   ForgottenPieces.StaircaseUp::new);
    }

    private static StructurePieceType reg(String id, StructurePieceType.ContextlessType type) {
        return Registry.register(BuiltInRegistries.STRUCTURE_PIECE, CoralineSystems.of(id), type);
    }
}
