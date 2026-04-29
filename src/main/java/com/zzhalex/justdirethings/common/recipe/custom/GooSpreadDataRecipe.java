package com.zzhalex.justdirethings.common.recipe.custom;

import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class GooSpreadDataRecipe extends AbstractJDTDataRecipe {

    private final ResourceLocation sourceId;
    private final JDTBlockStateSpec input;
    private final JDTBlockStateSpec output;
    private final int tierRequirement;
    private final int craftingDuration;

    public GooSpreadDataRecipe(ResourceLocation sourceId, JDTBlockStateSpec input, JDTBlockStateSpec output, int tierRequirement, int craftingDuration) {
        this.sourceId = sourceId;
        this.input = input;
        this.output = output;
        this.tierRequirement = tierRequirement;
        this.craftingDuration = craftingDuration;
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

    public int getTierRequirement() {
        return tierRequirement;
    }

    public int getCraftingDuration() {
        return craftingDuration;
    }

    public boolean matches(JDTBlockStateSpec sourceState, int gooTier) {
        return input.getBlockId().equals(sourceState.getBlockId())
                && input.getProperties().equals(sourceState.getProperties())
                && gooTier >= tierRequirement;
    }

    public boolean matches(IBlockState sourceState, int gooTier) {
        return input.matches(sourceState) && gooTier >= tierRequirement;
    }

    public static class Factory implements IRecipeFactory {

        @Override
        public GooSpreadDataRecipe parse(JsonContext context, JsonObject json) {
            ResourceLocation id = JDTBlockStateSpec.resourceLocation(context, JsonUtils.getString(json, "id"));
            JDTBlockStateSpec input = JDTBlockStateSpec.fromJson(context, JsonUtils.getJsonObject(json, "input"));
            JDTBlockStateSpec output = JDTBlockStateSpec.fromJson(context, JsonUtils.getJsonObject(json, "output"));
            int tierRequirement = JsonUtils.getInt(json, "tierRequirement");
            int craftingDuration = JsonUtils.getInt(json, "craftingDuration");
            return new GooSpreadDataRecipe(id, input, output, tierRequirement, craftingDuration);
        }
    }
}
