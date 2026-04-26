package com.zzhalex.justdirethings.common.item;

import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.item.Item;

/**
 * PARITY STUB: explicit behavior placeholder for upstream items that are registered
 * for ID/resource parity but still need their real gameplay ported.
 */
public class ItemParityStub extends Item {

    private final String upstreamClassName;
    private final String implementationPhase;

    public ItemParityStub(String upstreamClassName, String implementationPhase) {
        this.upstreamClassName = upstreamClassName;
        this.implementationPhase = implementationPhase;
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
    }

    public String getUpstreamClassName() {
        return upstreamClassName;
    }

    public String getImplementationPhase() {
        return implementationPhase;
    }
}
