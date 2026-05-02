package com.zzhalex.justdirethings.client.event;

import com.zzhalex.justdirethings.common.item.tool.ItemPortalGun;
import com.zzhalex.justdirethings.client.render.ThingFinder;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.LeftClickableTool;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.data.tool.AbilityBinding;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageExecuteAbility;
import com.zzhalex.justdirethings.network.message.MessagePortalGunLeftClick;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.MouseEvent;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class ClientPortalGunInputHandler {

    public static final ClientPortalGunInputHandler INSTANCE = new ClientPortalGunInputHandler();

    private ClientPortalGunInputHandler() {
    }

    @SubscribeEvent
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        sendLeftClickIfPortalGun(event);
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        sendLeftClickIfPortalGun(event);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState()) {
            return;
        }
        sendCustomBindings(Keyboard.getEventKey(), false);
    }

    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (!event.isButtonstate() || event.getButton() < 2) {
            return;
        }
        sendCustomBindings(event.getButton(), true);
    }

    public static boolean shouldSendLeftClick(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemPortalGun;
    }

    private static void sendLeftClickIfPortalGun(PlayerInteractEvent event) {
        if (event.getSide() != Side.CLIENT) {
            return;
        }
        sendConfiguredLeftClickAbilities(event.getEntityPlayer(), event.getItemStack(), event.getHand());
        if (!shouldSendLeftClick(event.getItemStack())) {
            return;
        }
        JDTNetwork.getChannel().sendToServer(new MessagePortalGunLeftClick());
    }

    private static void sendConfiguredLeftClickAbilities(EntityPlayer player, ItemStack stack, EnumHand hand) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof LeftClickableTool)) {
            return;
        }
        for (Ability ability : LeftClickableTool.getLeftClickList(stack)) {
            if (ability.requiresUseAction() && isAbilityEnabled(stack, ability)) {
                discoverLocallyIfScanner(player, ability, stack);
                JDTNetwork.getChannel().sendToServer(new MessageExecuteAbility(ability.getId(), hand));
            }
        }
    }

    private static void sendCustomBindings(int keyCode, boolean mouse) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || mc.currentScreen != null) {
            return;
        }

        EntityPlayer player = mc.player;
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool) || !(stack.getItem() instanceof LeftClickableTool)) {
                continue;
            }
            sendMatchingCustomBindings(player, stack, slot, keyCode, mouse);
        }
    }

    private static void sendMatchingCustomBindings(EntityPlayer player, ItemStack stack, int slot, int keyCode, boolean mouse) {
        for (AbilityBinding binding : LeftClickableTool.getCustomBindingList(stack)) {
            Ability ability = Ability.byId(binding.getAbilityId());
            if (ability == null || !ability.requiresUseAction() || binding.isMouseBinding() != mouse || binding.getKeyCode() != keyCode) {
                continue;
            }
            if (LeftClickableTool.getBindingMode(stack, ability) != 2) {
                continue;
            }
            if (!isAbilityEnabled(stack, ability)) {
                continue;
            }
            if (binding.isRequireEquipped() && !isEquipped(player, stack)) {
                continue;
            }
            discoverLocallyIfScanner(player, ability, stack);
            JDTNetwork.getChannel().sendToServer(new MessageExecuteAbility(ability.getId(), slot));
        }
    }

    private static void discoverLocallyIfScanner(EntityPlayer player, Ability ability, ItemStack stack) {
        if (ability == Ability.MOBSCANNER || ability == Ability.ORESCANNER || ability == Ability.OREXRAY) {
            ThingFinder.discover(player, ability, stack);
        }
    }

    private static boolean isEquipped(EntityPlayer player, ItemStack stack) {
        return player.getHeldItemMainhand() == stack
                || player.getHeldItemOffhand() == stack
                || player.inventory.armorInventory.contains(stack);
    }

    private static boolean isAbilityEnabled(ItemStack stack, Ability ability) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return false;
        }
        ToggleableTool tool = (ToggleableTool) stack.getItem();
        return tool.supportsAbility(ability)
                && tool.hasInstalledAbility(stack, ability)
                && tool.getSetting(stack, ability);
    }
}
