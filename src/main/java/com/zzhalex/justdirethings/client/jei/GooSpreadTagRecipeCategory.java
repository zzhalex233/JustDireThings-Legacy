package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModContentBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class GooSpreadTagRecipeCategory implements IRecipeCategory<GooSpreadTagRecipeWrapper> {

    public static final int WIDTH = 120;
    public static final int HEIGHT = 40;

    private static final ResourceLocation JEI_VANILLA = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");

    private final IDrawableStatic background;
    private final IDrawableStatic arrow;
    private final IDrawable icon;

    public GooSpreadTagRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.arrow = guiHelper.drawableBuilder(JEI_VANILLA, 24, 132, 24, 17).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModContentBlocks.GOO_BLOCK_TIER1));
    }

    @Override
    public String getUid() {
        return JDTJeiPlugin.GOO_SPREAD_TAG_UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("justdirethings.goospreadrecipetag.title");
    }

    @Override
    public String getModName() {
        return Reference.MOD_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void drawExtras(net.minecraft.client.Minecraft minecraft) {
        arrow.draw(minecraft, 54, 12);
        background.draw(minecraft, 17, 0);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GooSpreadTagRecipeWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup items = recipeLayout.getItemStacks();
        IGuiFluidStackGroup fluids = recipeLayout.getFluidStacks();

        items.init(0, true, 9, 12);
        items.set(0, JeiIngredientUtil.itemStacksForTag(wrapper.getRecipe().getInputTag()));

        items.init(1, true, 29, 12);
        items.set(1, JeiIngredientUtil.gooCatalysts(wrapper.getRecipe().getTierRequirement()));

        List<ItemStack> outputItems = JeiIngredientUtil.itemStacks(wrapper.getRecipe().getOutput());
        FluidStack outputFluid = JeiIngredientUtil.fluidStack(wrapper.getRecipe().getOutput());
        if (!outputItems.isEmpty()) {
            items.init(2, false, 88, 12);
            items.set(2, outputItems);
        } else if (outputFluid != null) {
            fluids.init(0, false, 88, 12, 16, 16, 1000, false, null);
            fluids.set(0, outputFluid);
        }
    }
}
