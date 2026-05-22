package com.zzhalex.justdirethings.common.block.group;

import com.zzhalex.justdirethings.Reference;
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

    public static boolean isNoAutoClick(Block block) {
        return false;
    }

    public static boolean isTickSpeedDenied(Block block) {
        ResourceLocation id = block == null ? null : block.getRegistryName();
        if (id == null) {
            return false;
        }
        return Reference.MOD_ID.equals(id.getNamespace()) && "inventory_holder".equals(id.getPath());
    }

    public static boolean isPhaseDenied(Block block) {
        if (block == null) {
            return false;
        }
        return block == Blocks.BARRIER
                || block == Blocks.BEDROCK
                || block == Blocks.END_PORTAL
                || block == Blocks.END_PORTAL_FRAME
                || block == Blocks.END_GATEWAY
                || block == Blocks.STRUCTURE_BLOCK
                || isModernJigsawFallback(block)
                || block == Blocks.PORTAL;
    }

    private static boolean isModernJigsawFallback(Block block) {
        ResourceLocation id = block.getRegistryName();
        return id != null && "minecraft".equals(id.getNamespace()) && "jigsaw".equals(id.getPath());
    }

    private static boolean isOptionalPowahPlayerTransmitter(Block block) {
        ResourceLocation id = block.getRegistryName();
        if (id == null || !"powah".equals(id.getNamespace())) {
            return false;
        }
        return id.getPath().toLowerCase(Locale.ROOT).contains("player_transmitter");
    }
}
