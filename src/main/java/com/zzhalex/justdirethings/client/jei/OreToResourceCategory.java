package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModContentBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class OreToResourceCategory implements IRecipeCategory<OreToResourceRecipe> {

    public static final int WIDTH = 120;
    public static final int HEIGHT = 30;

    private static final ResourceLocation JEI_VANILLA = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final IDrawable pickaxeIcon;
    private final IDrawableAnimated animatedArrow;

    public OreToResourceCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(120, 30);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModContentBlocks.RAW_FERRICORE_ORE));
        this.pickaxeIcon = guiHelper.createDrawableIngredient(new ItemStack(Items.IRON_PICKAXE));
        IDrawableStatic arrowDrawable = guiHelper.drawableBuilder(JEI_VANILLA, 82, 128, 24, 17).setTextureSize(256, 256).build();
        this.animatedArrow = guiHelper.createAnimatedDrawable(arrowDrawable, 40, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public String getUid() {
        return JDTJeiPlugin.ORE_TO_RESOURCE_UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("justdirethings.oretoresource.title");
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
        animatedArrow.draw(minecraft, 46, 10);
        background.draw(minecraft, 17, 0);
        pickaxeIcon.draw(minecraft, 50, -2);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, OreToResourceRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup items = recipeLayout.getItemStacks();
        items.init(0, true, 20, 10);
        items.set(0, recipe.getOreBlock());
        items.init(1, false, 80, 10);
        items.set(1, recipe.getOutput());
    }
}
