package com.zzhalex.justdirethings.common.recipe;

import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.data.tool.ToolState;
import com.zzhalex.justdirethings.data.tool.ToolStateIO;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class UpgradeStationRecipe {

    private final String id;

    protected UpgradeStationRecipe(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean matches(ItemStack template, ItemStack base, ItemStack addition) {
        return false;
    }

    public ItemStack createOutputStack(ItemStack template, ItemStack base, ItemStack addition) {
        return ItemStack.EMPTY;
    }

    public abstract ToolState createOutput(ToolState... inputs);

    protected static boolean sameItem(ItemStack stack, Item expected) {
        return expected != null && !stack.isEmpty() && stack.getItem() == expected;
    }

    protected static ItemStack copyWithItem(ItemStack source, Item resultItem) {
        if (resultItem == null || source.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack output = new ItemStack(resultItem);
        output.setItemDamage(source.getItemDamage());
        if (source.hasTagCompound()) {
            output.setTagCompound(source.getTagCompound().copy());
        }
        return output;
    }

    protected static ToolState readToolState(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(JDTDataKeys.TOOL_STATE)) {
            return ToolStateIO.read(stack.getTagCompound().getCompoundTag(JDTDataKeys.TOOL_STATE));
        }
        return new ToolState();
    }

    protected static void writeToolState(ItemStack stack, ToolState state) {
        if (stack.isEmpty()) {
            return;
        }

        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
        }
        root.setTag(JDTDataKeys.TOOL_STATE, ToolStateIO.write(state));
        stack.setTagCompound(root);
    }
}
