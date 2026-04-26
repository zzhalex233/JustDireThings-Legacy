package com.zzhalex.justdirethings.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JDTConfigDefaultsTest {

    @Test
    void fallbackCompatCanBeEnabledByDefault() {
        assertTrue(JDTConfig.enableFallbackCompat);
    }

    @Test
    void configClassExposesOnlyMutablePublicStaticFields() {
        for (Field field : JDTConfig.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
                continue;
            }

            assertFalse(
                    Modifier.isFinal(modifiers) && (field.getType().isPrimitive() || field.getType() == String.class),
                    () -> "Forge config sync will try to write public static final simple field " + field.getName()
            );
        }
    }
}
