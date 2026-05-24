package com.zzhalex.justdirethings;

import com.zzhalex.justdirethings.common.event.AbilityRuntimeEventHandler;
import com.zzhalex.justdirethings.common.event.CustomGooEventHandler;
import com.zzhalex.justdirethings.common.event.FluidDropEventHandler;
import com.zzhalex.justdirethings.common.event.GooSoilEventHandler;
import com.zzhalex.justdirethings.common.event.PolymorphicWandEventHandler;
import com.zzhalex.justdirethings.common.event.TimeCrystalBlockEventHandler;
import com.zzhalex.justdirethings.common.event.ToolMiningAbilityHandler;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import net.minecraft.nbt.NBTTagCompound;
import com.zzhalex.justdirethings.network.message.MessageSyncAbilityCooldowns;
import com.zzhalex.justdirethings.common.world.PortalChunkKeeper;
import com.zzhalex.justdirethings.registry.ModContainers;
import com.zzhalex.justdirethings.registry.ModEntities;
import com.zzhalex.justdirethings.registry.ModRecipes;
import com.zzhalex.justdirethings.registry.ModTileEntities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.List;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        ModTileEntities.register();
        ModContainers.registerGuiHandler();
        MinecraftForge.EVENT_BUS.register(AbilityRuntimeEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(CustomGooEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FluidDropEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GooSoilEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PolymorphicWandEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(TimeCrystalBlockEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ToolMiningAbilityHandler.INSTANCE);
        PortalChunkKeeper.initialize();
    }

    public void init(FMLInitializationEvent event) {
        ModRecipes.register();
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public void discoverThings(EntityPlayer player, Ability ability, ItemStack stack) {
        // Client-only scanner rendering is bridged by ClientProxy.
    }

    public void syncAbilityCooldowns(List<MessageSyncAbilityCooldowns.Entry> entries) {
        // Client-only HUD state is bridged by ClientProxy.
    }

    public void syncCustomGooTile(BlockPos pos, NBTTagCompound tag, boolean remove) {
        // Client-only custom goo renderer state is bridged by ClientProxy.
    }

    public void spawnTimeCrystalChargeParticle(World world, double startX, double startY, double startZ, double targetX, double targetY, double targetZ, float red, float green, float blue) {
        // Client-only particle rendering is bridged by ClientProxy.
    }

    public void spawnItemFlowParticle(World world, double startX, double startY, double startZ, double targetX, double targetY, double targetZ, ItemStack stack, int ticksPerBlock) {
        // Client-only particle rendering is bridged by ClientProxy.
    }

    public void openMachineSettingsCopierScreen(ItemStack stack) {
        // Client-only screen opening is bridged by ClientProxy.
    }
}
