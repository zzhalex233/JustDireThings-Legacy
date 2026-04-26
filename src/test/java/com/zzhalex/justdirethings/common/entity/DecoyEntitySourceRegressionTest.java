package com.zzhalex.justdirethings.common.entity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DecoyEntitySourceRegressionTest {

    @Test
    void doesNotAssumeAttackDamageAttributeIsPreRegistered() throws IOException {
        String source = Files.readString(
                Path.of("src/main/java/com/zzhalex/justdirethings/common/entity/EntityDecoy.java"),
                StandardCharsets.UTF_8
        );

        assertFalse(
                source.contains("getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue"),
                "EntityLiving does not pre-register ATTACK_DAMAGE in 1.12; direct chaining crashes during Decoy construction."
        );
    }
}
