package com.zzhalex.justdirethings.common.entity.group;

import com.zzhalex.justdirethings.registry.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class JDTEntityGroups {

    private static final Set<String> CREATURE_CATCHER_DENY = immutableSet(
            "minecraft:ender_dragon"
    );
    private static final Set<String> NO_AI_DENY = immutableSet(
            "minecraft:ender_dragon",
            "minecraft:wither"
    );
    private static final Set<String> NO_EARTHQUAKE = immutableSet(
            "minecraft:ender_dragon",
            "minecraft:wither"
    );
    private static final Set<String> POLYMORPHIC_PEACEFUL = immutableSet(
            "minecraft:sheep",
            "minecraft:pig",
            "minecraft:cow",
            "minecraft:mooshroom",
            "minecraft:chicken",
            "minecraft:bat",
            "minecraft:villager",
            "minecraft:squid",
            "minecraft:ocelot",
            "minecraft:wolf",
            "minecraft:horse",
            "minecraft:rabbit",
            "minecraft:donkey",
            "minecraft:mule",
            "minecraft:polar_bear",
            "minecraft:llama",
            "minecraft:parrot"
    );
    private static final Set<String> POLYMORPHIC_HOSTILE = immutableSet(
            "minecraft:zombie",
            "minecraft:skeleton",
            "minecraft:creeper",
            "minecraft:spider",
            "minecraft:enderman",
            "minecraft:silverfish",
            "minecraft:pig_zombie",
            "minecraft:ghast",
            "minecraft:blaze",
            "minecraft:slime",
            "minecraft:witch",
            "minecraft:endermite",
            "minecraft:stray",
            "minecraft:wither_skeleton",
            "minecraft:skeleton_horse",
            "minecraft:zombie_horse",
            "minecraft:zombie_villager",
            "minecraft:husk",
            "minecraft:guardian",
            "minecraft:evocation_illager",
            "minecraft:vex",
            "minecraft:vindication_illager",
            "minecraft:shulker"
    );
    private static final Set<String> POLYMORPHIC_TARGET_DENY = immutableSet(
            "minecraft:ender_dragon",
            "minecraft:wither"
    );

    private JDTEntityGroups() {
    }

    public static boolean isCreatureCatcherDenied(String entityId) {
        return CREATURE_CATCHER_DENY.contains(normalize(entityId));
    }

    public static boolean isNoAiDenied(String entityId) {
        return NO_AI_DENY.contains(normalize(entityId));
    }

    public static boolean isEarthquakeDenied(String entityId) {
        return NO_EARTHQUAKE.contains(normalize(entityId));
    }

    public static boolean isPolymorphicPeaceful(String entityId) {
        return POLYMORPHIC_PEACEFUL.contains(normalize(entityId));
    }

    public static boolean isPolymorphicHostile(String entityId) {
        return POLYMORPHIC_HOSTILE.contains(normalize(entityId));
    }

    public static boolean isPolymorphicTargetDenied(String entityId) {
        return POLYMORPHIC_TARGET_DENY.contains(normalize(entityId));
    }

    public static boolean canRandomlyPolymorph(String entityId) {
        return isPolymorphicPeaceful(entityId) || isPolymorphicHostile(entityId);
    }

    public static boolean isTeleportingNotSupported(Entity entity) {
        if (entity == null) {
            return false;
        }
        ResourceLocation entityId = EntityList.getKey(entity);
        if (entityId == null) {
            return false;
        }
        return entityId.equals(ModEntities.TIME_WAND_ENTITY_ID)
                || entityId.equals(ModEntities.PARADOX_ENTITY_ID);
    }

    public static Set<String> getPolymorphicPeacefulEntities() {
        return POLYMORPHIC_PEACEFUL;
    }

    public static Set<String> getPolymorphicHostileEntities() {
        return POLYMORPHIC_HOSTILE;
    }

    private static Set<String> immutableSet(String... ids) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(ids)));
    }

    private static String normalize(String entityId) {
        if (entityId == null) {
            return "";
        }
        return entityId.trim().toLowerCase(Locale.ROOT);
    }
}
