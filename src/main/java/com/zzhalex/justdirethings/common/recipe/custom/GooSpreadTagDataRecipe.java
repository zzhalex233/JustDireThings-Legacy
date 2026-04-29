package com.zzhalex.justdirethings.common.recipe.custom;

import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class GooSpreadTagDataRecipe extends AbstractJDTDataRecipe {

    private final ResourceLocation sourceId;
    private final String inputTag;
    private final JDTBlockStateSpec output;
    private final int tierRequirement;
    private final int craftingDuration;

    public GooSpreadTagDataRecipe(ResourceLocation sourceId, String inputTag, JDTBlockStateSpec output, int tierRequirement, int craftingDuration) {
        this.sourceId = sourceId;
        this.inputTag = inputTag;
        this.output = output;
        this.tierRequirement = tierRequirement;
        this.craftingDuration = craftingDuration;
    }

    public ResourceLocation getSourceId() {
        return sourceId;
    }

    public String getInputTag() {
        return inputTag;
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

    public boolean matches(IBlockState sourceState, int gooTier) {
        return gooTier >= tierRequirement && GooFluidRecipeRuntime.matchesBlockTag(inputTag, sourceState);
    }

    public static class Factory implements IRecipeFactory {

        @Override
        public GooSpreadTagDataRecipe parse(JsonContext context, JsonObject json) {
            ResourceLocation id = JDTBlockStateSpec.resourceLocation(context, JsonUtils.getString(json, "id"));
            String inputTag = JsonUtils.getString(JsonUtils.getJsonObject(json, "input"), "tag");
            JDTBlockStateSpec output = JDTBlockStateSpec.fromJson(context, JsonUtils.getJsonObject(json, "output"));
            int tierRequirement = JsonUtils.getInt(json, "tierRequirement");
            int craftingDuration = JsonUtils.getInt(json, "craftingDuration");
            return new GooSpreadTagDataRecipe(id, inputTag, output, tierRequirement, craftingDuration);
        }
    }
}
