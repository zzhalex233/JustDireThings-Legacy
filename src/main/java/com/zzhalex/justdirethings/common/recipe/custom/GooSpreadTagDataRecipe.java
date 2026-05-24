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
    private final JDTBlockStateSpec catalyst;
    private final int craftingDuration;

    public GooSpreadTagDataRecipe(ResourceLocation sourceId, String inputTag, JDTBlockStateSpec output, int tierRequirement, int craftingDuration) {
        this(sourceId, inputTag, output, tierRequirement, null, craftingDuration);
    }

    public GooSpreadTagDataRecipe(ResourceLocation sourceId, String inputTag, JDTBlockStateSpec output, JDTBlockStateSpec catalyst, int craftingDuration) {
        this(sourceId, inputTag, output, -1, catalyst, craftingDuration);
    }

    private GooSpreadTagDataRecipe(ResourceLocation sourceId, String inputTag, JDTBlockStateSpec output, int tierRequirement, JDTBlockStateSpec catalyst, int craftingDuration) {
        this.sourceId = sourceId;
        this.inputTag = inputTag;
        this.output = output;
        this.tierRequirement = tierRequirement;
        this.catalyst = catalyst == null ? null : catalyst.withoutProperty("alive");
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

    public JDTBlockStateSpec getCatalyst() {
        return catalyst;
    }

    public int getCraftingDuration() {
        return craftingDuration;
    }

    public boolean matches(IBlockState sourceState, int gooTier) {
        return matches(sourceState, gooTier, null);
    }

    public boolean matches(IBlockState sourceState, int gooTier, JDTBlockStateSpec gooCatalyst) {
        return catalystMatches(gooTier, gooCatalyst) && GooFluidRecipeRuntime.matchesBlockTag(inputTag, sourceState);
    }

    private boolean catalystMatches(int gooTier, JDTBlockStateSpec gooCatalyst) {
        if (catalyst == null) {
            return gooTier >= tierRequirement;
        }
        return catalyst.matches(gooCatalyst == null ? null : gooCatalyst.withoutProperty("alive"));
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
