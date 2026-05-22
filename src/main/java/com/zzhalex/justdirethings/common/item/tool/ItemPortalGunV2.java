package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.entity.EntityPortalProjectile;
import com.zzhalex.justdirethings.common.item.base.FluidPickupHelper;
import com.zzhalex.justdirethings.common.item.base.ItemFluidPoweredTool;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import com.zzhalex.justdirethings.common.portal.PortalLinkData;
import com.zzhalex.justdirethings.config.JDTConfig;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.registry.ModFluids;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

public class ItemPortalGunV2 extends ItemFluidPoweredTool {

    public static final int FLUID_CAPACITY = 8_000;
    public static final String FAVORITE_REMOVE = "Remove";
    public static final String FAVORITE_STAY_OPEN = "StayOpen";
    public static final String FAVORITE_EDITING = "Editing";

    public ItemPortalGunV2() {
        super(1_000_000, 1_000_000, 1_000_000, 0, FLUID_CAPACITY);
        setTranslationKey(Reference.MOD_ID + ".portal_gun_v2");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (FluidPickupHelper.pickupSourceFluid(world, player, stack, rayTrace(world, player, true), getContainedFluid(stack))) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        if (!world.isRemote) {
            spawnProjectile(world, player, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TextFormatting.DARK_AQUA + I18n.format(
                "justdirethings.portalfluidamt",
                TooltipHelper.formatNumber(getStoredFluid(stack)),
                TooltipHelper.formatNumber(getFluidCapacity())
        ));
    }

    public boolean spawnProjectile(World world, EntityPlayer player, ItemStack stack) {
        PortalLinkData linkData = getLinkData(stack);
        UUID portalGunId = getOrCreatePortalGunId(stack);
        linkData.setPortalGunUuid(portalGunId);
        PortalLinkData.PortalDestination destination = player.isSneaking()
                ? linkData.getPrevious()
                : linkData.getSelectedFavorite();

        if (destination == null || destination.isEmpty()) {
            return false;
        }

        int fluidCost = calculateFluidCost(player, destination);
        if (!player.capabilities.isCreativeMode && getStoredFluid(stack) < fluidCost) {
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.lowportalfluid"), true);
            playFailureSound(player);
            return false;
        }
        if (!player.capabilities.isCreativeMode && getStoredEnergy(stack) < JDTConfig.portalGunV2RfCost) {
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.lowenergy"), true);
            playFailureSound(player);
            return false;
        }

        if (!player.capabilities.isCreativeMode) {
            setStoredEnergy(stack, getStoredEnergy(stack) - JDTConfig.portalGunV2RfCost);
            setStoredFluid(stack, getStoredFluid(stack) - fluidCost);
        }

        EntityPortalProjectile projectile = new EntityPortalProjectile(world, player, portalGunId, destination);
        projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.0F, 1.0F);
        world.spawnEntity(projectile);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERPEARL_THROW, SoundCategory.PLAYERS, 0.5F, 1.0F);

