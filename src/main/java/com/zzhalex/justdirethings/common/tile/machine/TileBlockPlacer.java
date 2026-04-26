package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;

public class TileBlockPlacer extends TileTimedMachineBase {

    public TileBlockPlacer() {
        super(1);
    }

    @Override
    protected boolean performWork() {
        return MachineActionHelper.placeFirstBlock(getItemHandler(), world, MachineActionHelper.targetPos(this), MachineActionHelper.getFacing(this));
    }

    public static class T1 extends TileBlockPlacer {
    }

    public static class T2 extends TileBlockPlacer {
        // PARITY STUB: Upstream BlockPlacerT2BE adds energy, area placement, and filtering.
    }
}
