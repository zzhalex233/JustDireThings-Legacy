package com.zzhalex.justdirethings.compat.content;

import net.minecraft.util.ResourceLocation;

public enum CompatContentKey {
    BAMBOO(null, "futuremc:bamboo", "minecraft:reeds"),
    HONEY_BOTTLE(null, "futuremc:honey_bottle", "minecraft:sugar"),
    NETHERITE_PICKAXE(null, "futuremc:netherite_pickaxe", "minecraft:diamond_pickaxe"),
    NETHERITE_SCRAP(null, "futuremc:netherite_scrap", "minecraft:obsidian"),
    NETHERITE_BLOCK(null, "futuremc:netherite_block", "minecraft:obsidian"),
    AMETHYST_SHARD(null, null, "minecraft:quartz"),
    PHANTOM_MEMBRANE(null, null, "minecraft:ghast_tear"),
    TARGET(null, null, "minecraft:compass"),
    SCULK(null, null, "minecraft:obsidian"),
    SCULK_CATALYST(null, null, "minecraft:nether_star"),
    SCULK_SHRIEKER(null, null, "minecraft:dragon_breath"),
    CALIBRATED_SCULK_SENSOR(null, null, "minecraft:observer"),
    ECHO_SHARD(null, null, "minecraft:popped_chorus_fruit");

    private final String vanillaId;
    private final String futureMcId;
    private final String fallbackId;

    CompatContentKey(String vanillaId, String futureMcId, String fallbackId) {
        this.vanillaId = vanillaId;
        this.futureMcId = futureMcId;
        this.fallbackId = fallbackId;
    }

    public ResourceLocation getVanillaId() {
        return parse(vanillaId);
    }

    public ResourceLocation getFutureMcId() {
        return parse(futureMcId);
    }

    public ResourceLocation getFallbackId() {
        return parse(fallbackId);
    }

    public String getFallbackIdString() {
        return fallbackId;
    }

    private static ResourceLocation parse(String id) {
        return id == null ? null : new ResourceLocation(id);
    }
}