        linkData.setPrevious(previousDestination(player));
        setLinkData(stack, linkData);
        return true;
    }

    private PortalLinkData.PortalDestination previousDestination(EntityPlayer player) {
        EnumFacing facing = PortalLinkData.PortalDestination.facingFromPlayer(player);
        if (facing == EnumFacing.DOWN) {
            facing = EnumFacing.NORTH;
        }
        return new PortalLinkData.PortalDestination("previous", player.world.provider.getDimension(), player.posX, player.posY, player.posZ, facing);
    }

    public int calculateFluidCost(EntityPlayer player, PortalLinkData.PortalDestination destination) {
        if (player.capabilities.isCreativeMode) {
            return 0;
        }
        if (destination.getDimension() != player.world.provider.getDimension()) {
            return 100;
        }

        Vec3d projectedHit = projectedPortalHit(player);
        Vec3d destinationPosition = new Vec3d(destination.getX(), destination.getY(), destination.getZ());
        double distance = destinationPosition.distanceTo(projectedHit);
        return Math.min(100, (int) Math.ceil(distance * 0.25D));
    }

    private Vec3d projectedPortalHit(EntityPlayer player) {
        Vec3d eyes = player.getPositionEyes(0.0F);
        Vec3d look = player.getLook(0.0F);
        Vec3d reach = eyes.add(look.x * 5.0D, look.y * 5.0D, look.z * 5.0D);
        RayTraceResult result = player.world.rayTraceBlocks(eyes, reach, false, false, false);
        return result == null || result.hitVec == null ? reach : result.hitVec;
    }

    private void playFailureSound(EntityPlayer player) {
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    public PortalLinkData getLinkData(ItemStack stack) {
        NBTTagCompound rootTag = getOrCreateTag(stack);
        if (!rootTag.hasKey(JDTDataKeys.PORTAL_LINK_DATA)) {
            return new PortalLinkData();
        }
        return PortalLinkData.read(rootTag.getCompoundTag(JDTDataKeys.PORTAL_LINK_DATA));
    }

    public void setLinkData(ItemStack stack, PortalLinkData linkData) {
        getOrCreateTag(stack).setTag(JDTDataKeys.PORTAL_LINK_DATA, PortalLinkData.write(linkData));
    }

    public PortalLinkData.PortalDestination getFavorite(ItemStack stack, int favoriteIndex) {
        return getLinkData(stack).getFavorite(favoriteIndex);
    }

    public void setFavoritePosition(ItemStack stack, int favoriteIndex) {
        PortalLinkData linkData = getLinkData(stack);
        linkData.setFavoriteIndex(favoriteIndex);
        setLinkData(stack, linkData);
    }

    public void setStayOpen(ItemStack stack, boolean stayOpen) {
        PortalLinkData linkData = getLinkData(stack);
        linkData.setStayOpen(stayOpen);
        setLinkData(stack, linkData);
    }

    public void addFavorite(ItemStack stack, int favoriteIndex, PortalLinkData.PortalDestination destination) {
        PortalLinkData linkData = getLinkData(stack);
        linkData.setFavorite(favoriteIndex, destination);
        setLinkData(stack, linkData);
    }

    public void removeFavorite(ItemStack stack, int favoriteIndex) {
        PortalLinkData linkData = getLinkData(stack);
        linkData.clearFavorite(favoriteIndex);
        setLinkData(stack, linkData);
    }

    public void applyFavoriteUpdate(ItemStack stack, int favoriteIndex, NBTTagCompound favoriteTag) {
        PortalLinkData linkData = getLinkData(stack);
        linkData.setFavoriteIndex(favoriteIndex);
        if (favoriteTag != null && !favoriteTag.isEmpty()) {
            if (favoriteTag.hasKey(FAVORITE_STAY_OPEN)) {
                linkData.setStayOpen(favoriteTag.getBoolean(FAVORITE_STAY_OPEN));
            }
            boolean hasDestinationUpdate = favoriteTag.hasKey("Name", Constants.NBT.TAG_STRING)
                    || favoriteTag.hasKey("Dimension", Constants.NBT.TAG_INT)
                    || favoriteTag.hasKey(FAVORITE_REMOVE)
                    || favoriteTag.getBoolean(FAVORITE_EDITING);
            if (favoriteTag.getBoolean(FAVORITE_REMOVE)) {
                linkData.clearFavorite(favoriteIndex);
            } else if (hasDestinationUpdate) {
                PortalLinkData.PortalDestination destination = favoriteTag.getBoolean(FAVORITE_EDITING)
                        ? editFavorite(linkData.getFavorite(favoriteIndex), favoriteTag)
                        : PortalLinkData.PortalDestination.read(favoriteTag);
                linkData.setFavorite(favoriteIndex, destination);
            }
        }
        setLinkData(stack, linkData);
    }

    private PortalLinkData.PortalDestination editFavorite(PortalLinkData.PortalDestination existing, NBTTagCompound tag) {
        PortalLinkData.PortalDestination base = existing == null || existing.isEmpty()
                ? PortalLinkData.PortalDestination.EMPTY
                : existing;
        if (base.isEmpty()) {
            PortalLinkData.PortalDestination parsed = PortalLinkData.PortalDestination.read(tag);
            return parsed.isEmpty() ? base : parsed;
        }
        if (!tag.hasKey("Name") && !tag.hasKey("X") && !tag.hasKey("Y") && !tag.hasKey("Z")) {
            return base;
        }
        EnumFacing facing = EnumFacing.byName(tag.getString("Facing"));
        return new PortalLinkData.PortalDestination(
                tag.hasKey("Name", Constants.NBT.TAG_STRING) ? tag.getString("Name") : base.getName(),
                tag.hasKey("Dimension", Constants.NBT.TAG_INT) ? tag.getInteger("Dimension") : base.getDimension(),
                tag.hasKey("X", Constants.NBT.TAG_DOUBLE) ? tag.getDouble("X") : base.getX(),
                tag.hasKey("Y", Constants.NBT.TAG_DOUBLE) ? tag.getDouble("Y") : base.getY(),
                tag.hasKey("Z", Constants.NBT.TAG_DOUBLE) ? tag.getDouble("Z") : base.getZ(),
                facing == null ? base.getFacing() : facing
        );
    }

    public UUID getOrCreatePortalGunId(ItemStack stack) {
        PortalLinkData linkData = getLinkData(stack);
        if (linkData.getPortalGunUuid() == null) {
            linkData.setPortalGunUuid(UUID.randomUUID());
            setLinkData(stack, linkData);
        }
        return linkData.getPortalGunUuid();
    }

    public int getStoredEnergy(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.PORTAL_GUN_ENERGY);
    }

    public void setStoredEnergy(ItemStack stack, int storedEnergy) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.PORTAL_GUN_ENERGY, Math.max(0, Math.min(JDTConfig.portalGunV2RfCapacity, storedEnergy)));
    }

    @Override
    public int getEnergyCapacity(ItemStack stack) {
        return JDTConfig.portalGunV2RfCapacity;
    }

    @Override
    public int getMaxReceive(ItemStack stack) {
        return getEnergyCapacity(stack);
    }

    @Override
    public int getMaxExtract(ItemStack stack) {
        return getEnergyCapacity(stack);
    }

    public int getStoredFluid(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.PORTAL_GUN_FLUID);
    }

    public void setStoredFluid(ItemStack stack, int storedFluid) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.PORTAL_GUN_FLUID, Math.max(0, Math.min(getFluidCapacity(), storedFluid)));
    }

    @Override
    public Fluid getContainedFluid(ItemStack stack) {
        ModFluids.bootstrap();
        return ModFluids.getFluid("portal_fluid");
    }

    @Override
    public boolean canFillFluid(ItemStack stack, FluidStack resource) {
        return resource != null && resource.amount > 0 && resource.getFluid() == getContainedFluid(stack);
    }

    public static int getFullness(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemPortalGunV2)) {
            return 0;
        }

        ItemPortalGunV2 item = (ItemPortalGunV2) stack.getItem();
        int storedFluid = item.getStoredFluid(stack);
        if (storedFluid <= 0) {
            return 0;
        }

        float percentFull = (storedFluid / (float) item.getFluidCapacity()) * 100.0F;
        if (percentFull <= 33.0F) {
            return 1;
        }
        if (percentFull <= 66.0F) {
            return 2;
        }
        return 3;
    }

    public static ItemStack findHeldPortalGun(EntityPlayer player) {
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.getItem() instanceof ItemPortalGunV2) {
            return mainHand;
        }
        ItemStack offHand = player.getHeldItemOffhand();
        if (offHand.getItem() instanceof ItemPortalGunV2) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }
}
