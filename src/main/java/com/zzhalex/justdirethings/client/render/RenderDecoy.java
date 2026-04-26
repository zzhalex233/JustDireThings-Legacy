package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.common.entity.EntityDecoy;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class RenderDecoy extends RenderLiving<EntityDecoy> {

    private static final UUID DEFAULT_PLAYER_UUID = UUID.fromString("0192723f-b3dc-495a-959f-52c53fa63bff");

    public RenderDecoy(RenderManager renderManager) {
        super(renderManager, new ModelPlayer(0.0F, false), 0.5F);
        addLayer(new LayerBipedArmor(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDecoy entity) {
        return DefaultPlayerSkin.getDefaultSkin(entity.getOwnerUUID().orElse(DEFAULT_PLAYER_UUID));
    }
}
