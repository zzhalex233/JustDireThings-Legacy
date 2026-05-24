package com.zzhalex.justdirethings.compat.crafttweaker;

import com.zzhalex.justdirethings.common.recipe.custom.FluidDropDataRecipe;
import com.zzhalex.justdirethings.common.recipe.custom.GooCatalystRegistry;
import com.zzhalex.justdirethings.common.recipe.custom.GooSpreadDataRecipe;
import com.zzhalex.justdirethings.common.recipe.custom.GooSpreadTagDataRecipe;
import com.zzhalex.justdirethings.common.recipe.custom.JDTBlockStateSpec;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.justdirethings.Goo")
@ZenRegister
public final class CraftTweakerGoo {

    private CraftTweakerGoo() {
    }

    @ZenMethod
    public static void registerGoo(IBlockState catalyst, int tier, IItemStack revivalItem) {
        CraftTweakerAPI.apply(new RegisterGooAction(CraftTweakerHelper.blockStateSpec(catalyst), tier, itemStacks(revivalItem)));
    }

    @ZenMethod
    public static void registerGoo(IBlockState catalyst, int tier, IItemStack[] revivalItems) {
        CraftTweakerAPI.apply(new RegisterGooAction(CraftTweakerHelper.blockStateSpec(catalyst), tier, itemStacks(revivalItems)));
    }

    @ZenMethod
    public static void addSpread(String name, IBlockState input, IBlockState output, int tierRequirement, int craftingDuration) {
        CraftTweakerAPI.apply(new AddGooSpreadAction(
                CraftTweakerHelper.recipeId(name),
                CraftTweakerHelper.blockStateSpec(input),
                CraftTweakerHelper.blockStateSpec(output),
                tierRequirement,
                craftingDuration));
    }

    @ZenMethod
    public static void addSpread(String name, IBlockState input, IBlockState output, IBlockState catalyst, int craftingDuration) {
        CraftTweakerAPI.apply(new AddGooSpreadAction(
                CraftTweakerHelper.recipeId(name),
                CraftTweakerHelper.blockStateSpec(input),
                CraftTweakerHelper.blockStateSpec(output),
                CraftTweakerHelper.blockStateSpec(catalyst),
                craftingDuration));
    }

    @ZenMethod
    public static void addSpread(String name, IItemStack input, IItemStack output, int tierRequirement, int craftingDuration) {
        CraftTweakerAPI.apply(new AddGooSpreadAction(
                CraftTweakerHelper.recipeId(name),
                CraftTweakerHelper.blockStateSpec(input),
                CraftTweakerHelper.blockStateSpec(output),
                tierRequirement,
                craftingDuration));
    }

    @ZenMethod
    public static void addSpread(String name, IItemStack input, IItemStack output, IBlockState catalyst, int craftingDuration) {
        CraftTweakerAPI.apply(new AddGooSpreadAction(
                CraftTweakerHelper.recipeId(name),
                CraftTweakerHelper.blockStateSpec(input),
                CraftTweakerHelper.blockStateSpec(output),
                CraftTweakerHelper.blockStateSpec(catalyst),
                craftingDuration));
    }

    @ZenMethod
    public static void addSpreadTag(String name, String inputTag, IBlockState output, int tierRequirement, int craftingDuration) {
        CraftTweakerAPI.apply(new AddGooSpreadTagAction(
                CraftTweakerHelper.recipeId(name),
                inputTag,
                CraftTweakerHelper.blockStateSpec(output),
                tierRequirement,
                craftingDuration));
    }

    @ZenMethod
    public static void addSpreadTag(String name, String inputTag, IBlockState output, IBlockState catalyst, int craftingDuration) {
        CraftTweakerAPI.apply(new AddGooSpreadTagAction(
                CraftTweakerHelper.recipeId(name),
                inputTag,
                CraftTweakerHelper.blockStateSpec(output),
                CraftTweakerHelper.blockStateSpec(catalyst),
                craftingDuration));
    }

    @ZenMethod
    public static void addSpreadTag(String name, String inputTag, IItemStack output, int tierRequirement, int craftingDuration) {
        CraftTweakerAPI.apply(new AddGooSpreadTagAction(
                CraftTweakerHelper.recipeId(name),
                inputTag,
                CraftTweakerHelper.blockStateSpec(output),
                tierRequirement,
                craftingDuration));
    }

