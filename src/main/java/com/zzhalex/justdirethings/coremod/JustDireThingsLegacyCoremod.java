package com.zzhalex.justdirethings.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.Name("JustDireThingsLegacyCoremod")
@IFMLLoadingPlugin.TransformerExclusions("com.zzhalex.justdirethings.coremod")
public final class JustDireThingsLegacyCoremod implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                "com.zzhalex.justdirethings.coremod.RenderItemOverlayTransformer",
                "com.zzhalex.justdirethings.coremod.EntityRendererNightVisionTransformer",
                "com.zzhalex.justdirethings.coremod.EntityLivingBaseElytraTransformer",
                "com.zzhalex.justdirethings.coremod.EntityPhaseTransformer",
                "com.zzhalex.justdirethings.coremod.WorldPhaseCollisionTransformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
