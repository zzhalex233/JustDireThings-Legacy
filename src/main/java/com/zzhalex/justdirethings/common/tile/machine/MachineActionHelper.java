package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.InternalItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.common.util.UsefulFakePlayer;
import com.zzhalex.justdirethings.common.util.WorldInteractionRules;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.items.ItemHandlerHelper;

public final class MachineActionHelper {
    private MachineActionHelper() {
    }

    public static EnumFacing getFacing(TileMachineBase machine) {
        EnumFacing facing = EnumFacing.byIndex(machine.getDirection());
        return facing == null ? EnumFacing.NORTH : facing;
    }

    public static BlockPos targetPos(TileMachineBase machine) {
        return FakePlayerMath.targetPos(machine.getPos(), getFacing(machine));
    }

    public static FakePlayer createFakePlayer(WorldServer world, TileMachineBase machine) {
        FakePlayer fakePlayer = machine.getUsefulFakePlayer(world);
        alignFakePlayer(fakePlayer, targetPos(machine), getFacing(machine));
        return fakePlayer;
    }

    public static FakePlayer createFakePlayer(WorldServer world, BlockPos targetPos, EnumFacing facing) {
        FakePlayer fakePlayer = UsefulFakePlayer.createPlayer(world, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("127c8dd2-b17e-4a95-82af-7dcbcafc3987"), "[JDTMachine]"));
        alignFakePlayer(fakePlayer, targetPos, facing);
        return fakePlayer;
    }

    public static void alignFakePlayer(FakePlayer fakePlayer, BlockPos targetPos, EnumFacing facing) {
        fakePlayer.setPosition(
                targetPos.getX() + 0.5D - facing.getXOffset() * 0.6D,
                targetPos.getY() + 0.5D - facing.getYOffset() * 0.6D,
                targetPos.getZ() + 0.5D - facing.getZOffset() * 0.6D
        );
        fakePlayer.rotationYaw = FakePlayerMath.yawForFacing(facing);
        fakePlayer.rotationPitch = FakePlayerMath.pitchForFacing(facing);
    }

    public static void alignFakePlayerForUse(FakePlayer fakePlayer, BlockPos targetPos, EnumFacing facing) {
        fakePlayer.setPosition(
                targetPos.getX() + useSideOffset(facing, EnumFacing.Axis.X),
                targetPos.getY() + useSideOffset(facing, EnumFacing.Axis.Y) - fakePlayer.getEyeHeight(),
                targetPos.getZ() + useSideOffset(facing, EnumFacing.Axis.Z)
        );
        fakePlayer.rotationYaw = FakePlayerMath.yawForFacing(facing);
        fakePlayer.rotationYawHead = fakePlayer.rotationYaw;
        fakePlayer.rotationPitch = FakePlayerMath.pitchForFacing(facing);
    }

    private static double useSideOffset(EnumFacing facing, EnumFacing.Axis axis) {
        if (facing.getAxis() != axis) {
            return 0.5D;
        }
        return facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 0.95D : 0.05D;
    }

    public static boolean canReplace(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return world.isAirBlock(pos) || state.getBlock().isReplaceable(world, pos);
    }

    public static boolean canAttemptPlacement(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof ItemBlock
                || stack.getItem() instanceof ItemBlockSpecial
                || stack.getItem() instanceof ItemRedstone
                || stack.getItem() instanceof IPlantable;
    }

    public static int findFirstNonEmptySlot(InternalItemHandler itemHandler) {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            if (!itemHandler.getStackInSlot(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    public static int findFirstPlaceableSlot(InternalItemHandler itemHandler) {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.getItem() instanceof ItemBlock
                    || stack.getItem() instanceof ItemRedstone
                    || stack.getItem() instanceof IPlantable) {
                return slot;
            }
        }
        return -1;
    }

    public static void spawnStack(World world, BlockPos pos, EnumFacing facing, ItemStack stack) {
        spawnStack(world, pos, facing, stack, 0);
    }

    public static void spawnStack(World world, BlockPos pos, EnumFacing facing, ItemStack stack, int pickupDelay) {
        if (stack.isEmpty()) {
            return;
        }
        EntityItem entityItem = new EntityItem(
                world,
                pos.getX() + 0.5D + facing.getXOffset() * 0.4D,
                pos.getY() + 0.5D + facing.getYOffset() * 0.4D,
                pos.getZ() + 0.5D + facing.getZOffset() * 0.4D,
                stack.copy()
        );
        entityItem.motionX = facing.getXOffset() * 0.15D;
        entityItem.motionY = 0.15D + facing.getYOffset() * 0.15D;
        entityItem.motionZ = facing.getZOffset() * 0.15D;
        if (pickupDelay > 0) {
            entityItem.setPickupDelay(pickupDelay);
        }
        world.spawnEntity(entityItem);
    }

    public static void insertOrDrop(InternalItemHandler itemHandler, World world, BlockPos pos, EnumFacing facing, ItemStack stack) {
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(itemHandler, stack, false);
        if (!remainder.isEmpty()) {
            spawnStack(world, pos, facing, remainder);
        }
    }

    public static boolean placeFirstBlock(InternalItemHandler itemHandler, World world, BlockPos targetPos, EnumFacing facing) {
        if (!canReplace(world, targetPos)) {
            return false;
        }
        int slot = findFirstPlaceableSlot(itemHandler);
        if (slot < 0) {
            return false;
        }

        ItemStack stack = itemHandler.getStackInSlot(slot);
        ItemStack singleItem = stack.copy();
        singleItem.setCount(1);
        IBlockState state = resolvePlacementState(world, targetPos, facing, singleItem);
        if (state == null || !state.getBlock().canPlaceBlockAt(world, targetPos)) {
            return false;
        }
        if (!world.setBlockState(targetPos, state, 3)) {
            return false;
        }

        finalizePlacedBlock(world, targetPos, state, singleItem, facing);
        stack.shrink(1);
        itemHandler.setStackInSlot(slot, stack);
        return true;
    }

    public static boolean breakBlockIntoInventory(InternalItemHandler itemHandler, World world, BlockPos originPos, BlockPos targetPos, EnumFacing facing) {
        IBlockState state = world.getBlockState(targetPos);
        if (state.getBlock() == Blocks.AIR || state.getBlockHardness(world, targetPos) < 0.0F) {
            return false;
        }

        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, world, targetPos, state, 0);
        world.playEvent(2001, targetPos, Block.getStateId(state));
        world.setBlockToAir(targetPos);
        for (ItemStack drop : drops) {
            insertOrDrop(itemHandler, world, originPos, facing, drop);
        }
        return true;
    }

    public static boolean canBreakAndPlaceAt(World targetWorld, BlockPos blockPos, FakePlayer fakePlayer) {
        return canBreakAt(targetWorld, blockPos, fakePlayer) && canPlaceAt(targetWorld, blockPos, fakePlayer);
    }

    public static boolean canBreakAt(World targetWorld, BlockPos blockPos, FakePlayer fakePlayer) {
        return targetWorld != null && blockPos != null && fakePlayer != null;
    }

    public static boolean canPlaceAt(World targetWorld, BlockPos blockPos, FakePlayer fakePlayer) {
        if (targetWorld == null || blockPos == null || fakePlayer == null) {
            return false;
        }
        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(targetWorld, blockPos);
        BlockEvent.PlaceEvent event = ForgeEventFactory.onPlayerBlockPlace(fakePlayer, snapshot, EnumFacing.UP, EnumHand.MAIN_HAND);
        return !event.isCanceled();
    }

    public static boolean swapFirstBlock(InternalItemHandler itemHandler, World world, BlockPos originPos, BlockPos targetPos, EnumFacing facing) {
        IBlockState currentState = world.getBlockState(targetPos);
        if (currentState.getBlock() == Blocks.AIR) {
            return false;
        }

        int slot = findFirstPlaceableSlot(itemHandler);
        if (slot < 0) {
            return false;
        }

        ItemStack replacement = itemHandler.getStackInSlot(slot);
        ItemStack singleItem = replacement.copy();
        singleItem.setCount(1);
        IBlockState replacementState = resolvePlacementState(world, targetPos, facing, singleItem);
        if (replacementState == null || !replacementState.getBlock().canPlaceBlockAt(world, targetPos)) {
            return false;
        }
        NonNullList<ItemStack> drops = NonNullList.create();
        currentState.getBlock().getDrops(drops, world, targetPos, currentState, 0);
        if (!world.setBlockState(targetPos, replacementState, 3)) {
            return false;
        }

        finalizePlacedBlock(world, targetPos, replacementState, singleItem, facing);
        replacement.shrink(1);
        itemHandler.setStackInSlot(slot, replacement);
        for (ItemStack drop : drops) {
            insertOrDrop(itemHandler, world, originPos, facing, drop);
        }
        return true;
    }

    public static boolean useHeldItemOnTarget(WorldServer world, TileMachineBase machine, InternalItemHandler itemHandler, int slot, BlockPos targetPos, EnumFacing facing, boolean targetIsReplaceable) {
        FakePlayer fakePlayer = machine.getUsefulFakePlayer(world);
        alignFakePlayer(fakePlayer, targetPos(machine), getFacing(machine));
        return useHeldItemOnTarget(world, fakePlayer, itemHandler, slot, targetPos, facing, targetIsReplaceable);
    }

    public static boolean useHeldItemOnTarget(WorldServer world, FakePlayer fakePlayer, InternalItemHandler itemHandler, int slot, BlockPos targetPos, EnumFacing facing, boolean targetIsReplaceable) {
        alignFakePlayerForUse(fakePlayer, targetPos, facing);
        EnumFacing hitFace = targetIsReplaceable ? facing : facing.getOpposite();
        return useHeldItemOnBlock(world, fakePlayer, itemHandler, slot, targetPos, hitFace);
    }

    public static boolean useHeldItemOnBlock(WorldServer world, FakePlayer fakePlayer, InternalItemHandler itemHandler, int slot, BlockPos clickPos, EnumFacing hitFace, BlockPos playerTargetPos, EnumFacing playerFacing) {
        alignFakePlayerForUse(fakePlayer, playerTargetPos, playerFacing);
        return useHeldItemOnBlock(world, fakePlayer, itemHandler, slot, clickPos, hitFace);
    }

    public static boolean useHeldItemOnBlock(WorldServer world, FakePlayer fakePlayer, InternalItemHandler itemHandler, int slot, BlockPos clickPos, EnumFacing hitFace) {
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, itemHandler.getStackInSlot(slot).copy());
        if (fakePlayer.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
            return useEmptyHandOnBlock(world, fakePlayer, clickPos, hitFace);
        }

        EnumActionResult blockResult = fakePlayer.interactionManager.processRightClickBlock(
                fakePlayer,
                world,
                fakePlayer.getHeldItem(EnumHand.MAIN_HAND),
                EnumHand.MAIN_HAND,
                clickPos,
                hitFace,
                0.5F,
                0.5F,
                0.5F
        );
        syncFakePlayerHeldItem(itemHandler, slot, fakePlayer.getHeldItem(EnumHand.MAIN_HAND));
        if (blockResult == EnumActionResult.SUCCESS) {
            return true;
        }
        if (blockResult == EnumActionResult.FAIL || fakePlayer.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
            return false;
        }

        EnumActionResult itemResult = fakePlayer.interactionManager.processRightClick(
                fakePlayer,
                world,
                fakePlayer.getHeldItem(EnumHand.MAIN_HAND),
                EnumHand.MAIN_HAND
        );
        syncFakePlayerHeldItem(itemHandler, slot, fakePlayer.getHeldItem(EnumHand.MAIN_HAND));
        return itemResult == EnumActionResult.SUCCESS;
    }

    private static boolean useEmptyHandOnBlock(WorldServer world, FakePlayer fakePlayer, BlockPos clickPos, EnumFacing hitFace) {
        PlayerInteractEvent.RightClickBlock event = net.minecraftforge.common.ForgeHooks.onRightClickBlock(
                fakePlayer,
                EnumHand.MAIN_HAND,
                clickPos,
                hitFace,
                net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(fakePlayer, fakePlayer.interactionManager.getBlockReachDistance() + 1)
        );
        if (event.isCanceled() || event.getUseBlock() == Event.Result.DENY) {
            return false;
        }
        if (fakePlayer.isSneaking() && event.getUseBlock() != Event.Result.ALLOW) {
            return false;
        }
        IBlockState state = world.getBlockState(clickPos);
        return state.getBlock().onBlockActivated(world, clickPos, state, fakePlayer, EnumHand.MAIN_HAND, hitFace, 0.5F, 0.5F, 0.5F);
    }

    private static void syncFakePlayerHeldItem(InternalItemHandler itemHandler, int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack.copy());
    }

    private static IBlockState resolvePlacementState(World world, BlockPos targetPos, EnumFacing facing, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        if (stack.getItem() instanceof ItemBlock) {
            ItemBlock itemBlock = (ItemBlock) stack.getItem();
            FakePlayer placer = world instanceof WorldServer ? createFakePlayer((WorldServer) world, targetPos, facing) : null;
            IBlockState state = itemBlock.getBlock().getStateForPlacement(
                    world,
                    targetPos,
                    facing,
                    0.5F,
                    0.5F,
                    0.5F,
                    stack.getMetadata(),
                    placer
            );
            return WorldInteractionRules.orientPlacementState(state, facing, WorldInteractionRules.horizontalFacing(facing, EnumFacing.NORTH));
        }

        if (stack.getItem() instanceof ItemRedstone) {
            return Blocks.REDSTONE_WIRE.getDefaultState();
        }

        if (stack.getItem() instanceof IPlantable) {
            IBlockState state = ((IPlantable) stack.getItem()).getPlant(world, targetPos);
            return state.getBlock().canPlaceBlockAt(world, targetPos) ? state : null;
        }

        return null;
    }

    private static void finalizePlacedBlock(World world, BlockPos targetPos, IBlockState state, ItemStack placedStack, EnumFacing facing) {
        if (!(world instanceof WorldServer)) {
            return;
        }
        FakePlayer fakePlayer = createFakePlayer((WorldServer) world, targetPos, facing);
        ItemBlock.setTileEntityNBT(world, fakePlayer, targetPos, placedStack);
        state.getBlock().onBlockPlacedBy(world, targetPos, state, fakePlayer, placedStack);
    }
}
