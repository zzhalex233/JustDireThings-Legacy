package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.Reference;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum CreatureCatcherModelBakeHandler {
    INSTANCE;

    private static final ModelResourceLocation CATCHER_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":creaturecatcher", "inventory");
    private static final ModelResourceLocation BASE_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":creaturecatcher_base", "inventory");

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
        IBakedModel baseModel = registry.getObject(BASE_MODEL);
        if (baseModel != null) {
            registry.putObject(CATCHER_MODEL, new CreatureCatcherBuiltinModel(baseModel));
        }
    }
}
