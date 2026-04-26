package com.zzhalex.justdirethings.common.portal;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public final class PortalDirectTeleporter implements ITeleporter {

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        // JDT portals set the final exit position after dimension transfer.
        // Do not ask vanilla to search/create Nether portals: non-player entities can crash there.
    }

    @Override
    public boolean isVanilla() {
        return false;
    }
}
