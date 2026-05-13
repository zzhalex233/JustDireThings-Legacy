package com.zzhalex.justdirethings.client.event;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.BoundInventoryHelper;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public enum AbilityRenderHandler {
    INSTANCE;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null || minecraft.getRenderViewEntity() == null) {
            return;
        }

        Entity viewer = minecraft.getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.getPartialTicks();
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.getPartialTicks();
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);
        prepareGlState();
        try {
            renderHeldStack(minecraft.player, minecraft.player.getHeldItemMainhand());
            renderHeldStack(minecraft.player, minecraft.player.getHeldItemOffhand());
        } finally {
            restoreGlState();
            GlStateManager.popMatrix();
        }
    }

    private static void renderHeldStack(EntityPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return;
        }

        ToggleableTool tool = (ToggleableTool) stack.getItem();
        if (tool.supportsAbility(Ability.VOIDSHIFT)
                && tool.hasInstalledAbility(stack, Ability.VOIDSHIFT)
                && tool.getSetting(stack, Ability.VOIDSHIFT)
                && tool.getCustomSetting(stack, Ability.VOIDSHIFT) == 0) {
            renderVoidShiftPreview(player, stack);
        }

        if (tool.supportsAbility(Ability.DROPTELEPORT)
                && tool.hasInstalledAbility(stack, Ability.DROPTELEPORT)
                && tool.getSetting(stack, Ability.DROPTELEPORT)
                && tool.getCustomSetting(stack, Ability.DROPTELEPORT) == 0) {
            renderDropTeleportBinding(player, stack);
        }
    }

    private static void renderVoidShiftPreview(EntityPlayer player, ItemStack stack) {
        Vec3d position = getVoidShiftPosition(player.world, player, stack);
        if (position == null) {
            return;
        }

        AxisAlignedBB feetBox = new AxisAlignedBB(
                position.x - 0.3D,
                position.y,
                position.z - 0.3D,
                position.x + 0.3D,
                position.y + 1.8D,
                position.z + 0.3D
        );
        drawSolidBox(feetBox, 120, 220, 255, 45);
        drawWireBox(feetBox, 120, 220, 255, 210);
    }

    private static Vec3d getVoidShiftPosition(World world, EntityPlayer player, ItemStack stack) {
        if (world == null || player == null || stack == null || stack.isEmpty()) {
            return null;
        }

        ToggleableTool tool = (ToggleableTool) stack.getItem();
        int distance = Math.max(1, tool.getToolValue(stack, Ability.VOIDSHIFT));
        Vec3d eyePosition = player.getPositionEyes(1.0F);
        Vec3d lookVector = player.getLookVec();
        Vec3d endPosition = eyePosition.add(lookVector.scale(distance));
        RayTraceResult result = world.rayTraceBlocks(eyePosition, endPosition, false, true, false);
        if (result == null || result.typeOfHit == RayTraceResult.Type.MISS) {
            return getShapeAdjustedPosition(world, endPosition);
        }
        return getShapeAdjustedPosition(world, result.getBlockPos().down().offset(result.sideHit));
    }

    private static Vec3d getShapeAdjustedPosition(World world, Vec3d missPosition) {
        return getShapeAdjustedPosition(world, new BlockPos(missPosition).down());
    }

    private static Vec3d getShapeAdjustedPosition(World world, BlockPos landingPos) {
        AxisAlignedBB collisionBox = world.getBlockState(landingPos).getCollisionBoundingBox(world, landingPos);
        double yOffset = collisionBox == null ? 0.0D : collisionBox.maxY;
        return new Vec3d(landingPos.getX() + 0.5D, landingPos.getY() + yOffset, landingPos.getZ() + 0.5D);
    }

    private static void renderDropTeleportBinding(EntityPlayer player, ItemStack stack) {
        BoundInventoryHelper.BoundLocation boundLocation = BoundInventoryHelper.getBoundTo(stack);
        if (boundLocation == null || player.dimension != boundLocation.getDimension()) {
            return;
        }

        BlockPos pos = boundLocation.getPos();
        AxisAlignedBB box = new AxisAlignedBB(pos).grow(0.005D);
        drawSolidBox(box, 0, 255, 0, 55);
        EnumFacing side = boundLocation.getSide();
        if (side != null) {
            drawSolidBox(faceBox(pos, side), 0, 80, 255, 80);
        }
        drawWireBox(box, 255, 255, 255, 220);
    }

    private static AxisAlignedBB faceBox(BlockPos pos, EnumFacing side) {
        double minX = pos.getX() - 0.007D;
        double minY = pos.getY() - 0.007D;
        double minZ = pos.getZ() - 0.007D;
        double maxX = pos.getX() + 1.007D;
        double maxY = pos.getY() + 1.007D;
        double maxZ = pos.getZ() + 1.007D;
        double thickness = 0.02D;
        switch (side) {
            case DOWN:
                return new AxisAlignedBB(minX, minY, minZ, maxX, minY + thickness, maxZ);
            case UP:
                return new AxisAlignedBB(minX, maxY - thickness, minZ, maxX, maxY, maxZ);
            case NORTH:
                return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, minZ + thickness);
            case SOUTH:
                return new AxisAlignedBB(minX, minY, maxZ - thickness, maxX, maxY, maxZ);
            case WEST:
                return new AxisAlignedBB(minX, minY, minZ, minX + thickness, maxY, maxZ);
            case EAST:
            default:
                return new AxisAlignedBB(maxX - thickness, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private static void prepareGlState() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.depthMask(false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glLineWidth(2.0F);
    }

    private static void restoreGlState() {
        GL11.glLineWidth(1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawWireBox(AxisAlignedBB box, int red, int green, int blue, int alpha) {
        GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, alpha / 255.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        addLine(buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ);
        addLine(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
        addLine(buffer, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ);
        addLine(buffer, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ);
        addLine(buffer, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ);
        addLine(buffer, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
        addLine(buffer, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ);
        addLine(buffer, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ);
        addLine(buffer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ);
        addLine(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
        addLine(buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
        addLine(buffer, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ);
        tessellator.draw();
    }

    private static void addLine(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2) {
        buffer.pos(x1, y1, z1).endVertex();
        buffer.pos(x2, y2, z2).endVertex();
    }

    private static void drawSolidBox(AxisAlignedBB box, int red, int green, int blue, int alpha) {
        GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, alpha / 255.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        addBoxVertices(buffer, box);
        tessellator.draw();
    }

    private static void addBoxVertices(BufferBuilder buffer, AxisAlignedBB box) {
        addQuad(buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ);
        addQuad(buffer, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ);
        addQuad(buffer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.minY, box.minZ);
        addQuad(buffer, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ);
        addQuad(buffer, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ);
        addQuad(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.minY, box.maxZ);
    }

    private static void addQuad(BufferBuilder buffer,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2,
                                double x3, double y3, double z3,
                                double x4, double y4, double z4) {
        buffer.pos(x1, y1, z1).endVertex();
        buffer.pos(x2, y2, z2).endVertex();
        buffer.pos(x3, y3, z3).endVertex();
        buffer.pos(x4, y4, z4).endVertex();
    }
}
