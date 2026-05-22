package com.zzhalex.justdirethings.common.event;

import com.zzhalex.justdirethings.common.block.BlockTimeCrystalBlock;
import com.zzhalex.justdirethings.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class TimeCrystalBlockEventHandler {

    public static final TimeCrystalBlockEventHandler INSTANCE = new TimeCrystalBlockEventHandler();

    private TimeCrystalBlockEventHandler() {
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        RayTraceResult result = event.getRayTraceResult();
        if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        World world = event.getEntity().world;
        if (world == null || world.isRemote) {
            return;
        }

        BlockPos pos = result.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof BlockTimeCrystalBlock) {
            world.playSound(null, pos, ModSounds.AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }
}