    @ZenMethod
    public static void addSpreadTag(String name, String inputTag, IItemStack output, IBlockState catalyst, int craftingDuration) {
        CraftTweakerAPI.apply(new AddGooSpreadTagAction(
                CraftTweakerHelper.recipeId(name),
                inputTag,
                CraftTweakerHelper.blockStateSpec(output),
                CraftTweakerHelper.blockStateSpec(catalyst),
                craftingDuration));
    }

    @ZenMethod
    public static void removeSpread(String name) {
        CraftTweakerAPI.apply(new RemoveRecipeByIdAction(CraftTweakerHelper.recipeId(name), GooSpreadDataRecipe.class, "goo spread"));
    }

    @ZenMethod
    public static void removeSpread(IBlockState input) {
        CraftTweakerAPI.apply(new RemoveGooSpreadByInputAction(CraftTweakerHelper.blockStateSpec(input)));
    }

    @ZenMethod
    public static void removeSpread(IItemStack input) {
        CraftTweakerAPI.apply(new RemoveGooSpreadByInputAction(CraftTweakerHelper.blockStateSpec(input)));
    }

    @ZenMethod
    public static void removeSpreadTag(String inputTag) {
        CraftTweakerAPI.apply(new RemoveGooSpreadTagAction(inputTag));
    }

    @ZenMethod
    public static void addFluidDrop(String name, IBlockState input, IBlockState output, IItemStack catalyst) {
        CraftTweakerAPI.apply(new AddFluidDropAction(
                CraftTweakerHelper.recipeId(name),
                CraftTweakerHelper.blockStateSpec(input),
                CraftTweakerHelper.blockStateSpec(output),
                CraftTweakerHelper.itemId(catalyst)));
    }

    @ZenMethod
    public static void addFluidDrop(String name, IBlockState input, IItemStack output, IItemStack catalyst) {
        CraftTweakerAPI.apply(new AddFluidDropAction(
                CraftTweakerHelper.recipeId(name),
                CraftTweakerHelper.blockStateSpec(input),
                CraftTweakerHelper.blockStateSpec(output),
                CraftTweakerHelper.itemId(catalyst)));
    }

    @ZenMethod
    public static void addFluidDrop(String name, ILiquidStack input, IBlockState output, IItemStack catalyst) {
        CraftTweakerAPI.apply(new AddFluidDropAction(
                CraftTweakerHelper.recipeId(name),
                CraftTweakerHelper.blockStateSpec(input),
                CraftTweakerHelper.blockStateSpec(output),
                CraftTweakerHelper.itemId(catalyst)));
    }

    @ZenMethod
    public static void addFluidDrop(String name, ILiquidStack input, IItemStack output, IItemStack catalyst) {
        CraftTweakerAPI.apply(new AddFluidDropAction(
                CraftTweakerHelper.recipeId(name),
                CraftTweakerHelper.blockStateSpec(input),
                CraftTweakerHelper.blockStateSpec(output),
                CraftTweakerHelper.itemId(catalyst)));
    }

    @ZenMethod
    public static void removeFluidDrop(String name) {
        CraftTweakerAPI.apply(new RemoveRecipeByIdAction(CraftTweakerHelper.recipeId(name), FluidDropDataRecipe.class, "fluid drop"));
    }

    @ZenMethod
    public static void removeFluidDrop(IBlockState input, IItemStack catalyst) {
        CraftTweakerAPI.apply(new RemoveFluidDropAction(
                CraftTweakerHelper.blockStateSpec(input),
                CraftTweakerHelper.itemId(catalyst)));
    }

    @ZenMethod
    public static void removeFluidDrop(ILiquidStack input, IItemStack catalyst) {
        CraftTweakerAPI.apply(new RemoveFluidDropAction(
                CraftTweakerHelper.blockStateSpec(input),
                CraftTweakerHelper.itemId(catalyst)));
    }

    private static List<ItemStack> itemStacks(IItemStack stack) {
        List<ItemStack> stacks = new ArrayList<>();
        ItemStack itemStack = CraftTweakerHelper.itemStack(stack);
        if (!itemStack.isEmpty()) {
            stacks.add(itemStack);
        }
        return stacks;
    }

    private static List<ItemStack> itemStacks(IItemStack[] inputStacks) {
        List<ItemStack> stacks = new ArrayList<>();
        if (inputStacks == null) {
            return stacks;
        }
        for (IItemStack inputStack : inputStacks) {
            if (inputStack == null) {
                continue;
            }
            ItemStack itemStack = CraftTweakerHelper.itemStack(inputStack);
            if (!itemStack.isEmpty()) {
                stacks.add(itemStack);
            }
        }
        return stacks;
    }

