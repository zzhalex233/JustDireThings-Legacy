package com.zzhalex.justdirethings.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelPortalProjectile extends ModelBase {

    private final ModelRenderer main;

    public ModelPortalProjectile() {
        textureWidth = 64;
        textureHeight = 32;
        main = new ModelRenderer(this);
        main.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -1.0F, 8, 8, 2);
        main.setTextureOffset(0, 10).addBox(-1.0F, -4.0F, -4.0F, 2, 8, 8);
        main.setTextureOffset(20, 0).addBox(-4.0F, -1.0F, -4.0F, 8, 2, 8);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        main.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        main.rotateAngleY = netHeadYaw * ((float) Math.PI / 180.0F);
        main.rotateAngleX = headPitch * ((float) Math.PI / 180.0F);
    }
}
