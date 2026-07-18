package net.zharok01.coralinesystems.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.block.BrewingCauldronBlock;
import net.zharok01.coralinesystems.client.particle.CauldronSplashParticle;

import java.util.function.Supplier;

/**
 * Sent server → client when an entity enters the fluid surface of a
 * BrewingCauldronBlock.  Carries the cauldron position and the packed
 * RGB tint color so the client can spawn correctly-colored splash
 * particles without needing its own block-entity lookup.
 *
 * Wire layout (5 bytes total):
 *   BlockPos   — long (8 bytes, via FriendlyByteBuf#writeBlockPos)
 *   color RGB  — int  (4 bytes)
 *
 * Registered in CoralinePacketHandler as PLAY_TO_CLIENT.
 */
public class CauldronSplashPacket {

    private final BlockPos pos;
    private final int color;

    public CauldronSplashPacket(BlockPos pos, int color) {
        this.pos = pos;
        this.color = color;
    }

    // ── Codec ────────────────────────────────────────────────────────────────

    public static void encode(CauldronSplashPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.color);
    }

    public static CauldronSplashPacket decode(FriendlyByteBuf buf) {
        BlockPos pos   = buf.readBlockPos();
        int      color = buf.readInt();
        return new CauldronSplashPacket(pos, color);
    }

    // ── Handler ──────────────────────────────────────────────────────────────

    public static void handle(CauldronSplashPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }

    /**
     * Client-only execution.  Spawns a short burst of tinted
     * CauldronSplashParticles at the fluid surface of the cauldron.
     * Guarded by @OnlyIn so it is never loaded on a dedicated server.
     */
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(CauldronSplashPacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockPos pos = packet.pos;
        BlockState state = level.getBlockState(pos);

        // Derive fluid surface Y from the cauldron's LEVEL state property,
        // matching the formula in LayeredCauldronBlock.getContentHeight:
        //   (6 + level * 3) / 16.0
        // BrewingCauldronBlock uses the same LEVEL_CAULDRON property.
        double surfaceY;
        if (state.getBlock() instanceof BrewingCauldronBlock) {
            int cauldronLevel = state.getValue(BrewingCauldronBlock.LEVEL);
            surfaceY = pos.getY() + (6.0 + cauldronLevel * 3.0) / 16.0;
        } else {
            // Fallback if the block changed in the tick between server send
            // and client receive — use mid-cauldron height.
            surfaceY = pos.getY() + 0.5;
        }

        // Unpack the RGB tint the server computed.
        int rgb   = packet.color;
        float r   = ((rgb >> 16) & 0xFF) / 255.0f;
        float g   = ((rgb >>  8) & 0xFF) / 255.0f;
        float b   = (rgb         & 0xFF) / 255.0f;

        // Spawn a small burst spread across the inner 10/16 of the cauldron.
        RandomSource rand = level.getRandom();
        int count = 6 + rand.nextInt(5);   // 6-10 particles
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.1875 + rand.nextDouble() * 0.625; // [3/16 .. 13/16]
            double z = pos.getZ() + 0.1875 + rand.nextDouble() * 0.625;
            // Slight upward velocity, random lateral scatter.
            double vx = (rand.nextDouble() - 0.5) * 0.15;
            double vy = 0.1 + rand.nextDouble() * 0.1;
            double vz = (rand.nextDouble() - 0.5) * 0.15;
            level.addParticle(
                    CauldronSplashParticle.TYPE,
                    x, surfaceY, z,
                    // We pack r/g/b into the dx/dy/dz velocity slots — the
                    // particle provider reads them back as color.  vx/vy/vz
                    // are stored separately inside the particle itself.
                    r, g, b
            );
            // Spawn a second, slightly delayed particle nearby for visual
            // density without a separate packet.
            level.addParticle(
                    CauldronSplashParticle.TYPE,
                    pos.getX() + 0.1875 + rand.nextDouble() * 0.625,
                    surfaceY,
                    pos.getZ() + 0.1875 + rand.nextDouble() * 0.625,
                    r, g, b
            );
        }
    }
}
