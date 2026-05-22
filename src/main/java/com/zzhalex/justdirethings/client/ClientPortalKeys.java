package com.zzhalex.justdirethings.client;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public final class ClientPortalKeys {

    public static final KeyBinding TOGGLE_TOOL = new KeyBinding(
            "justdirethings.key.toggle_tool",
            Keyboard.KEY_V,
            "justdirethings.key.category"
    );

    private ClientPortalKeys() {
    }
}
