package com.zzhalex.justdirethings.client.render.tile;

import com.zzhalex.justdirethings.common.block.machine.BlockMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderExperienceHolder extends TileEntitySpecialRenderer<TileExperienceHolder> {

    private static final ItemStack EXPERIENCE_BOTTLE = new ItemStack(Items.EXPERIENCE_BOTTLE);

    @Override
    public void render(TileExperienceHolder tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tile == null || tile.getWorld() == null) {
            return;
        }

        IBlockState state = tile.getWorld().getBlockState(tile.getPos());
        EnumFacing direction = state.getPropertyKeys().contains(BlockMachineBase.FACING)
                ? state.getValue(BlockMachineBase.FACING).getOpposite()
                : EnumFacing.NORTH;

        GlStateManager.pushMatrix();
        float previousBrightnessX = OpenGlHelper.lastBrightnessX;
        float previousBrightnessY = OpenGlHelper.lastBrightnessY;
        try {
            GlStateManager.translate(
                    x + 0.5D + direction.getXOffset() * 0.3D,
                    y + 0.5D + direction.getYOffset() * 0.3D,
                    z + 0.5D + direction.getZOffset() * 0.3D
            );
            GlStateManager.rotate(direction.getZOffset() * -90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(direction.getXOffset() * 90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(direction.getYOffset() == 1 ? 0.0F : 180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((System.currentTimeMillis() / 15L) % 360L, 0.0F, 1.0F, 0.0F);
            GlStateManager.scale(0.15F, 0.15F, 0.15F);
            RenderHelper.enableStandardItemLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(EXPERIENCE_BOTTLE, ItemCameraTransforms.TransformType.FIXED);
        } finally {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, previousBrightnessX, previousBrightnessY);
            GlStateManager.popMatrix();
        }
    }
}
