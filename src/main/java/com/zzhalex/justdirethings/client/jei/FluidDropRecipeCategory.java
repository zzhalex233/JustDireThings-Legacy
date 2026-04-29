package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModContentItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.List;

public class FluidDropRecipeCategory implements IRecipeCategory<FluidDropRecipeWrapper> {

    public static final int WIDTH = 120;
    public static final int HEIGHT = 40;

    private static final ResourceLocation JEI_VANILLA = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");

    private final IDrawableStatic background;
    private final IDrawableStatic arrow;
    private final IDrawable icon;

    public FluidDropRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.arrow = guiHelper.drawableBuilder(JEI_VANILLA, 82, 128, 24, 17).setTextureSize(256, 256).build();
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModContentItems.getItem("polymorphic_catalyst")));
    }

    @Override
    public String getUid() {
        return JDTJeiPlugin.FLUID_DROP_UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("justdirethings.fluiddroprecipe.title");
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
        arrow.draw(minecraft, 34, 20);
        background.draw(minecraft, 17, 0);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FluidDropRecipeWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup items = recipeLayout.getItemStacks();
        IGuiFluidStackGroup fluids = recipeLayout.getFluidStacks();

        Item catalyst = ForgeRegistries.ITEMS.getValue(wrapper.getRecipe().getCatalystId());
        if (catalyst != null) {
            items.init(0, true, 9, 0);
            items.set(0, new ItemStack(catalyst));
        }

        FluidStack inputFluid = JeiIngredientUtil.fluidStack(wrapper.getRecipe().getInput());
        if (inputFluid != null) {
            fluids.init(0, true, 9, 20, 16, 16, 1000, false, null);
            fluids.set(0, inputFluid);
        }

        List<ItemStack> outputItems = JeiIngredientUtil.itemStacks(wrapper.getRecipe().getOutput());
        FluidStack outputFluid = JeiIngredientUtil.fluidStack(wrapper.getRecipe().getOutput());
        if (!outputItems.isEmpty()) {
            items.init(1, false, 68, 20);
            items.set(1, outputItems);
        } else if (outputFluid != null) {
            fluids.init(1, false, 68, 20, 16, 16, 1000, false, null);
            fluids.set(1, outputFluid);
        }
    }
}
