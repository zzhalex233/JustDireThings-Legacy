package com.zzhalex.justdirethings.common.recipe.custom;

import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class FluidDropDataRecipe extends AbstractJDTDataRecipe {

    private final ResourceLocation sourceId;
    private final JDTBlockStateSpec input;
    private final JDTBlockStateSpec output;
    private final ResourceLocation catalystId;

    public FluidDropDataRecipe(ResourceLocation sourceId, JDTBlockStateSpec input, JDTBlockStateSpec output, ResourceLocation catalystId) {
        this.sourceId = sourceId;
        this.input = input;
        this.output = output;
        this.catalystId = catalystId;
    }

    public ResourceLocation getSourceId() {
        return sourceId;
    }

    public JDTBlockStateSpec getInput() {
        return input;
    }

    public JDTBlockStateSpec getOutput() {
        return output;
    }

    public ResourceLocation getCatalystId() {
        return catalystId;
    }

    public boolean matches(JDTBlockStateSpec fluidState, ResourceLocation catalyst) {
        return input.getBlockId().equals(fluidState.getBlockId())
                && input.getProperties().equals(fluidState.getProperties())
                && catalystId.equals(catalyst);
    }

    public boolean matches(IBlockState fluidState, ItemStack catalystStack) {
        if (catalystStack.isEmpty()) {
            return false;
        }
        Item catalyst = ForgeRegistries.ITEMS.getValue(catalystId);
        if (catalyst == null || catalystStack.getItem() != catalyst) {
            return false;
        }
        return input.matches(fluidState) && GooFluidRecipeRuntime.isSourceFluidBlock(fluidState);
    }

    public static class Factory implements IRecipeFactory {

        @Override
        public FluidDropDataRecipe parse(JsonContext context, JsonObject json) {
            ResourceLocation id = JDTBlockStateSpec.resourceLocation(context, JsonUtils.getString(json, "id"));
            JDTBlockStateSpec input = JDTBlockStateSpec.fromJson(context, JsonUtils.getJsonObject(json, "input"));
            JDTBlockStateSpec output = JDTBlockStateSpec.fromJson(context, JsonUtils.getJsonObject(json, "output"));
            ResourceLocation catalyst = JDTBlockStateSpec.resourceLocation(context, JsonUtils.getString(json, "catalyst"));
            return new FluidDropDataRecipe(id, input, output, catalyst);
        }
    }
}
