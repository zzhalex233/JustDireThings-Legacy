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
    private final JDTBlockStateSpec catalyst;
    private final int craftingDuration;

    public GooSpreadDataRecipe(ResourceLocation sourceId, JDTBlockStateSpec input, JDTBlockStateSpec output, int tierRequirement, int craftingDuration) {
        this(sourceId, input, output, tierRequirement, null, craftingDuration);
    }

    public GooSpreadDataRecipe(ResourceLocation sourceId, JDTBlockStateSpec input, JDTBlockStateSpec output, JDTBlockStateSpec catalyst, int craftingDuration) {
        this(sourceId, input, output, -1, catalyst, craftingDuration);
    }

    private GooSpreadDataRecipe(ResourceLocation sourceId, JDTBlockStateSpec input, JDTBlockStateSpec output, int tierRequirement, JDTBlockStateSpec catalyst, int craftingDuration) {
        this.sourceId = sourceId;
        this.input = input;
        this.output = output;
        this.tierRequirement = tierRequirement;
        this.catalyst = catalyst == null ? null : catalyst.withoutProperty("alive");
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

    public JDTBlockStateSpec getCatalyst() {
        return catalyst;
    }

    public int getCraftingDuration() {
        return craftingDuration;
    }

    public boolean matches(JDTBlockStateSpec sourceState, int gooTier) {
        return matches(sourceState, gooTier, null);
    }

    public boolean matches(JDTBlockStateSpec sourceState, int gooTier, JDTBlockStateSpec gooCatalyst) {
        return input.equals(sourceState) && catalystMatches(gooTier, gooCatalyst);
    }

    public boolean matches(IBlockState sourceState, int gooTier) {
        return matches(sourceState, gooTier, null);
    }

    public boolean matches(IBlockState sourceState, int gooTier, JDTBlockStateSpec gooCatalyst) {
        return input.matches(sourceState) && catalystMatches(gooTier, gooCatalyst);
    }

    private boolean catalystMatches(int gooTier, JDTBlockStateSpec gooCatalyst) {
        if (catalyst == null) {
            return gooTier >= tierRequirement;
        }
        return catalyst.matches(gooCatalyst == null ? null : gooCatalyst.withoutProperty("alive"));
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
