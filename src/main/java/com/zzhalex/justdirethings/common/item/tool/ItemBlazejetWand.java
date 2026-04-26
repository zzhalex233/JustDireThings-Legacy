package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.ItemToggleableTool;

public class ItemBlazejetWand extends ItemToggleableTool {

    public static final int DURABILITY = 200;

    public ItemBlazejetWand() {
        setMaxDamage(DURABILITY);
        addSupportedAbilities(Ability.LAVAREPAIR, Ability.AIRBURST);
    }
}
