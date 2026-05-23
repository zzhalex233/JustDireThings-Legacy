package com.zzhalex.justdirethings.common.util;

import com.zzhalex.justdirethings.config.JDTConfig;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariableProvider;

public class ConfigValueComponentProcessor implements IComponentProcessor {

    private static final Pattern CONFIG_VALUE = Pattern.compile("#([^#]+)#");
    private String text;

    @Override
    public void setup(IVariableProvider<String> variables) {
        text = variables.has("text") ? variables.get("text") : "";
    }

    @Override
    public String process(String key) {
        return "text".equals(key) ? replaceConfigValues(text) : null;
    }

    private static String replaceConfigValues(String rawText) {
        Matcher matcher = CONFIG_VALUE.matcher(rawText);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            Object value = resolveConfigValue(matcher.group(1));
            if (value != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(value)));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static Object resolveConfigValue(String key) {
        Object value = resolveConfigField(key);
        if (value != null) {
            return value;
        }
        int separator = key.lastIndexOf('.');
        return separator >= 0 ? resolveConfigField(key.substring(separator + 1)) : null;
    }

    private static Object resolveConfigField(String key) {
        try {
            Field field = JDTConfig.class.getField(toFieldName(key));
            return field.get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static String toFieldName(String key) {
        StringBuilder name = new StringBuilder();
        boolean upperNext = false;
        for (int i = 0; i < key.length(); i++) {
            char value = key.charAt(i);
            if (value == '_' || value == '-' || value == '.') {
                upperNext = name.length() > 0;
            } else if (upperNext) {
                name.append(Character.toUpperCase(value));
                upperNext = false;
            } else {
                name.append(value);
            }
        }
        return name.toString();
    }
}
