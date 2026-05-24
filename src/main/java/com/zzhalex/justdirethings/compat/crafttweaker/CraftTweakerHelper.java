package com.zzhalex.justdirethings.compat.crafttweaker;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.recipe.custom.JDTBlockStateSpec;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

final class CraftTweakerHelper {

    private CraftTweakerHelper() {
    }

    static ResourceLocation recipeId(String name) {
        if (name == null || name.trim().isEmpty()) {
            CraftTweakerAPI.logError("JustDireThings recipe id cannot be empty");
            return null;
        }
        try {
            String id = name.trim();
            return id.indexOf(':') >= 0 ? new ResourceLocation(id) : new ResourceLocation(Reference.MOD_ID, id);
        } catch (RuntimeException exception) {
            CraftTweakerAPI.logError("Invalid JustDireThings recipe id: " + name, exception);
            return null;
        }
    }

    static String stationRecipeId(String name) {
        if (name == null || name.trim().isEmpty()) {
            CraftTweakerAPI.logError("JustDireThings upgrade station recipe id cannot be empty");
            return null;
        }
        return name.trim();
    }

    static ItemStack itemStack(IItemStack stack) {
        return stack == null ? ItemStack.EMPTY : CraftTweakerMC.getItemStack(stack);
    }

    static ResourceLocation itemId(IItemStack stack) {
        ItemStack itemStack = itemStack(stack);
        if (itemStack.isEmpty() || itemStack.getItem().getRegistryName() == null) {
            CraftTweakerAPI.logError("Expected a registered item stack, got " + stack);
            return null;
        }
        return itemStack.getItem().getRegistryName();
    }

    static JDTBlockStateSpec blockStateSpec(IBlockState state) {
        if (state == null || !(state.getInternal() instanceof net.minecraft.block.state.IBlockState)) {
            CraftTweakerAPI.logError("Expected a block state, got " + state);
            return null;
        }
        return JDTBlockStateSpec.fromState((net.minecraft.block.state.IBlockState) state.getInternal());
    }

    static JDTBlockStateSpec blockStateSpec(IItemStack stack) {
        ItemStack itemStack = itemStack(stack);
        if (itemStack.isEmpty()) {
            CraftTweakerAPI.logError("Expected a block item stack, got " + stack);
            return null;
        }
        Block block = Block.getBlockFromItem(itemStack.getItem());
        if (block == Blocks.AIR) {
            CraftTweakerAPI.logError("Expected a block item stack, got " + stack);
            return null;
        }
        int meta = itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE ? 0 : itemStack.getItemDamage();
        return JDTBlockStateSpec.fromState(block.getStateFromMeta(meta));
    }

    static JDTBlockStateSpec blockStateSpec(ILiquidStack stack) {
        FluidStack fluidStack = CraftTweakerMC.getLiquidStack(stack);
        if (fluidStack == null || fluidStack.getFluid() == null || fluidStack.getFluid().getBlock() == null) {
            CraftTweakerAPI.logError("Expected a placeable liquid stack, got " + stack);
            return null;
        }
        return JDTBlockStateSpec.fromState(fluidStack.getFluid().getBlock().getDefaultState());
    }

    static void registerRecipe(ResourceLocation id, IRecipe recipe) {
        removeRecipes(existing -> id.equals(existing.getRegistryName()));
        recipe.setRegistryName(id);
        ForgeRegistries.RECIPES.register(recipe);
    }

    static int removeRecipes(Predicate<IRecipe> predicate) {
        List<ResourceLocation> removingRecipes = new ArrayList<>();
        for (IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
            if (recipe.getRegistryName() != null && predicate.test(recipe)) {
                removingRecipes.add(recipe.getRegistryName());
            }
        }
        removingRecipes.forEach(recipe -> RegistryManager.ACTIVE.getRegistry(GameData.RECIPES).remove(recipe));
        return removingRecipes.size();
    }

    static boolean sameBlockStateSpec(JDTBlockStateSpec first, JDTBlockStateSpec second) {
        return first != null
                && second != null
                && first.getBlockId().equals(second.getBlockId())
                && first.getProperties().equals(second.getProperties());
    }
}
