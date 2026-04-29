package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.item.base.ToolSettingApplier;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageToolSlotSetting implements IMessage {

    private String abilityId;
    private int slot;
    private int mode;
    private int value;

    public MessageToolSlotSetting() {
    }

    public MessageToolSlotSetting(String abilityId, int slot, int mode, int value) {
        this.abilityId = abilityId;
        this.slot = slot;
        this.mode = mode;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        abilityId = ByteBufUtils.readUTF8String(buf);
        slot = buf.readInt();
        mode = buf.readInt();
        value = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, abilityId == null ? "" : abilityId);
        buf.writeInt(slot);
        buf.writeInt(mode);
        buf.writeInt(value);
    }

    public static class Handler implements IMessageHandler<MessageToolSlotSetting, IMessage> {

        @Override
        public IMessage onMessage(MessageToolSlotSetting message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack stack = player.inventory.getStackInSlot(message.slot);
                if (ToolSettingApplier.applySlotSetting(stack, message.abilityId, message.mode, message.value)) {
                    player.inventoryContainer.detectAndSendChanges();
                    if (player.openContainer != null) {
                        player.openContainer.detectAndSendChanges();
                    }
                }
            });
            return null;
        }
    }
}
