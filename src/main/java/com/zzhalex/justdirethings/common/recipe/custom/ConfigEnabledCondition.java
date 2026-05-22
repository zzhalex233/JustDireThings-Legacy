package com.zzhalex.justdirethings.common.recipe.custom;

import com.google.gson.JsonObject;
import com.zzhalex.justdirethings.config.JDTConfig;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class ConfigEnabledCondition implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        String key = JsonUtils.getString(json, "key");
        if ("enableSmithingTemplates".equals(key)) {
            return () -> JDTConfig.enableSmithingTemplates;
        }
        return () -> false;
    }
}
