package com.zzhalex.justdirethings.registry;

import com.google.common.collect.ImmutableList;
import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.fluid.JDTFluid;
import com.zzhalex.justdirethings.common.fluid.JDTFluidBlock;
import com.zzhalex.justdirethings.common.fluid.JDTFluidDefinition;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModFluids {

    private static final ResourceLocation STILL = new ResourceLocation(Reference.MOD_ID, "block/fluid_source");
    private static final ResourceLocation FLOWING = new ResourceLocation(Reference.MOD_ID, "block/fluid_flowing");
    private static final ResourceLocation OVERLAY = new ResourceLocation(Reference.MOD_ID, "block/fluid_overlay");
    private static final List<JDTFluidDefinition> DEFINITIONS = ImmutableList.of(
        new JDTFluidDefinition("polymorphic_fluid", 0xFFFFFFFF, 0, 1000, 1000, 300),
        new JDTFluidDefinition("portal_fluid", 0xFF00DD00, 0, 1000, 1000, 300),
        new JDTFluidDefinition("time_fluid", 0x7700FF00, 0, 1000, 250, -200),
        new JDTFluidDefinition("unstable_portal_fluid", 0xFF9400D3, 0, 1000, 1000, 300),
        new JDTFluidDefinition("unrefined_t2_fluid", 0xFF8B4500, 0, 1000, 1000, 300),
        new JDTFluidDefinition("refined_t2_fluid", 0xFF8B0000, 0, 1000, 1000, 300),
        new JDTFluidDefinition("unrefined_t3_fluid", 0xFF64D5AD, 0, 1000, 1000, 300),
        new JDTFluidDefinition("refined_t3_fluid", 0xFF40C7C7, 0, 1000, 1000, 300),
        new JDTFluidDefinition("unrefined_t4_fluid", 0xFF36484A, 0, 1000, 1000, 300),
        new JDTFluidDefinition("refined_t4_fluid", 0xFF1B2027, 0, 1000, 1000, 300),
        new JDTFluidDefinition("xp_fluid", 0xFF32CD32, 0, 1000, 1000, 300)
    );
    private static final Map<String, Fluid> FLUIDS = new LinkedHashMap<>();
    private static final Map<String, Block> FLUID_BLOCKS = new LinkedHashMap<>();
    private static boolean bootstrapped;

    private ModFluids() {
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        FluidRegistry.enableUniversalBucket();
        for (JDTFluidDefinition definition : DEFINITIONS) {
            Fluid fluid = FluidRegistry.getFluid(definition.getId());
            if (fluid == null) {
                fluid = new JDTFluid(definition, STILL, FLOWING, OVERLAY);
                FluidRegistry.registerFluid(fluid);
            }
            FluidRegistry.addBucketForFluid(fluid);
            FLUIDS.put(definition.getId(), fluid);
            FLUID_BLOCKS.put(definition.getId(), new JDTFluidBlock(definition, fluid));
        }
    }

    public static List<String> coreFluidIds() {
        return DEFINITIONS.stream().map(JDTFluidDefinition::getId).collect(java.util.stream.Collectors.toList());
    }

    public static Fluid getFluid(String id) {
        return FLUIDS.get(id);
    }

    public static Block getFluidBlock(String id) {
        return FLUID_BLOCKS.get(id);
    }

    public static Collection<Block> getFluidBlocks() {
        return FLUID_BLOCKS.values();
    }

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        bootstrap();
        for (Block block : FLUID_BLOCKS.values()) {
            event.getRegistry().register(block);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        for (Block block : FLUID_BLOCKS.values()) {
            ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
                @Override
                protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return new ModelResourceLocation(block.getRegistryName(), "fluid");
                }
            });
        }
    }
}
