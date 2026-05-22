package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import com.zzhalex.justdirethings.common.item.base.LeftClickableTool;
import com.zzhalex.justdirethings.common.item.base.ToolSettingApplier;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.data.tool.AbilityBinding;
import com.zzhalex.justdirethings.data.tool.ToolState;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageExecuteAbility implements IMessage {

    private String abilityId;
    private boolean offhand;
    private int slot = -1;
    private int keyCode = -1;
    private boolean mouse;
    private boolean useOn;
    private BlockPos useOnPos = BlockPos.ORIGIN;
    private EnumFacing useOnFacing = EnumFacing.DOWN;

    public MessageExecuteAbility() {
    }

    public MessageExecuteAbility(String abilityId, EnumHand hand) {
        this.abilityId = abilityId;
        this.offhand = hand == EnumHand.OFF_HAND;
    }

    public MessageExecuteAbility(String abilityId, EnumHand hand, boolean useOn, BlockPos useOnPos, EnumFacing useOnFacing) {
        this.abilityId = abilityId;
        this.offhand = hand == EnumHand.OFF_HAND;
        this.useOn = useOn;
        this.useOnPos = useOnPos == null ? BlockPos.ORIGIN : useOnPos;
        this.useOnFacing = useOnFacing == null ? EnumFacing.DOWN : useOnFacing;
    }

    public MessageExecuteAbility(String abilityId, int slot) {
        this.abilityId = abilityId;
        this.slot = slot;
    }

    public MessageExecuteAbility(String abilityId, int slot, int keyCode, boolean mouse, boolean useOn, BlockPos useOnPos, EnumFacing useOnFacing) {
        this.abilityId = abilityId;
        this.slot = slot;
        this.keyCode = keyCode;
        this.mouse = mouse;
        this.useOn = useOn;
        this.useOnPos = useOnPos == null ? BlockPos.ORIGIN : useOnPos;
        this.useOnFacing = useOnFacing == null ? EnumFacing.DOWN : useOnFacing;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        abilityId = ByteBufUtils.readUTF8String(buf);
        offhand = buf.readBoolean();
        slot = buf.readInt();
        keyCode = buf.readInt();
        mouse = buf.readBoolean();
        useOn = buf.readBoolean();
        useOnPos = BlockPos.fromLong(buf.readLong());
        useOnFacing = EnumFacing.byIndex(buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, abilityId == null ? "" : abilityId);
        buf.writeBoolean(offhand);
        buf.writeInt(slot);
        buf.writeInt(keyCode);
        buf.writeBoolean(mouse);
        buf.writeBoolean(useOn);
        buf.writeLong(useOnPos.toLong());
        buf.writeInt(useOnFacing.getIndex());
    }

    public static class Handler implements IMessageHandler<MessageExecuteAbility, IMessage> {

        @Override
        public IMessage onMessage(MessageExecuteAbility message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> executeAbility(
                    player,
                    message.abilityId,
                    message.offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND,
                    message.slot,
                    message.keyCode,
                    message.mouse,
                    message.useOn,
                    message.useOnPos,
                    message.useOnFacing
            ));
            return null;
        }

        private static void executeAbility(
                EntityPlayerMP player,
                String abilityId,
                EnumHand hand,
                int slot,
                int keyCode,
                boolean mouse,
                boolean useOn,
                BlockPos useOnPos,
                EnumFacing useOnFacing
        ) {
            Ability ability = Ability.byId(abilityId);
            if (ability == null) {
                return;
            }

            ItemStack stack = slot >= 0 ? player.inventory.getStackInSlot(slot) : player.getHeldItem(hand);
            if (stack.isEmpty()) {
                return;
            }
            if (!isValidRequestedAbility(player, stack, ability, hand, slot, keyCode, mouse, useOn)) {
                return;
            }

            if (useOn) {
                RayTraceResult hit = getUseOnHit(player, useOnPos, useOnFacing);
                if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK || hit.getBlockPos() == null || hit.sideHit == null) {
                    return;
                }
                AbilityMethods.executeUseOn(ability, player.world, player, stack, hit.getBlockPos(), hit.sideHit, hand);
            } else if (ability.requiresUseAction()) {
                AbilityMethods.execute(ability, player.world, player, stack);
            } else {
                applyPassiveCustomBinding(player, stack, ability);
            }
        }

        private static boolean isValidRequestedAbility(EntityPlayerMP player, ItemStack stack, Ability ability, EnumHand hand, int slot, int keyCode, boolean mouse, boolean useOn) {
            if (!(stack.getItem() instanceof ToggleableTool)) {
                return false;
            }
            ToggleableTool tool = (ToggleableTool) stack.getItem();
            ToolState state = ToggleableTool.readToolState(stack);
            if (!canExecuteRequested(tool, stack, state, ability)) {
                return false;
            }
            if (slot >= 0) {
                return stack.getItem() instanceof LeftClickableTool && canUseCustomBindingFromSlot(player, stack, ability, slot, keyCode, mouse);
            }
            if (player.getHeldItem(hand) != stack || ability.getBindingType() != Ability.BindingType.LEFT_AND_CUSTOM) {
                return false;
            }
            if (!(stack.getItem() instanceof LeftClickableTool)) {
                return true;
            }
            int bindingMode = LeftClickableTool.getBindingMode(stack, ability);
            return bindingMode == 1;
        }

        private static boolean canUseCustomBindingFromSlot(EntityPlayerMP player, ItemStack stack, Ability ability, int slot, int keyCode, boolean mouse) {
            AbilityBinding binding = LeftClickableTool.getAbilityBinding(stack, ability);
            return binding != null
                    && binding.getKeyCode() == keyCode
                    && binding.isMouseBinding() == mouse
                    && LeftClickableTool.getBindingMode(stack, ability) == 2
                    && (!binding.isRequireEquipped() || LeftClickableTool.isInventorySlotEquipped(player, slot));
        }

        private static boolean canExecuteRequested(ToggleableTool tool, ItemStack stack, ToolState state, Ability ability) {
            return tool != null
                    && stack != null
                    && !stack.isEmpty()
                    && state != null
                    && ability != null
                    && tool.supportsAbility(ability)
                    && tool.hasInstalledAbility(stack, ability)
                    && tool.isEnabled(stack)
                    && (isPassiveBindingAbility(ability) || tool.getSetting(stack, ability));
        }

        private static void applyPassiveCustomBinding(EntityPlayerMP player, ItemStack stack, Ability ability) {
            if (!(stack.getItem() instanceof ToggleableTool)) {
                return;
            }
            if (!isPassiveBindingAbility(ability)) {
                return;
            }
            ToggleableTool tool = (ToggleableTool) stack.getItem();
            int mode = ability.getSettingType() == Ability.SettingType.CYCLE ? 1 : 0;
            if (ToolSettingApplier.applySlotSetting(stack, ability.getId(), mode, 0)) {
                sendPassiveToggleMessage(player, stack, tool, ability);
                player.inventoryContainer.detectAndSendChanges();
                if (player.openContainer != null) {
                    player.openContainer.detectAndSendChanges();
                }
            }
        }

        private static void sendPassiveToggleMessage(EntityPlayerMP player, ItemStack stack, ToggleableTool tool, Ability ability) {
            if (ability.getSettingType() == Ability.SettingType.CYCLE && tool.getSetting(stack, ability)) {
                int currentValue = tool.getToolValue(stack, ability);
                player.sendStatusMessage(new TextComponentTranslation(
                        "justdirethings.ability",
                        new TextComponentTranslation(ability.getTranslationKey() + "_" + currentValue),
                        new TextComponentTranslation("justdirethings.enabled")
                ), true);
                return;
            }
            player.sendStatusMessage(new TextComponentTranslation(
                    "justdirethings.ability",
                    new TextComponentTranslation(ability.getTranslationKey()),
                    new TextComponentTranslation(tool.getSetting(stack, ability) ? "justdirethings.enabled" : "justdirethings.disabled")
            ), true);
        }

        private static boolean isPassiveBindingAbility(Ability ability) {
            Ability.UseType useType = ability.getUseType();
            return useType == Ability.UseType.PASSIVE
                    || useType == Ability.UseType.PASSIVE_TICK
                    || useType == Ability.UseType.PASSIVE_COOLDOWN
                    || useType == Ability.UseType.PASSIVE_TICK_COOLDOWN;
        }

        private static RayTraceResult getUseOnHit(EntityPlayerMP player, BlockPos fallbackPos, EnumFacing fallbackFacing) {
            double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
            RayTraceResult hit = player.rayTrace(reach, 1.0F);
            if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
                return hit;
            }
            if (fallbackPos != null && fallbackFacing != null && !fallbackPos.equals(BlockPos.ORIGIN)) {
                return new RayTraceResult(RayTraceResult.Type.BLOCK, null, fallbackFacing, fallbackPos);
            }
            return null;
        }
    }
}
