package com.zzhalex.justdirethings;

import com.zzhalex.justdirethings.client.ClientRegistration;
import com.zzhalex.justdirethings.client.render.ThingFinder;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
}
