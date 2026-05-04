package com.zzhalex.justdirethings.common.block.group;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockBed;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public final class JDTBlockGroups {

    private JDTBlockGroups() {
    }

    public static boolean isEclipseGateDenied(Block block) {
        if (block == null) {
            return false;
        }
        if (block == Blocks.PORTAL || block == Blocks.END_PORTAL || block == Blocks.END_GATEWAY) {
            return true;
        }
        if (block instanceof BlockDoor) {
            return true;
        }
        return isOptionalPowahPlayerTransmitter(block);
    }

    public static boolean isSwapperDenied(Block block) {
        if (block == null) {
            return false;
        }
        if (block == Blocks.PISTON_HEAD
                || block == Blocks.PISTON_EXTENSION
                || block == Blocks.BEDROCK
                || block == Blocks.END_PORTAL_FRAME) {
            return true;
        }
        if (block == Blocks.PORTAL || block == Blocks.END_PORTAL || block == Blocks.END_GATEWAY) {
            return true;
        }
        if (block instanceof BlockDoor || block instanceof BlockBed) {
            return true;
        }
        return false;
    }

    private static boolean isOptionalPowahPlayerTransmitter(Block block) {
        ResourceLocation id = block.getRegistryName();
        if (id == null || !"powah".equals(id.getNamespace())) {
            return false;
        }
        return id.getPath().toLowerCase(Locale.ROOT).contains("player_transmitter");
    }
}