    private static final class RegisterGooAction implements IAction {

        private final JDTBlockStateSpec catalyst;
        private final int tier;
        private final List<ItemStack> revivalItems;

        private RegisterGooAction(JDTBlockStateSpec catalyst, int tier, List<ItemStack> revivalItems) {
            this.catalyst = catalyst;
            this.tier = tier;
            this.revivalItems = revivalItems;
        }

        @Override
        public void apply() {
            GooCatalystRegistry.registerCustomGoo(catalyst, tier, revivalItems);
        }

        @Override
        public String describe() {
            return "Registering JustDireThings custom goo catalyst";
        }

        @Override
        public boolean validate() {
            return catalyst != null && tier >= 1 && GooCatalystRegistry.canBeCustomGoo(catalyst) && revivalItems != null && !revivalItems.isEmpty();
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings custom goo catalysts need a valid non-fluid block, a tier of at least 1, and at least one revival item";
        }
    }

    private static final class AddGooSpreadAction implements IAction {

        private final ResourceLocation id;
        private final JDTBlockStateSpec input;
        private final JDTBlockStateSpec output;
        private final int tierRequirement;
        private final JDTBlockStateSpec catalyst;
        private final int craftingDuration;

        private AddGooSpreadAction(ResourceLocation id, JDTBlockStateSpec input, JDTBlockStateSpec output, int tierRequirement, int craftingDuration) {
            this(id, input, output, tierRequirement, null, craftingDuration);
        }

        private AddGooSpreadAction(ResourceLocation id, JDTBlockStateSpec input, JDTBlockStateSpec output, JDTBlockStateSpec catalyst, int craftingDuration) {
            this(id, input, output, -1, catalyst, craftingDuration);
        }

        private AddGooSpreadAction(ResourceLocation id, JDTBlockStateSpec input, JDTBlockStateSpec output, int tierRequirement, JDTBlockStateSpec catalyst, int craftingDuration) {
            this.id = id;
            this.input = input;
            this.output = output;
            this.tierRequirement = tierRequirement;
            this.catalyst = catalyst;
            this.craftingDuration = craftingDuration;
        }

        @Override
        public void apply() {
            CraftTweakerHelper.registerRecipe(id, catalyst == null
                    ? new GooSpreadDataRecipe(id, input, output, tierRequirement, craftingDuration)
                    : new GooSpreadDataRecipe(id, input, output, catalyst, craftingDuration));
        }

        @Override
        public String describe() {
            return "Adding JustDireThings goo spread recipe " + id;
        }

        @Override
        public boolean validate() {
            return id != null && input != null && output != null && (tierRequirement >= 0 || catalyst != null) && craftingDuration > 0;
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings goo spread recipes need a valid id, input, output, tier or catalyst, and positive duration";
        }
    }

    private static final class AddGooSpreadTagAction implements IAction {

        private final ResourceLocation id;
        private final String inputTag;
        private final JDTBlockStateSpec output;
        private final int tierRequirement;
        private final JDTBlockStateSpec catalyst;
        private final int craftingDuration;

        private AddGooSpreadTagAction(ResourceLocation id, String inputTag, JDTBlockStateSpec output, int tierRequirement, int craftingDuration) {
            this(id, inputTag, output, tierRequirement, null, craftingDuration);
        }

        private AddGooSpreadTagAction(ResourceLocation id, String inputTag, JDTBlockStateSpec output, JDTBlockStateSpec catalyst, int craftingDuration) {
            this(id, inputTag, output, -1, catalyst, craftingDuration);
        }

        private AddGooSpreadTagAction(ResourceLocation id, String inputTag, JDTBlockStateSpec output, int tierRequirement, JDTBlockStateSpec catalyst, int craftingDuration) {
            this.id = id;
            this.inputTag = inputTag;
            this.output = output;
            this.tierRequirement = tierRequirement;
            this.catalyst = catalyst;
            this.craftingDuration = craftingDuration;
        }

        @Override
        public void apply() {
            CraftTweakerHelper.registerRecipe(id, catalyst == null
                    ? new GooSpreadTagDataRecipe(id, inputTag, output, tierRequirement, craftingDuration)
                    : new GooSpreadTagDataRecipe(id, inputTag, output, catalyst, craftingDuration));
        }

        @Override
        public String describe() {
            return "Adding JustDireThings goo spread tag recipe " + id;
        }

