package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import com.zzhalex.justdirethings.common.item.base.AbilityExecutionHelper;
import com.zzhalex.justdirethings.common.item.base.LeftClickableTool;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.data.tool.ToolState;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageExecuteAbility implements IMessage {

    private String abilityId;
    private boolean offhand;
    private int slot = -1;

    public MessageExecuteAbility() {
    }

    public MessageExecuteAbility(String abilityId, EnumHand hand) {
        this.abilityId = abilityId;
        this.offhand = hand == EnumHand.OFF_HAND;
    }

    public MessageExecuteAbility(String abilityId, int slot) {
        this.abilityId = abilityId;
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        abilityId = ByteBufUtils.readUTF8String(buf);
        offhand = buf.readBoolean();
        slot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, abilityId == null ? "" : abilityId);
        buf.writeBoolean(offhand);
        buf.writeInt(slot);
    }

    public static class Handler implements IMessageHandler<MessageExecuteAbility, IMessage> {

        @Override
        public IMessage onMessage(MessageExecuteAbility message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> executeAbility(player, message.abilityId, message.offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, message.slot));
            return null;
        }

        private static void executeAbility(EntityPlayerMP player, String abilityId, EnumHand hand, int slot) {
            Ability ability = Ability.byId(abilityId);
            if (ability == null || !ability.requiresUseAction()) {
                return;
            }

            ItemStack stack = slot >= 0 ? player.inventory.getStackInSlot(slot) : player.getHeldItem(hand);
            if (stack.isEmpty()) {
                return;
            }
            if (!isValidRequestedAbility(player, stack, ability, hand, slot)) {
                return;
            }

            AbilityMethods.execute(ability, player.world, player, stack);
        }

        private static boolean isValidRequestedAbility(EntityPlayerMP player, ItemStack stack, Ability ability, EnumHand hand, int slot) {
            if (!(stack.getItem() instanceof ToggleableTool)) {
                return false;
            }
            ToggleableTool tool = (ToggleableTool) stack.getItem();
            ToolState state = ToggleableTool.readToolState(stack);
            if (!AbilityExecutionHelper.canExecuteFromDirectUse(tool, stack, state, ability)) {
                return false;
            }
            if (slot >= 0) {
                return stack.getItem() instanceof LeftClickableTool
                        && LeftClickableTool.getBindingMode(stack, ability) == 2;
            }
            return player.getHeldItem(hand) == stack
                    && (ability.getBindingType() == Ability.BindingType.LEFT_AND_CUSTOM
                    || (stack.getItem() instanceof LeftClickableTool && LeftClickableTool.getBindingMode(stack, ability) == 1));
        }
    }
}
