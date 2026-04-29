package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.item.base.ToolSettingApplier;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageToolBindingSetting implements IMessage {

    private int slot;
    private String abilityId;
    private int button;
    private int keyCode;
    private boolean mouse;
    private boolean requireEquipped;

    public MessageToolBindingSetting() {
    }

    public MessageToolBindingSetting(int slot, String abilityId, int button, int keyCode, boolean mouse, boolean requireEquipped) {
        this.slot = slot;
        this.abilityId = abilityId;
        this.button = button;
        this.keyCode = keyCode;
        this.mouse = mouse;
        this.requireEquipped = requireEquipped;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slot = buf.readInt();
        abilityId = ByteBufUtils.readUTF8String(buf);
        button = buf.readInt();
        keyCode = buf.readInt();
        mouse = buf.readBoolean();
        requireEquipped = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slot);
        ByteBufUtils.writeUTF8String(buf, abilityId == null ? "" : abilityId);
        buf.writeInt(button);
        buf.writeInt(keyCode);
        buf.writeBoolean(mouse);
        buf.writeBoolean(requireEquipped);
    }

    public static class Handler implements IMessageHandler<MessageToolBindingSetting, IMessage> {

        @Override
        public IMessage onMessage(MessageToolBindingSetting message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack stack = player.inventory.getStackInSlot(message.slot);
                if (ToolSettingApplier.applyBinding(stack, message.abilityId, message.button, message.keyCode, message.mouse, message.requireEquipped)) {
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
