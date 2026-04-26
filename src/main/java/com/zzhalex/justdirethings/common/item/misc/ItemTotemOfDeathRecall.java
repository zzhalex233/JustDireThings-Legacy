package com.zzhalex.justdirethings.common.item.misc;

import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTotemOfDeathRecall extends Item {

    private static final String TAG_BOUND = "JDTBoundDeathLocation";
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_X = "X";
    private static final String TAG_Y = "Y";
    private static final String TAG_Z = "Z";
    private static final int REQUIRED_USE_TICKS = 20;

    public ItemTotemOfDeathRecall() {
        setMaxStackSize(1);
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!hasBoundLocation(stack)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft) {
        if (world.isRemote || !(entityLiving instanceof EntityPlayerMP)) {
            return;
        }
        int usedTicks = getMaxItemUseDuration(stack) - timeLeft;
        if (usedTicks < REQUIRED_USE_TICKS) {
            return;
        }

        BoundLocation boundLocation = getBoundLocation(stack);
        if (boundLocation == null) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) entityLiving;
        Entity teleported = player;
        if (player.dimension != boundLocation.dimension) {
            World targetWorld = DimensionManager.getWorld(boundLocation.dimension);
            if (targetWorld == null) {
                return;
            }
            teleported = player.changeDimension(boundLocation.dimension);
            if (!(teleported instanceof EntityPlayerMP)) {
                return;
            }
            player = (EntityPlayerMP) teleported;
        }

        player.connection.setPlayerLocation(boundLocation.position.x, boundLocation.position.y, boundLocation.position.z, player.rotationYaw, player.rotationPitch);
        teleported.fallDistance = 0.0F;
        stack.shrink(1);
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return hasBoundLocation(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        BoundLocation boundLocation = getBoundLocation(stack);
        if (boundLocation != null) {
            tooltip.add(TextFormatting.DARK_PURPLE + I18n.format("justdirethings.boundto", boundLocation.dimension, boundLocation.toShortString()));
        }
    }

    public static boolean hasBoundLocation(ItemStack stack) {
        return getBoundLocation(stack) != null;
    }

    @Nullable
    public static BoundLocation getBoundLocation(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(TAG_BOUND)) {
            return null;
        }
        NBTTagCompound bound = root.getCompoundTag(TAG_BOUND);
        return new BoundLocation(
                bound.getInteger(TAG_DIMENSION),
                bound.getDouble(TAG_X),
                bound.getDouble(TAG_Y),
                bound.getDouble(TAG_Z)
        );
    }

    public static void setBoundLocation(ItemStack stack, int dimension, double x, double y, double z) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }

        NBTTagCompound bound = new NBTTagCompound();
        bound.setInteger(TAG_DIMENSION, dimension);
        bound.setDouble(TAG_X, x);
        bound.setDouble(TAG_Y, y);
        bound.setDouble(TAG_Z, z);
        root.setTag(TAG_BOUND, bound);
    }

    public static final class BoundLocation {

        private final int dimension;
        private final Vec3d position;

        private BoundLocation(int dimension, double x, double y, double z) {
            this.dimension = dimension;
            this.position = new Vec3d(x, y, z);
        }

        public int getDimension() {
            return dimension;
        }

        public Vec3d getPosition() {
            return position;
        }

        private String toShortString() {
            return String.format("%.1f, %.1f, %.1f", position.x, position.y, position.z);
        }
    }
}
