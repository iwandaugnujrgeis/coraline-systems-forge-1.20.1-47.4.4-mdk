package net.zharok01.coralinesystems.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.zharok01.coralinesystems.block.BrewingCauldronBlock;
import net.zharok01.coralinesystems.client.particle.CauldronBubbleParticle;
import net.zharok01.coralinesystems.client.particle.CauldronSplashParticle;

import java.util.function.Supplier;

public class CauldronSplashPacket {

    private final BlockPos pos;
    private final int color;
    private final boolean burstMode;

    public CauldronSplashPacket(BlockPos pos, int color) {
        this(pos, color, false);
    }

    public CauldronSplashPacket(BlockPos pos, int color, boolean burstMode) {
        this.pos = pos;
        this.color = color;
        this.burstMode = burstMode;
    }

    public static void encode(CauldronSplashPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.color);
        buf.writeBoolean(packet.burstMode);
    }

    public static CauldronSplashPacket decode(FriendlyByteBuf buf) {
        return new CauldronSplashPacket(buf.readBlockPos(), buf.readInt(), buf.readBoolean());
    }

    public static void handle(CauldronSplashPacket packet, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(CauldronSplashPacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockPos pos = packet.pos;
        BlockState state = level.getBlockState(pos);

        double surfaceY;
        if (state.getBlock() instanceof BrewingCauldronBlock) {
            int cauldronLevel = state.getValue(BrewingCauldronBlock.LEVEL);
            surfaceY = pos.getY() + (6.0 + cauldronLevel * 3.0) / 16.0;
        } else {
            surfaceY = pos.getY() + 0.5;
        }

        int rgb = packet.color;
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;

        RandomSource rand = level.getRandom();

        if (packet.burstMode) {
            int count = 18 + rand.nextInt(8);
            for (int i = 0; i < count; i++) {
                double x = pos.getX() + 0.2 + rand.nextDouble() * 0.6;
                double z = pos.getZ() + 0.2 + rand.nextDouble() * 0.6;
                level.addParticle(CauldronBubbleParticle.TYPE, x, surfaceY - 0.05, z, r, g, b);
            }
        } else {
            int count = 16 + rand.nextInt(9);
            for (int i = 0; i < count; i++) {
                double x = pos.getX() + 0.1875 + rand.nextDouble() * 0.625;
                double z = pos.getZ() + 0.1875 + rand.nextDouble() * 0.625;
                level.addParticle(CauldronSplashParticle.TYPE, x, surfaceY + 0.1, z, r, g, b);
            }
        }
    }
}