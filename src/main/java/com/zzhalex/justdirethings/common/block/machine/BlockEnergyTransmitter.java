package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileEnergyTransmitter;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockEnergyTransmitter extends BlockAttachedMachineBase {

    public BlockEnergyTransmitter() {
        this("energytransmitter");
    }

    public BlockEnergyTransmitter(String registryPath) {
        super(registryPath, ModContainers.GUI_ENERGY_TRANSMITTER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEnergyTransmitter();
    }
}
