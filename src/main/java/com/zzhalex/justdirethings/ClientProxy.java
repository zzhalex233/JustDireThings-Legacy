package com.zzhalex.justdirethings;

import com.zzhalex.justdirethings.client.ClientRegistration;
import com.zzhalex.justdirethings.client.gui.upstream.MachineSettingsCopierScreen;
import com.zzhalex.justdirethings.common.goo.CustomGooRuntime;
import com.zzhalex.justdirethings.client.overlay.ClientAbilityCooldowns;
import com.zzhalex.justdirethings.client.particle.TimeCrystalParticles;
import com.zzhalex.justdirethings.client.render.ThingFinder;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.data.tool.AbilityCooldown;
import com.zzhalex.justdirethings.network.message.MessageSyncAbilityCooldowns;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientRegistration.initialize();
    }

    @Override
    public void discoverThings(EntityPlayer player, Ability ability, ItemStack stack) {
        ThingFinder.discover(player, ability, stack);
    }

    @Override
    public void syncAbilityCooldowns(List<MessageSyncAbilityCooldowns.Entry> entries) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return;
        }
        minecraft.addScheduledTask(() -> {
            Map<EntityEquipmentSlot, List<AbilityCooldown>> cooldowns = new EnumMap<>(EntityEquipmentSlot.class);
            if (entries != null) {
                for (MessageSyncAbilityCooldowns.Entry entry : entries) {
                    if (entry.getSlot() == null || entry.getCooldown() == null) {
                        continue;
                    }
                    cooldowns.computeIfAbsent(entry.getSlot(), slot -> new ArrayList<>()).add(entry.getCooldown());
                }
            }
            ClientAbilityCooldowns.replaceAll(cooldowns);
        });
    }

    @Override
    public void syncCustomGooTile(BlockPos pos, NBTTagCompound tag, boolean remove) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return;
        }
        minecraft.addScheduledTask(() -> CustomGooRuntime.applyClientSync(minecraft.world, pos, tag, remove));
    }

    @Override
    public void spawnTimeCrystalChargeParticle(World world, double startX, double startY, double startZ, double targetX, double targetY, double targetZ, float red, float green, float blue) {
        Minecraft minecraft = Minecraft.getMinecraft();
        TimeCrystalParticles.spawnCharge(world == null && minecraft != null ? minecraft.world : world, startX, startY, startZ, targetX, targetY, targetZ, red, green, blue);
    }

    @Override
    public void spawnItemFlowParticle(World world, double startX, double startY, double startZ, double targetX, double targetY, double targetZ, ItemStack stack, int ticksPerBlock) {
        Minecraft minecraft = Minecraft.getMinecraft();
        TimeCrystalParticles.spawnItemFlow(world == null && minecraft != null ? minecraft.world : world, startX, startY, startZ, targetX, targetY, targetZ, stack, ticksPerBlock);
    }

    @Override
    public void openMachineSettingsCopierScreen(ItemStack stack) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null) {
            minecraft.displayGuiScreen(new MachineSettingsCopierScreen(stack));
        }
    }
}
