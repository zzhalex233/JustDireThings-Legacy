package com.zzhalex.justdirethings.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleBreaking;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class ItemFlowParticle extends ParticleBreaking {

    private static final Random RANDOM = new Random();

    public ItemFlowParticle(World world, double x, double y, double z, double targetX, double targetY, double targetZ, ItemStack stack, int ticksPerBlock) {
        super(world, x, y, z, stack.getItem(), stack.getMetadata());
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        particleGravity = 0.0F;
        canCollide = false;

        Vec3d source = new Vec3d(posX, posY, posZ);
        Vec3d target = new Vec3d(targetX, targetY, targetZ);
        Vec3d path = target.subtract(source);
        double distance = path.length();
        if (distance > 0.0D) {
            Vec3d normalized = path.normalize();
            float minSize = 0.1F;
            float maxSize = 0.2F;
            float particleSize = minSize + RANDOM.nextFloat() * (maxSize - minSize);
            float speedModifier = (1.0F - 0.5F) * (particleSize - minSize) / (maxSize - minSize) + 0.25F;
            float speedAdjust = Math.max(1, ticksPerBlock) * (1.0F / speedModifier);
            motionX = normalized.x / speedAdjust;
            motionY = normalized.y / speedAdjust;
            motionZ = normalized.z / speedAdjust;
            particleMaxAge = Math.max(1, (int) (distance * speedAdjust));
            multipleParticleScaleBy(particleSize);
        }

        TextureAtlasSprite sprite = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack).getParticleTexture();
        if (sprite != null) {
            setParticleTexture(sprite);
        }
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }
        move(motionX, motionY, motionZ);
    }
}
