package com.zzhalex.justdirethings.compat.crafttweaker;

import com.zzhalex.justdirethings.common.recipe.CustomUpgradeStationRecipe;
import com.zzhalex.justdirethings.registry.ModRecipes;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.justdirethings.UpgradeStation")
@ZenRegister
public final class CraftTweakerUpgradeStation {

    private CraftTweakerUpgradeStation() {
    }

    @ZenMethod
    public static void addRecipe(String name, IItemStack base, IItemStack addition, IItemStack output) {
        addRecipe(name, null, base, addition, output);
    }

    @ZenMethod
    public static void addRecipe(String name, IItemStack template, IItemStack base, IItemStack addition, IItemStack output) {
        CraftTweakerAPI.apply(new AddUpgradeStationRecipeAction(
                CraftTweakerHelper.stationRecipeId(name),
                CraftTweakerHelper.itemStack(template),
                CraftTweakerHelper.itemStack(base),
                CraftTweakerHelper.itemStack(addition),
                CraftTweakerHelper.itemStack(output)));
    }

    @ZenMethod
    public static void removeRecipe(String name) {
        CraftTweakerAPI.apply(new RemoveUpgradeStationRecipeAction(CraftTweakerHelper.stationRecipeId(name)));
    }

    @ZenMethod
    public static void removeByOutput(IItemStack output) {
        CraftTweakerAPI.apply(new RemoveUpgradeStationRecipeByOutputAction(CraftTweakerHelper.itemStack(output)));
    }

    private static final class AddUpgradeStationRecipeAction implements IAction {

        private final String id;
        private final ItemStack template;
        private final ItemStack base;
        private final ItemStack addition;
        private final ItemStack output;

        private AddUpgradeStationRecipeAction(String id, ItemStack template, ItemStack base, ItemStack addition, ItemStack output) {
            this.id = id;
            this.template = template;
            this.base = base;
            this.addition = addition;
            this.output = output;
        }

        @Override
        public void apply() {
            ModRecipes.addUpgradeStationRecipe(new CustomUpgradeStationRecipe(id, template, base, addition, output));
        }

        @Override
        public String describe() {
            return "Adding JustDireThings upgrade station recipe " + id;
        }

        @Override
        public boolean validate() {
            return id != null && !base.isEmpty() && !addition.isEmpty() && !output.isEmpty();
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings upgrade station recipes need a valid id, base, addition, and output";
        }
    }

    private static final class RemoveUpgradeStationRecipeAction implements IAction {

        private final String id;

        private RemoveUpgradeStationRecipeAction(String id) {
            this.id = id;
        }

        @Override
        public void apply() {
            ModRecipes.removeUpgradeStationRecipe(id);
        }

        @Override
        public String describe() {
            return "Removing JustDireThings upgrade station recipe " + id;
        }

        @Override
        public boolean validate() {
            return id != null;
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings upgrade station recipe removal needs a valid id";
        }
    }

    private static final class RemoveUpgradeStationRecipeByOutputAction implements IAction {

        private final ItemStack output;

        private RemoveUpgradeStationRecipeByOutputAction(ItemStack output) {
            this.output = output;
        }

        @Override
        public void apply() {
            ModRecipes.removeUpgradeStationRecipesByOutput(output);
        }

        @Override
        public String describe() {
            return "Removing JustDireThings upgrade station recipes by output";
        }

        @Override
        public boolean validate() {
            return !output.isEmpty();
        }

        @Override
        public String describeInvalid() {
            return "JustDireThings upgrade station recipe removal needs a valid output";
        }
    }
}
