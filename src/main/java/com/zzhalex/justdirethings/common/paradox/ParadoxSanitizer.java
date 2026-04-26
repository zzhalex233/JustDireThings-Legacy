package com.zzhalex.justdirethings.common.paradox;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ParadoxSanitizer {

    private static final Set<String> RESTRICTIVE_FIELDS = new HashSet<>(Arrays.asList(
            "id",
            "IsBaby",
            "UUID",
            "Health",
            "Motion",
            "Rotation",
            "Fire",
            "CustomName",
            "NoAI",
            "PersistenceRequired",
            "Silent",
            "Color",
            "Sheared",
            "Variant",
            "FromBucket",
            "Age",
            "VillagerData",
            "Xp",
            "LastRestock",
            "RestocksToday",
            "Offers",
            "EggLayTime"
    ));

    private static final Set<String> DENY_INVENTORY_FIELDS = new HashSet<>(Arrays.asList(
            "ArmorItems",
            "HandItems",
            "Items",
            "SaddleItem",
            "Inventory"
    ));

    private ParadoxSanitizer() {
    }

    public static NBTTagCompound restrictive(NBTTagCompound input) {
        NBTTagCompound output = new NBTTagCompound();
        if (input == null) {
            return output;
        }

        for (String key : RESTRICTIVE_FIELDS) {
            if (input.hasKey(key)) {
                output.setTag(key, copyTag(input.getTag(key)));
            }
        }
        return output;
    }

    public static NBTTagCompound denyInventory(NBTTagCompound input) {
        NBTTagCompound output = new NBTTagCompound();
        if (input == null) {
            return output;
        }

        for (String key : input.getKeySet()) {
            if (!DENY_INVENTORY_FIELDS.contains(key)) {
                output.setTag(key, copyTag(input.getTag(key)));
            }
        }
        return output;
    }

    private static NBTBase copyTag(NBTBase input) {
        return input == null ? new NBTTagCompound() : input.copy();
    }
}
