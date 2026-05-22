package com.zzhalex.justdirethings.common.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityFireResistantItem extends EntityItem {

    public EntityFireResistantItem(World world) {
        super(world);
        isImmuneToFire = true;
    }

    public EntityFireResistantItem(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
        isImmuneToFire = true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE || source == DamageSource.LAVA) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }
}
