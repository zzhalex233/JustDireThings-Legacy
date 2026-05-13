package com.zzhalex.justdirethings.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public final class AlwaysVisibleParticleHelper {

    private static final Random RANDOM = new Random();
    private static final long PARTICLE_LIFETIME_MILLIS = 1100L;
    private static final List<VisibleParticle> PARTICLES = new ArrayList<>();

    private AlwaysVisibleParticleHelper() {
    }

    public static void addBlockParticles(RenderWorldLastEvent event, List<BlockPos> positions, int particlesPerBlock, int red, int green, int blue, int alpha) {
        if (positions == null || positions.isEmpty()) {
            return;
        }
        for (BlockPos pos : positions) {
            for (int i = 0; i < particlesPerBlock; i++) {
                addParticle(
                        pos.getX() + RANDOM.nextDouble(),
                        pos.getY() + RANDOM.nextDouble(),
                        pos.getZ() + RANDOM.nextDouble(),
                        red, green, blue, alpha);
            }
        }
    }

    public static void addEntityParticles(RenderWorldLastEvent event, List<Entity> entities, int particlesPerEntity, int red, int green, int blue, int alpha) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        for (Entity entity : entities) {
            if (entity == null || entity.isDead) {
                continue;
            }
            AxisAlignedBB bounds = entity.getEntityBoundingBox();
            for (int i = 0; i < particlesPerEntity; i++) {
                addParticle(
                        randomBetween(bounds.minX, bounds.maxX),
                        randomBetween(bounds.minY, bounds.maxY),
                        randomBetween(bounds.minZ, bounds.maxZ),
                        red, green, blue, alpha);
            }
        }
    }

    public static void renderActive(RenderWorldLastEvent event) {
        if (PARTICLES.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        Entity viewer = minecraft.getRenderViewEntity();
        if (viewer == null) {
            return;
        }

        long now = System.currentTimeMillis();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.getPartialTicks();
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.getPartialTicks();
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);
        prepareState();
        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            Iterator<VisibleParticle> iterator = PARTICLES.iterator();
            while (iterator.hasNext()) {
                VisibleParticle particle = iterator.next();
                long age = now - particle.createdAt;
                if (age >= PARTICLE_LIFETIME_MILLIS) {
                    iterator.remove();
                    continue;
                }
                float life = 1.0F - ((float) age / (float) PARTICLE_LIFETIME_MILLIS);
                addParticleQuad(buffer, particle, Math.max(32, (int) (particle.alpha * life)));
            }
            tessellator.draw();
        } finally {
            restoreState();
            GlStateManager.popMatrix();
        }
    }

    private static void prepareState() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.depthMask(false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void restoreState() {
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void addParticle(double x, double y, double z, int red, int green, int blue, int alpha) {
        PARTICLES.add(new VisibleParticle(x, y, z, red, green, blue, alpha, 0.055D + RANDOM.nextDouble() * 0.045D));
    }

    private static void addParticleQuad(BufferBuilder buffer, VisibleParticle particle, int alpha) {
        double size = particle.size;
        buffer.pos(particle.x - size, particle.y, particle.z - size).color(particle.red, particle.green, particle.blue, alpha).endVertex();
        buffer.pos(particle.x + size, particle.y, particle.z - size).color(particle.red, particle.green, particle.blue, alpha).endVertex();
        buffer.pos(particle.x + size, particle.y, particle.z + size).color(particle.red, particle.green, particle.blue, alpha).endVertex();
        buffer.pos(particle.x - size, particle.y, particle.z + size).color(particle.red, particle.green, particle.blue, alpha).endVertex();

        buffer.pos(particle.x - size, particle.y - size, particle.z).color(particle.red, particle.green, particle.blue, alpha).endVertex();
        buffer.pos(particle.x + size, particle.y - size, particle.z).color(particle.red, particle.green, particle.blue, alpha).endVertex();
        buffer.pos(particle.x + size, particle.y + size, particle.z).color(particle.red, particle.green, particle.blue, alpha).endVertex();
        buffer.pos(particle.x - size, particle.y + size, particle.z).color(particle.red, particle.green, particle.blue, alpha).endVertex();
    }

    private static double randomBetween(double min, double max) {
        return min + (max - min) * RANDOM.nextDouble();
    }

    private static final class VisibleParticle {
        private final double x;
        private final double y;
        private final double z;
        private final int red;
        private final int green;
        private final int blue;
        private final int alpha;
        private final double size;
        private final long createdAt;

        private VisibleParticle(double x, double y, double z, int red, int green, int blue, int alpha, double size) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.size = size;
            this.createdAt = System.currentTimeMillis();
        }
    }
}
