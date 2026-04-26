package com.zzhalex.justdirethings.network;

import com.zzhalex.justdirethings.network.message.MessageSyncToolState;
import com.zzhalex.justdirethings.network.message.MessagePortalGunLeftClick;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageRoundTripTest {

    @Test
    void syncToolStateMessageEncodesAndDecodes() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Ability", "flight");

        MessageSyncToolState original = new MessageSyncToolState(3, tag);
        ByteBuf buf = Unpooled.buffer();
        original.toBytes(buf);

        MessageSyncToolState copy = new MessageSyncToolState();
        copy.fromBytes(buf);

        assertEquals(3, copy.getSlot());
        assertEquals("flight", copy.getToolStateTag().getString("Ability"));
    }

    @Test
    void portalGunLeftClickMessageHasNoPayload() {
        MessagePortalGunLeftClick original = new MessagePortalGunLeftClick();
        ByteBuf buf = Unpooled.buffer();
        original.toBytes(buf);

        MessagePortalGunLeftClick copy = new MessagePortalGunLeftClick();
        copy.fromBytes(buf);

        assertEquals(0, buf.readableBytes());
    }
}
