package com.zzhalex.justdirethings.common.item.group;

import net.minecraft.init.Items;
import net.minecraft.item.Item;

public final class JDTItemGroups {

    private JDTItemGroups() {
    }

    public static boolean isFuelCanisterDenied(Item item) {
        return item == Items.LAVA_BUCKET;
    }

    public static boolean isParadoxAbsorbDenied(Item item) {
        return item == Item.getItemFromBlock(net.minecraft.init.Blocks.BEDROCK);
    }
}
