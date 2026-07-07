package net.zharok01.coralinesystems.client.particle;

import com.legacy.rediscovered.client.RediscoveredRenderRefs;
import com.legacy.rediscovered.client.render.RediscoveredRenderType;
import com.legacy.rediscovered.client.render.model.DragonPylonRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.client.entity.orb.OrbRenderer;
import net.zharok01.coralinesystems.entity.OrbShieldBurstData;

/**
 * The Orb's death "shield burst" — a single energy shell that balloons outward
 * and fades, exactly mirroring Rediscovered's PylonShieldBlastParticle (used by
 * PylonBurstEntity's explosion), but reusing the Orb's own shell texture
 * (OrbRenderer.ORB_SHELL_TEXTURE) instead of the dragon pylon's core texture,
 * so the burst visually matches the Orb's living shield.
 */
@OnlyIn(Dist.CLIENT)
public class OrbShieldBurstParticle extends Particle
{
    private final ModelPart model;
    private final float initialRotation;
    final float startSize, endSize, startAlpha, endAlpha;

    public OrbShieldBurstParticle(ClientLevel level, double x, double y, double z, int lifetime, float startSize, float endSize, float startAlpha, float endAlpha)
    {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);

        // Same shell geometry the live Orb uses (RediscoveredRenderRefs.DRAGON_PYLON cube)
        this.model = Minecraft.getInstance().getEntityModels().bakeLayer(RediscoveredRenderRefs.DRAGON_PYLON).getChild("cube");
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.initialRotation = level.random.nextFloat() * 360;
        this.lifetime = lifetime;

        this.startSize = startSize;
        this.endSize = endSize;

        this.startAlpha = startAlpha;
        this.endAlpha = endAlpha;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        float age = this.age + partialTicks;
        float endPercent = Mth.clamp(age / this.lifetime, 0.0F, 1.0F);
        float startPercent = 1.0F - endPercent;

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());
        poseStack.translate(x, y, z);

        float scale = this.startSize * startPercent + this.endSize * endPercent;
        float alpha = this.startAlpha * startPercent + this.endAlpha * endPercent;
        float rotation = this.initialRotation + age * 20;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer energyBuffer = OrbRenderer.ORB_SHELL_TEXTURE.buffer(bufferSource, RediscoveredRenderType::energy);
        DragonPylonRenderer.renderPylonShields(this.model, poseStack, energyBuffer, this.getLightColor(partialTicks), rotation, scale, 1.18F, 1, alpha);

        bufferSource.endBatch();
        poseStack.popPose();
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    public int getLightColor(float partialTicks)
    {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public boolean shouldCull()
    {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<OrbShieldBurstData>
    {
        @Override
        public OrbShieldBurstParticle createParticle(OrbShieldBurstData type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            return new OrbShieldBurstParticle(level, x, y, z, type.lifetime(), type.startSize(), type.endSize(), type.startAlpha(), type.endAlpha());
        }
    }
}