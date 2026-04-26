package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileParadoxMachine;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockParadoxMachine extends BlockMachineBase {

    public BlockParadoxMachine() {
        this("paradoxmachine");
    }

    public BlockParadoxMachine(String registryPath) {
        super(registryPath, ModContainers.GUI_PARADOX_MACHINE);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileParadoxMachine();
    }
}
