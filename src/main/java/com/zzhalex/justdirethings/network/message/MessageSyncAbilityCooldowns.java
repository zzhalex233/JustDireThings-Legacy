package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.data.tool.AbilityCooldown;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageSyncAbilityCooldowns implements IMessage {

    private List<Entry> entries;

    public MessageSyncAbilityCooldowns() {
        this(Collections.emptyList());
    }

    public MessageSyncAbilityCooldowns(List<Entry> entries) {
        this.entries = entries == null ? Collections.emptyList() : new ArrayList<>(entries);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        entries = new ArrayList<>(Math.max(0, count));
        for (int i = 0; i < count; i++) {
            EntityEquipmentSlot slot = EntityEquipmentSlot.values()[buf.readInt()];
            String abilityId = ByteBufUtils.readUTF8String(buf);
            int remainingTicks = buf.readInt();
            boolean active = buf.readBoolean();
            entries.add(new Entry(slot, abilityId, remainingTicks, active));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entries.size());
        for (Entry entry : entries) {
            buf.writeInt(entry.getSlot().ordinal());
            ByteBufUtils.writeUTF8String(buf, entry.getCooldown().getAbilityId());
            buf.writeInt(entry.getCooldown().getRemainingTicks());
            buf.writeBoolean(entry.getCooldown().isActive());
        }
    }

    public static final class Entry {

        private final EntityEquipmentSlot slot;
        private final AbilityCooldown cooldown;

        public Entry(EntityEquipmentSlot slot, String abilityId, int remainingTicks, boolean active) {
            this.slot = slot;
            this.cooldown = new AbilityCooldown(abilityId, remainingTicks, active);
        }

        public EntityEquipmentSlot getSlot() {
            return slot;
        }

        public AbilityCooldown getCooldown() {
            return cooldown;
        }
    }

    public static class Handler implements IMessageHandler<MessageSyncAbilityCooldowns, IMessage> {

        @Override
        public IMessage onMessage(MessageSyncAbilityCooldowns message, MessageContext ctx) {
            JustDireThingsLegacy.proxy.syncAbilityCooldowns(message.entries);
            return null;
        }
    }
}