        @Override
        public boolean validate() {
            return id != null && inputTag != null && !inputTag.trim().isEmpty() && output != null && (tierRequirement >= 0 || catalyst != null) && craftingDuration > 0;
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings goo spread tag recipes need a valid id, tag, output, tier or catalyst, and positive duration";
        }
    }

    private static final class AddFluidDropAction implements IAction {

        private final ResourceLocation id;
        private final JDTBlockStateSpec input;
        private final JDTBlockStateSpec output;
        private final ResourceLocation catalyst;

        private AddFluidDropAction(ResourceLocation id, JDTBlockStateSpec input, JDTBlockStateSpec output, ResourceLocation catalyst) {
            this.id = id;
            this.input = input;
            this.output = output;
            this.catalyst = catalyst;
        }

        @Override
        public void apply() {
            CraftTweakerHelper.registerRecipe(id, new FluidDropDataRecipe(id, input, output, catalyst));
        }

        @Override
        public String describe() {
            return "Adding JustDireThings fluid drop recipe " + id;
        }

        @Override
        public boolean validate() {
            return id != null && input != null && output != null && catalyst != null;
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings fluid drop recipes need a valid id, input, output, and catalyst";
        }
    }

    private static final class RemoveRecipeByIdAction implements IAction {

        private final ResourceLocation id;
        private final Class<?> recipeClass;
        private final String recipeType;

        private RemoveRecipeByIdAction(ResourceLocation id, Class<?> recipeClass, String recipeType) {
            this.id = id;
            this.recipeClass = recipeClass;
            this.recipeType = recipeType;
        }

        @Override
        public void apply() {
            CraftTweakerHelper.removeRecipes(recipe -> recipeClass.isInstance(recipe) && id.equals(recipe.getRegistryName()));
        }

        @Override
        public String describe() {
            return "Removing JustDireThings " + recipeType + " recipe " + id;
        }

        @Override
        public boolean validate() {
            return id != null;
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings recipe removal needs a valid recipe id";
        }
    }

    private static final class RemoveGooSpreadByInputAction implements IAction {

        private final JDTBlockStateSpec input;

        private RemoveGooSpreadByInputAction(JDTBlockStateSpec input) {
            this.input = input;
        }

        @Override
        public void apply() {
            CraftTweakerHelper.removeRecipes(recipe -> recipe instanceof GooSpreadDataRecipe
                    && CraftTweakerHelper.sameBlockStateSpec(((GooSpreadDataRecipe) recipe).getInput(), input));
        }

        @Override
        public String describe() {
            return "Removing JustDireThings goo spread recipes by input";
        }

        @Override
        public boolean validate() {
            return input != null;
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings goo spread removal needs a valid input block state";
        }
    }

    private static final class RemoveGooSpreadTagAction implements IAction {

        private final String inputTag;

        private RemoveGooSpreadTagAction(String inputTag) {
            this.inputTag = inputTag;
        }

        @Override
        public void apply() {
            CraftTweakerHelper.removeRecipes(recipe -> recipe instanceof GooSpreadTagDataRecipe
                    && inputTag.equals(((GooSpreadTagDataRecipe) recipe).getInputTag()));
        }

        @Override
        public String describe() {
            return "Removing JustDireThings goo spread tag recipes for " + inputTag;
        }

        @Override
        public boolean validate() {
            return inputTag != null && !inputTag.trim().isEmpty();
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings goo spread tag removal needs a valid tag";
        }
    }

    private static final class RemoveFluidDropAction implements IAction {

        private final JDTBlockStateSpec input;
        private final ResourceLocation catalyst;

        private RemoveFluidDropAction(JDTBlockStateSpec input, ResourceLocation catalyst) {
            this.input = input;
            this.catalyst = catalyst;
        }

        @Override
        public void apply() {
            CraftTweakerHelper.removeRecipes(recipe -> recipe instanceof FluidDropDataRecipe
                    && CraftTweakerHelper.sameBlockStateSpec(((FluidDropDataRecipe) recipe).getInput(), input)
                    && catalyst.equals(((FluidDropDataRecipe) recipe).getCatalystId()));
        }

        @Override
        public String describe() {
            return "Removing JustDireThings fluid drop recipes by input and catalyst";
        }

        @Override
        public boolean validate() {
            return input != null && catalyst != null;
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings fluid drop removal needs a valid input block state and catalyst";
        }
    }
}
