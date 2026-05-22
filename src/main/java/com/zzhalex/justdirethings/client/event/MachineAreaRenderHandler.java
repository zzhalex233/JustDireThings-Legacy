package com.zzhalex.justdirethings.client.event;

import com.zzhalex.justdirethings.client.render.tile.RenderMachineArea;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public enum MachineAreaRenderHandler {
    INSTANCE;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.getRenderViewEntity() == null) {
            return;
        }

        Entity viewer = minecraft.getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.getPartialTicks();
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.getPartialTicks();
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);
        RenderMachineArea.prepareGlState();
        try {
            for (TileEntity tileEntity : new ArrayList<>(minecraft.world.loadedTileEntityList)) {
                if (tileEntity instanceof TileMachineBase) {
                    RenderMachineArea.render((TileMachineBase) tileEntity, event.getPartialTicks());
                }
            }
        } finally {
            RenderMachineArea.restoreGlState();
            GlStateManager.popMatrix();
        }
    }
}
