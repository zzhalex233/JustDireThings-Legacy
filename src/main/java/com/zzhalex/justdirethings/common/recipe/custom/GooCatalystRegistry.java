package com.zzhalex.justdirethings.common.recipe.custom;

import com.zzhalex.justdirethings.registry.ModContentBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GooCatalystRegistry {

    private static final Map<JDTBlockStateSpec, CustomGooEntry> CUSTOM_CATALYSTS = new LinkedHashMap<>();

    private GooCatalystRegistry() {
    }

    public static void registerCustomGoo(JDTBlockStateSpec catalyst, int tier, List<ItemStack> revivalItems) {
        List<ItemStack> copiedRevivalItems = copyNonEmptyStacks(revivalItems);
        if (catalyst == null || tier < 1 || copiedRevivalItems.isEmpty()) {
            return;
        }
        CUSTOM_CATALYSTS.put(normalize(catalyst), new CustomGooEntry(tier, copiedRevivalItems));
    }

    public static int getCustomTier(JDTBlockStateSpec catalyst) {
        CustomGooEntry entry = CUSTOM_CATALYSTS.get(normalize(catalyst));
        return entry == null ? -1 : entry.tier;
    }

    public static int effectiveTier(JDTBlockStateSpec catalyst, int fallbackTier) {
        int customTier = getCustomTier(catalyst);
        return customTier >= 1 ? customTier : fallbackTier;
    }

    public static boolean isCustomGoo(JDTBlockStateSpec catalyst) {
        return CUSTOM_CATALYSTS.containsKey(normalize(catalyst));
    }

    public static boolean canBeCustomGoo(JDTBlockStateSpec catalyst) {
        if (catalyst == null) {
            return false;
        }
        net.minecraft.block.state.IBlockState state = catalyst.toBlockState();
        Block block = state.getBlock();
        return block != Blocks.AIR && !block.hasTileEntity(state) && !(block instanceof BlockLiquid) && !(block instanceof IFluidBlock);
    }

    public static boolean validRevivalItem(JDTBlockStateSpec catalyst, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (ItemStack revivalItem : revivalItemsFor(catalyst)) {
            if (ItemStack.areItemsEqual(revivalItem, stack) && ItemStack.areItemStackTagsEqual(revivalItem, stack)) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> revivalItemsFor(JDTBlockStateSpec catalyst) {
        CustomGooEntry entry = CUSTOM_CATALYSTS.get(normalize(catalyst));
        return entry == null ? Collections.emptyList() : copyNonEmptyStacks(entry.revivalItems);
    }

    public static List<ItemStack> catalystsForTier(int tierRequirement) {
        List<ItemStack> stacks = new ArrayList<>();
        addTierCatalyst(stacks, tierRequirement, 1, ModContentBlocks.GOO_BLOCK_TIER1);
        addTierCatalyst(stacks, tierRequirement, 2, ModContentBlocks.GOO_BLOCK_TIER2);
        addTierCatalyst(stacks, tierRequirement, 3, ModContentBlocks.GOO_BLOCK_TIER3);
        addTierCatalyst(stacks, tierRequirement, 4, ModContentBlocks.GOO_BLOCK_TIER4);
        for (Map.Entry<JDTBlockStateSpec, CustomGooEntry> entry : CUSTOM_CATALYSTS.entrySet()) {
            if (entry.getValue().tier >= tierRequirement) {
                stacks.addAll(GooFluidRecipeRuntime.itemStacksForBlockState(entry.getKey()));
            }
        }
        return stacks;
    }

    public static List<ItemStack> itemStacksForCatalyst(JDTBlockStateSpec catalyst) {
        if (catalyst == null) {
            return new ArrayList<>();
        }
        return GooFluidRecipeRuntime.itemStacksForBlockState(normalize(catalyst));
    }

    public static void clearCustomCatalystsForTesting() {
        CUSTOM_CATALYSTS.clear();
    }

    private static void addTierCatalyst(List<ItemStack> stacks, int tierRequirement, int tier, Block block) {
        if (tier >= tierRequirement) {
            Item item = Item.getItemFromBlock(block);
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
    }

    private static JDTBlockStateSpec normalize(JDTBlockStateSpec catalyst) {
        return catalyst == null ? null : catalyst.withoutProperty("alive");
    }

    private static List<ItemStack> copyNonEmptyStacks(List<ItemStack> stacks) {
        List<ItemStack> copies = new ArrayList<>();
        if (stacks == null) {
            return copies;
        }
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                copies.add(stack.copy());
            }
        }
        return copies;
    }

    private static final class CustomGooEntry {

        private final int tier;
        private final List<ItemStack> revivalItems;

        private CustomGooEntry(int tier, List<ItemStack> revivalItems) {
            this.tier = tier;
            this.revivalItems = revivalItems;
        }
    }
}
