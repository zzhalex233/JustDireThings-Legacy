package com.zzhalex.justdirethings.common.item.equipment;

import com.zzhalex.justdirethings.common.block.machine.BlockMachineBase;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockSwapper;
import com.zzhalex.justdirethings.common.util.DimensionDisplayHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemJDTWrench extends Item {

    private static final String TAG_BOUND = "JDTWrenchBoundTo";
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_X = "X";
    private static final String TAG_Y = "Y";
    private static final String TAG_Z = "Z";

    private final JDTToolTier tier;

    public ItemJDTWrench(String id, JDTToolTier tier) {
        this.tier = tier;
        setMaxStackSize(1);
        setMaxDamage(Math.max(384, tier.asVanillaMaterial().getMaxUses()));
        EquipmentItemSupport.configure(this, id);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return EquipmentItemSupport.matchesRepairItem(repair, tier.getRepairStack()) || super.getIsRepairable(toRepair, repair);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote || !player.isSneaking()) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        BoundLocation boundLocation = getBoundTo(stack);
        if (boundLocation == null) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        removeBoundTo(stack);
        player.sendStatusMessage(new TextComponentTranslation("justdirethings.bindremoved"), true);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        IBlockState state = world.getBlockState(pos);
        if (!player.isSneaking() && specialBlockHandling(world, player, pos, stack)) {
            return EnumActionResult.SUCCESS;
        }
        IBlockState rotatedState = rotateState(world, pos, state);
        if (world.isRemote) {
            return shouldConsumeUse(state, rotatedState) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
        }
        if (rotatedState.equals(state)) {
            return shouldConsumeUse(state, rotatedState) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
        }
        world.setBlockState(pos, rotatedState, 3);
        TileMachineBase machine = getMachine(world, pos);
        if (machine != null && rotatedState.getPropertyKeys().contains(BlockMachineBase.FACING)) {
            machine.setDirection(rotatedState.getValue(BlockMachineBase.FACING).getIndex());
            machine.markDirtyClient();
        }
        world.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_ROTATE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return EnumActionResult.SUCCESS;
    }

    private boolean shouldConsumeUse(IBlockState state, IBlockState rotatedState) {
        return !rotatedState.equals(state) || state.getBlock() instanceof BlockMachineBase;
    }

    private IBlockState rotateState(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() instanceof BlockMachineBase) {
            return ((BlockMachineBase) state.getBlock()).direRotate(state, world, pos, Rotation.CLOCKWISE_90);
        }
        return state.withRotation(Rotation.CLOCKWISE_90);
    }

    private boolean specialBlockHandling(World world, EntityPlayer player, BlockPos blockPos, ItemStack itemStack) {
        TileBlockSwapper blockSwapper = getSwapper(world, blockPos);
        if (blockSwapper == null) {
            return false;
        }
        if (world.isRemote) {
            return false;
        }

        BoundLocation boundLocation = getBoundTo(itemStack);
        if (boundLocation == null) {
            BoundLocation newBinding = new BoundLocation(world.provider.getDimension(), blockPos);
            setBoundTo(itemStack, newBinding);
            sendBoundMessage(world, player, newBinding);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }

        boolean bound = blockSwapper.handleConnection(boundLocation.getDimension(), boundLocation.getPos());
        if (bound) {
            sendBoundMessage(world, player, boundLocation);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
        } else {
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.bindremoved"), true);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        removeBoundTo(itemStack);
        return true;
    }

    private void sendBoundMessage(World world, EntityPlayer player, BoundLocation boundLocation) {
        player.sendStatusMessage(new TextComponentTranslation(
                "justdirethings.boundto",
                boundLocation.getDimensionName(),
                "[" + boundLocation.toShortString() + "]"
        ), true);
    }

    @Nullable
    private static TileBlockSwapper getSwapper(World world, BlockPos blockPos) {
        return world.getTileEntity(blockPos) instanceof TileBlockSwapper ? (TileBlockSwapper) world.getTileEntity(blockPos) : null;
    }

    @Nullable
    private static TileMachineBase getMachine(World world, BlockPos blockPos) {
        return world.getTileEntity(blockPos) instanceof TileMachineBase ? (TileMachineBase) world.getTileEntity(blockPos) : null;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return getBoundTo(stack) != null || super.hasEffect(stack);
    }

    public static BoundLocation getBoundTo(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(TAG_BOUND)) {
            return null;
        }
        NBTTagCompound bound = root.getCompoundTag(TAG_BOUND);
        return new BoundLocation(
                bound.getInteger(TAG_DIMENSION),
                new BlockPos(bound.getInteger(TAG_X), bound.getInteger(TAG_Y), bound.getInteger(TAG_Z))
        );
    }

    public static void setBoundTo(ItemStack stack, BoundLocation boundLocation) {
        if (stack == null || stack.isEmpty() || boundLocation == null) {
            return;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }
        NBTTagCompound bound = new NBTTagCompound();
        bound.setInteger(TAG_DIMENSION, boundLocation.getDimension());
        bound.setInteger(TAG_X, boundLocation.getPos().getX());
        bound.setInteger(TAG_Y, boundLocation.getPos().getY());
        bound.setInteger(TAG_Z, boundLocation.getPos().getZ());
        root.setTag(TAG_BOUND, bound);
    }

    public static void removeBoundTo(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root != null) {
            root.removeTag(TAG_BOUND);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        BoundLocation boundLocation = getBoundTo(stack);
        if (boundLocation != null) {
            tooltip.add(TextFormatting.DARK_PURPLE + I18n.format(
                    "justdirethings.boundto",
                    boundLocation.getDimensionName(),
                    boundLocation.toShortString()
            ));
        }
    }

    public static final class BoundLocation {
        private final int dimension;
        private final BlockPos pos;

        public BoundLocation(int dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos == null ? BlockPos.ORIGIN : pos;
        }

        public int getDimension() {
            return dimension;
        }

        public String getDimensionName() {
            return DimensionDisplayHelper.getDimensionName(dimension);
        }

        public BlockPos getPos() {
            return pos;
        }

        public String toShortString() {
            return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        }
    }
}
