package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.common.entity.EntityFireResistantItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;

public class RenderFireResistantItem extends RenderEntityItem {

    public RenderFireResistantItem(RenderManager renderManager) {
        super(renderManager, Minecraft.getMinecraft().getRenderItem());
    }

    @Override
    public boolean shouldSpreadItems() {
        return true;
    }

    @Override
    public boolean shouldBob() {
        return true;
    }
}
