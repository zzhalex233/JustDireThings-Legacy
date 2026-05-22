package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModBlocks;
import com.zzhalex.justdirethings.registry.ModRecipes;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class UpgradeStationRecipeCategory implements IRecipeCategory<UpgradeStationRecipeWrapper> {

    public static final int WIDTH = 140;
    public static final int HEIGHT = 56;

    private static final ResourceLocation JEI_VANILLA = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
    private static final ResourceLocation SLOT_BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/justslot.png");
    private static final ResourceLocation TEMPLATE_PLACEHOLDER = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/empty_slot_smithing_template.png");

    private final IDrawableStatic background;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;
    private final IDrawableStatic templatePlaceholder;
    private final IDrawable icon;

    public UpgradeStationRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.drawableBuilder(SLOT_BACKGROUND, 0, 0, 18, 18).setTextureSize(256, 256).build();
        this.arrow = guiHelper.drawableBuilder(JEI_VANILLA, 24, 132, 24, 17).setTextureSize(256, 256).build();
        this.templatePlaceholder = guiHelper.drawableBuilder(TEMPLATE_PLACEHOLDER, 0, 0, 16, 16).setTextureSize(16, 16).build();
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.UPGRADE_STATION));
    }

    @Override
    public String getUid() {
        return JDTJeiPlugin.UPGRADE_STATION_UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("tile.justdirethings.upgrade_station.name");
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
        drawSlot(minecraft, 8, 20);
        drawSlot(minecraft, 30, 20);
        drawSlot(minecraft, 52, 20);
        drawSlot(minecraft, 112, 20);
        if (ModRecipes.areSmithingTemplatesEnabled()) {
            templatePlaceholder.draw(minecraft, 8, 20);
        }
        arrow.draw(minecraft, 76, 20);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, UpgradeStationRecipeWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup items = recipeLayout.getItemStacks();
        // Keep the empty template slot initialized so JEI's transfer ordinal still maps base/addition to container slots 1/2.
        items.init(0, true, 8, 20);
        items.init(1, true, 30, 20);
        items.init(2, true, 52, 20);
        items.init(3, false, 112, 20);

        setIfPresent(items, 0, wrapper.getRecipe().getTemplateStacks());
        setIfPresent(items, 1, wrapper.getRecipe().getBaseStacks());
        setIfPresent(items, 2, wrapper.getRecipe().getAdditionStacks());

        ItemStack output = wrapper.getRecipe().getJeiOutputStack();
        if (!output.isEmpty()) {
            items.set(3, output);
        }
    }

    private static void setIfPresent(IGuiItemStackGroup items, int slot, List<ItemStack> stacks) {
        if (!stacks.isEmpty()) {
            items.set(slot, stacks);
        }
    }

    private void drawSlot(net.minecraft.client.Minecraft minecraft, int x, int y) {
        slot.draw(minecraft, x, y);
    }
}
