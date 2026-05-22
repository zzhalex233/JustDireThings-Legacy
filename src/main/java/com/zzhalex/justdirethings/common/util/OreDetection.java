package com.zzhalex.justdirethings.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDetection {

    private OreDetection() {
    }

    public static boolean isOreBlock(IBlockState state) {
        if (state == null) {
            return false;
        }

        Block block = state.getBlock();
        if (block == null) {
            return false;
        }

        if (block.getRegistryName() != null) {
            String path = block.getRegistryName().getPath();
            if (path.endsWith("_ore") || path.contains("_ore_") || path.startsWith("raw_")) {
                return true;
            }
        }

        Item item = Item.getItemFromBlock(block);
        if (item == null) {
            return false;
        }

        ItemStack oreStack = new ItemStack(item, 1, block.getMetaFromState(state));
        ItemStack wildcardStack = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
        return hasOreDictionaryPrefix(oreStack, "ore")
                || hasOreDictionaryPrefix(wildcardStack, "ore")
                || hasOreDictionaryPrefix(oreStack, "cluster")
                || hasOreDictionaryPrefix(wildcardStack, "cluster");
    }

    private static boolean hasOreDictionaryPrefix(ItemStack stack, String prefix) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if (OreDictionary.getOreName(oreId).startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
