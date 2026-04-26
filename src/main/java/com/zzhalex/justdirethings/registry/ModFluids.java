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
    private static final ModelResourceLocation FLUID_MODEL = new ModelResourceLocation(new ResourceLocation(Reference.MOD_ID, "fluid_block"), "fluid");
    private static final List<JDTFluidDefinition> DEFINITIONS = ImmutableList.of(
        new JDTFluidDefinition("polymorphic_fluid", 0xFF7D5DFF, 10, 1200, 1800, 320),
        new JDTFluidDefinition("portal_fluid", 0xFF55D8FF, 12, 1100, 1600, 320),
        new JDTFluidDefinition("time_fluid", 0xFF5EFFF3, 13, 1050, 1400, 280),
        new JDTFluidDefinition("unstable_portal_fluid", 0xFFE39CFF, 15, 950, 900, 340),
        new JDTFluidDefinition("unrefined_t2_fluid", 0xFFB96022, 8, 1400, 2200, 360),
        new JDTFluidDefinition("refined_t2_fluid", 0xFFFF8A34, 10, 1200, 1800, 360),
        new JDTFluidDefinition("unrefined_t3_fluid", 0xFF8D2BE7, 10, 1450, 2400, 420),
        new JDTFluidDefinition("refined_t3_fluid", 0xFFC55CFF, 12, 1250, 2000, 420),
        new JDTFluidDefinition("unrefined_t4_fluid", 0xFF1D5774, 11, 1450, 2200, 300),
        new JDTFluidDefinition("refined_t4_fluid", 0xFF2FB6E8, 14, 1200, 1700, 300),
        new JDTFluidDefinition("xp_fluid", 0xFF6DFF5D, 14, 1000, 1000, 295)
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
                fluid = new JDTFluid(definition, STILL, FLOWING);
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
                    return FLUID_MODEL;
                }
            });
        }
    }
}
