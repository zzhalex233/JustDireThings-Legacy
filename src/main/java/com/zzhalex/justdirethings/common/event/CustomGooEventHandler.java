package com.zzhalex.justdirethings.common.event;

import com.zzhalex.justdirethings.common.goo.CustomGooRuntime;
import com.zzhalex.justdirethings.common.recipe.custom.GooCatalystRegistry;
import com.zzhalex.justdirethings.common.recipe.custom.JDTBlockStateSpec;
import com.zzhalex.justdirethings.common.tile.goo.TileGooBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class CustomGooEventHandler {

    public static final CustomGooEventHandler INSTANCE = new CustomGooEventHandler();

    private CustomGooEventHandler() {
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        JDTBlockStateSpec catalyst = JDTBlockStateSpec.fromState(state);
        if (!GooCatalystRegistry.isCustomGoo(catalyst)) {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        EnumHand hand = event.getHand();
        ItemStack held = player.getHeldItem(hand);
        if (!GooCatalystRegistry.validRevivalItem(catalyst, held)) {
            return;
        }
        if (!canRevive(world, pos)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (world.isRemote) {
            com.zzhalex.justdirethings.network.JDTNetwork.getChannel().sendToServer(
                    new com.zzhalex.justdirethings.network.message.MessageReviveCustomGoo(pos, hand));
            return;
        }
        revive((WorldServer) world, pos, player, hand);
    }

    public static boolean revive(WorldServer world, BlockPos pos, EntityPlayer player, EnumHand hand) {
        if (world == null || pos == null || player == null || hand == null || !canRevive(world, pos)) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        JDTBlockStateSpec catalyst = JDTBlockStateSpec.fromState(state);
        ItemStack held = player.getHeldItem(hand);
        if (!GooCatalystRegistry.validRevivalItem(catalyst, held)) {
            return false;
        }

        TileGooBlock gooTile = CustomGooRuntime.getOrCreate(world, pos);
        if (gooTile == null || gooTile.isGooAlive()) {
            return false;
        }

        gooTile.reviveCustomGoo();
        CustomGooRuntime.syncTile(world, pos, gooTile);
        world.playSound(null, pos, SoundType.SLIME.getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 0.5F);
        if (!player.capabilities.isCreativeMode) {
            held.shrink(1);
        }
        if (player instanceof EntityPlayerMP && !(player instanceof FakePlayer)) {
            ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
        }
        return true;
    }

    private static boolean canRevive(World world, BlockPos pos) {
        TileEntity vanillaTile = world.getTileEntity(pos);
        if (vanillaTile != null) {
            return false;
        }

        TileGooBlock gooTile = CustomGooRuntime.getOrCreate(world, pos);
        return gooTile != null && !gooTile.isGooAlive();
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            CustomGooRuntime.tickWorld(event.world);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        CustomGooRuntime.clear(event.getWorld());
    }
}
