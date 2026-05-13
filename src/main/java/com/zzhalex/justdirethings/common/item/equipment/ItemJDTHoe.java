package com.zzhalex.justdirethings.common.item.equipment;

import com.google.common.collect.Multimap;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.BoundInventoryHelper;
import com.zzhalex.justdirethings.common.item.base.LeftClickableTool;
import com.zzhalex.justdirethings.common.item.base.AbilityExecutionHelper;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import com.zzhalex.justdirethings.common.tile.goo.TileGooSoil;
import com.zzhalex.justdirethings.common.util.MiningCollect;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemJDTHoe extends ItemHoe implements ToggleableTool, LeftClickableTool {

    private final JDTToolTier tier;

    public ItemJDTHoe(String id, JDTToolTier tier) {
        super(tier.asVanillaMaterial());
        this.tier = tier;
        EquipmentItemSupport.configure(this, id);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return EquipmentItemSupport.matchesRepairItem(repair, tier.getRepairStack()) || super.getIsRepairable(toRepair, repair);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ActionResult<ItemStack> opened = EquipmentItemSupport.openSettingsIfSneaking(this, worldIn, playerIn, handIn);
        if (opened != null) {
            return opened;
        }
        ActionResult<ItemStack> abilityResult = AbilityExecutionHelper.tryExecuteRightClickAbility(worldIn, playerIn, handIn);
        return abilityResult != null ? abilityResult : super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (bindDrops(player, worldIn, pos, hand, facing)) {
            return EnumActionResult.SUCCESS;
        }
        EnumActionResult abilityResult = AbilityExecutionHelper.tryExecuteUseOnAbility(worldIn, player, hand, pos, facing);
        if (abilityResult == EnumActionResult.SUCCESS) {
            return abilityResult;
        }

        if (tryHammerTill(player, worldIn, pos, hand, facing, hitX, hitY, hitZ)) {
            return EnumActionResult.SUCCESS;
        }

        EnumActionResult result = super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        return bindSoil(player, worldIn, pos, hand, facing) ? EnumActionResult.SUCCESS : result;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (EquipmentItemSupport.isCreativePlayer(attacker)) {
            return true;
        }
        if (EquipmentItemSupport.isPowered(stack) && !EquipmentItemSupport.hasPoweredDurability(stack, 1)) {
            return true;
        }
        if (EquipmentItemSupport.consumePoweredDurability(stack, 1)) {
            return true;
        }
        return super.hitEntity(stack, target, attacker);
    }

    @Override
    protected void setBlock(ItemStack stack, EntityPlayer player, World worldIn, BlockPos pos, IBlockState state) {
        worldIn.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, state, 11);
            if (player.capabilities.isCreativeMode) {
                return;
            }
            if (!EquipmentItemSupport.consumePoweredDurability(stack, 1)) {
                stack.damageItem(1, player);
            }
        }
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        return EquipmentItemSupport.getPoweredAttributeModifiers(slot, stack, super.getAttributeModifiers(slot, stack));
    }

    private boolean tryHammerTill(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!AbilityMethods.canUseAbilityAndDurability(stack, Ability.HAMMER)) {
            return false;
        }

        Set<BlockPos> affectedBlocks = MiningCollect.collect(player, pos, world, MiningCollect.getHammerRange(stack), stack, this::canTillWithHammer);
        if (affectedBlocks.isEmpty()) {
            return false;
        }

        boolean changed = false;
        for (BlockPos blockPos : affectedBlocks) {
            IBlockState oldState = world.getBlockState(blockPos);
            EnumActionResult result = super.onItemUse(player, world, blockPos, hand, facing, hitX, hitY, hitZ);
            if (result == EnumActionResult.SUCCESS) {
                bindSoil(player, world, blockPos, hand, facing);
                changed = true;
                if (!world.isRemote) {
                    world.notifyBlockUpdate(blockPos, oldState, world.getBlockState(blockPos), 3);
                }
            }
        }
        if (changed && !player.capabilities.isCreativeMode) {
            AbilityMethods.damageTool(stack, player, Ability.HAMMER);
        }
        return changed;
    }

    private boolean canTillWithHammer(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side) {
        if (world.getBlockState(pos.up()).getMaterial().isSolid()) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == Blocks.GRASS
                || state.getBlock() == Blocks.GRASS_PATH
                || (state.getBlock() == Blocks.DIRT && state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT);
    }

    private boolean bindDrops(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing) {
        if (player == null || !player.isSneaking()) {
            return false;
        }
        ItemStack stack = player.getHeldItem(hand);
        if (!AbilityMethods.canUseAbility(stack, Ability.DROPTELEPORT)) {
            return false;
        }
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }

        BoundInventoryHelper.BoundLocation newBinding = new BoundInventoryHelper.BoundLocation(world.provider.getDimension(), pos, facing);
        BoundInventoryHelper.BoundLocation existing = BoundInventoryHelper.getBoundTo(stack);
        if (newBinding.equals(existing)) {
            BoundInventoryHelper.removeBoundTo(stack);
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.bindremoved"), true);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        } else {
            BoundInventoryHelper.setBoundTo(stack, newBinding);
            player.sendStatusMessage(new TextComponentTranslation(
                    "justdirethings.boundto",
                    newBinding.getDimensionName(),
                    "[" + newBinding.toShortString() + "]"
            ), true);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }

    private boolean bindSoil(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing) {
        if (player == null || world.isRemote) {
            return false;
        }
        if (!(world.getTileEntity(pos) instanceof TileGooSoil)) {
            return false;
        }
        ItemStack stack = player.getHeldItem(hand);
        if (!AbilityMethods.canUseAbility(stack, Ability.DROPTELEPORT)) {
            return false;
        }
        if (AbilityMethods.testUseTool(stack, Ability.DROPTELEPORT) < 0) {
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.bindfailed"), true);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ANVIL_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return false;
        }
        BoundInventoryHelper.BoundLocation boundLocation = BoundInventoryHelper.getBoundTo(stack);
        if (boundLocation == null) {
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.bindfailed"), true);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ANVIL_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return false;
        }

        TileGooSoil soil = (TileGooSoil) world.getTileEntity(pos);
        soil.bindInventory(boundLocation.getPos(), boundLocation.getSide(), boundLocation.getDimension());
        player.sendStatusMessage(new TextComponentTranslation(
                "justdirethings.boundto",
                boundLocation.getDimensionName(),
                "[" + boundLocation.toShortString() + "]"
        ), true);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        AbilityMethods.damageTool(stack, player, Ability.DROPTELEPORT);
        return true;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return EquipmentItemSupport.initEnergyCapabilities(this, stack);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EquipmentItemSupport.showEnergyBar(this, stack) || super.showDurabilityBar(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return EquipmentItemSupport.showEnergyBar(this, stack)
                ? EquipmentItemSupport.getEnergyDurabilityForDisplay(this, stack)
                : super.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return EquipmentItemSupport.showEnergyBar(this, stack)
                ? EquipmentItemSupport.getEnergyBarColor(this, stack)
                : super.getRGBDurabilityForDisplay(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        EquipmentItemSupport.appendEquipmentTooltip(this, stack, tooltip);
    }

    @Override
    public Set<Ability> getSupportedAbilities() {
        return EquipmentItemSupport.getAbilities(this);
    }

    @Override
    public Map<Ability, AbilityParams> getAbilityParamsMap() {
        return EquipmentItemSupport.getAbilityParams(this);
    }
}
