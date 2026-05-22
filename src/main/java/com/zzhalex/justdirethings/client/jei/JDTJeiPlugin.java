package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.GuiUpgradeStation;
import com.zzhalex.justdirethings.common.container.ContainerUpgradeStation;
import com.zzhalex.justdirethings.common.recipe.custom.FluidDropDataRecipe;
import com.zzhalex.justdirethings.common.recipe.custom.GooFluidRecipeRuntime;
import com.zzhalex.justdirethings.common.recipe.custom.GooSpreadDataRecipe;
import com.zzhalex.justdirethings.common.recipe.custom.GooSpreadTagDataRecipe;
import com.zzhalex.justdirethings.registry.ModBlocks;
import com.zzhalex.justdirethings.registry.ModContentBlocks;
import com.zzhalex.justdirethings.registry.ModContentItems;
import com.zzhalex.justdirethings.registry.ModRecipes;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

@JEIPlugin
public class JDTJeiPlugin implements IModPlugin {

    public static final String GOO_SPREAD_UID = Reference.MOD_ID + ".goospreadrecipe";
    public static final String GOO_SPREAD_TAG_UID = Reference.MOD_ID + ".goospreadrecipetag";
    public static final String FLUID_DROP_UID = Reference.MOD_ID + ".fluiddroprecipe";
    public static final String ORE_TO_RESOURCE_UID = Reference.MOD_ID + ".ore_to_resource";
    public static final String UPGRADE_STATION_UID = Reference.MOD_ID + ".upgrade_station";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new GooSpreadRecipeCategory(guiHelper),
                new GooSpreadTagRecipeCategory(guiHelper),
                new FluidDropRecipeCategory(guiHelper),
                new OreToResourceCategory(guiHelper),
                new UpgradeStationRecipeCategory(guiHelper)
        );
    }

    @Override
    public void register(IModRegistry registry) {
        registry.handleRecipes(GooSpreadDataRecipe.class, GooSpreadRecipeWrapper::new, GOO_SPREAD_UID);
        registry.handleRecipes(GooSpreadTagDataRecipe.class, GooSpreadTagRecipeWrapper::new, GOO_SPREAD_TAG_UID);
        registry.handleRecipes(FluidDropDataRecipe.class, FluidDropRecipeWrapper::new, FLUID_DROP_UID);

        registry.addRecipes(GooFluidRecipeRuntime.gooSpreadRecipes(), GOO_SPREAD_UID);
        registry.addRecipes(GooFluidRecipeRuntime.gooSpreadTagRecipes(), GOO_SPREAD_TAG_UID);
        registry.addRecipes(GooFluidRecipeRuntime.fluidDropRecipes(), FLUID_DROP_UID);
        registry.addRecipes(oreToResourceRecipes(), ORE_TO_RESOURCE_UID);
        registry.addRecipes(wrapUpgradeStationRecipes(), UPGRADE_STATION_UID);
        registry.addGhostIngredientHandler(GuiMachineBase.class, new GhostFilterBasic());

        registry.addRecipeCatalyst(new ItemStack(ModContentBlocks.GOO_BLOCK_TIER1), GOO_SPREAD_UID, GOO_SPREAD_TAG_UID);
        registry.addRecipeCatalyst(new ItemStack(ModContentBlocks.GOO_BLOCK_TIER2), GOO_SPREAD_UID, GOO_SPREAD_TAG_UID);
        registry.addRecipeCatalyst(new ItemStack(ModContentBlocks.GOO_BLOCK_TIER3), GOO_SPREAD_UID, GOO_SPREAD_TAG_UID);
        registry.addRecipeCatalyst(new ItemStack(ModContentBlocks.GOO_BLOCK_TIER4), GOO_SPREAD_UID, GOO_SPREAD_TAG_UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.UPGRADE_STATION), UPGRADE_STATION_UID);
        registry.addRecipeClickArea(GuiUpgradeStation.class, ContainerUpgradeStation.ARROW_X, ContainerUpgradeStation.ARROW_Y, 24, 17, UPGRADE_STATION_UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                ContainerUpgradeStation.class,
                UPGRADE_STATION_UID,
                0,
                3,
                ContainerUpgradeStation.SLOT_COUNT,
                36);
    }

    private static List<UpgradeStationRecipeWrapper> wrapUpgradeStationRecipes() {
        java.util.List<UpgradeStationRecipeWrapper> recipes = new java.util.ArrayList<>();
        for (com.zzhalex.justdirethings.common.recipe.UpgradeStationRecipe recipe : ModRecipes.UPGRADE_STATION_RECIPES) {
            recipes.add(new UpgradeStationRecipeWrapper(recipe));
        }
        return recipes;
    }

    private static List<OreToResourceRecipe> oreToResourceRecipes() {
        return Arrays.asList(
                new OreToResourceRecipe(ModContentBlocks.getBlock("raw_ferricore_ore"), new ItemStack(ModContentItems.getItem("raw_ferricore"))),
                new OreToResourceRecipe(ModContentBlocks.getBlock("raw_blazegold_ore"), new ItemStack(ModContentItems.getItem("raw_blazegold"))),
                new OreToResourceRecipe(ModContentBlocks.getBlock("raw_celestigem_ore"), new ItemStack(ModContentItems.getItem("celestigem"))),
                new OreToResourceRecipe(ModContentBlocks.getBlock("raw_eclipsealloy_ore"), new ItemStack(ModContentItems.getItem("raw_eclipsealloy"))),
                new OreToResourceRecipe(ModContentBlocks.getBlock("raw_coal_t1_ore"), new ItemStack(ModContentItems.getItem("coal_t1"))),
                new OreToResourceRecipe(ModContentBlocks.getBlock("raw_coal_t2_ore"), new ItemStack(ModContentItems.getItem("coal_t2"))),
                new OreToResourceRecipe(ModContentBlocks.getBlock("raw_coal_t3_ore"), new ItemStack(ModContentItems.getItem("coal_t3"))),
                new OreToResourceRecipe(ModContentBlocks.getBlock("raw_coal_t4_ore"), new ItemStack(ModContentItems.getItem("coal_t4")))
        );
    }
}
