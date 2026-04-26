package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockExperienceHolder extends BlockAttachedMachineBase {

    public BlockExperienceHolder() {
        this("experienceholder");
    }

    public BlockExperienceHolder(String registryPath) {
        super(registryPath, ModContainers.GUI_EXPERIENCE_HOLDER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileExperienceHolder();
    }
}
