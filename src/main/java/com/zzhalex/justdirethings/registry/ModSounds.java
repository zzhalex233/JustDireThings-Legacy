package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModSounds {

    public static final SoundEvent AMETHYST_BLOCK_BREAK = create("block.amethyst_block.break");
    public static final SoundEvent AMETHYST_BLOCK_STEP = create("block.amethyst_block.step");
    public static final SoundEvent AMETHYST_BLOCK_PLACE = create("block.amethyst_block.place");
    public static final SoundEvent AMETHYST_BLOCK_HIT = create("block.amethyst_block.hit");
    public static final SoundEvent AMETHYST_BLOCK_FALL = create("block.amethyst_block.fall");
    public static final SoundEvent AMETHYST_BLOCK_CHIME = create("block.amethyst_block.chime");

    public static final SoundEvent AMETHYST_CLUSTER_BREAK = create("block.amethyst_cluster.break");
    public static final SoundEvent AMETHYST_CLUSTER_STEP = create("block.amethyst_cluster.step");
    public static final SoundEvent AMETHYST_CLUSTER_PLACE = create("block.amethyst_cluster.place");
    public static final SoundEvent AMETHYST_CLUSTER_HIT = create("block.amethyst_cluster.hit");
    public static final SoundEvent AMETHYST_CLUSTER_FALL = create("block.amethyst_cluster.fall");

    public static final SoundEvent SMALL_AMETHYST_BUD_BREAK = create("block.small_amethyst_bud.break");
    public static final SoundEvent SMALL_AMETHYST_BUD_PLACE = create("block.small_amethyst_bud.place");
    public static final SoundEvent MEDIUM_AMETHYST_BUD_BREAK = create("block.medium_amethyst_bud.break");
    public static final SoundEvent MEDIUM_AMETHYST_BUD_PLACE = create("block.medium_amethyst_bud.place");
    public static final SoundEvent LARGE_AMETHYST_BUD_BREAK = create("block.large_amethyst_bud.break");
    public static final SoundEvent LARGE_AMETHYST_BUD_PLACE = create("block.large_amethyst_bud.place");

    public static final SoundEvent RESPAWN_ANCHOR_CHARGE = create("block.respawn_anchor.charge");
    public static final SoundEvent RESPAWN_ANCHOR_DEPLETE = create("block.respawn_anchor.deplete");
    public static final SoundEvent PARADOX_AMBIENT = create("paradox_ambient");
    public static final SoundEvent PORTAL_GUN_CLOSE = create("portal_gun_close");
    public static final SoundEvent PORTAL_GUN_OPEN = create("portal_gun_open");

    private static final SoundEvent[] SOUNDS = {
            AMETHYST_BLOCK_BREAK,
            AMETHYST_BLOCK_STEP,
            AMETHYST_BLOCK_PLACE,
            AMETHYST_BLOCK_HIT,
            AMETHYST_BLOCK_FALL,
            AMETHYST_BLOCK_CHIME,
            AMETHYST_CLUSTER_BREAK,
            AMETHYST_CLUSTER_STEP,
            AMETHYST_CLUSTER_PLACE,
            AMETHYST_CLUSTER_HIT,
            AMETHYST_CLUSTER_FALL,
            SMALL_AMETHYST_BUD_BREAK,
            SMALL_AMETHYST_BUD_PLACE,
            MEDIUM_AMETHYST_BUD_BREAK,
            MEDIUM_AMETHYST_BUD_PLACE,
            LARGE_AMETHYST_BUD_BREAK,
            LARGE_AMETHYST_BUD_PLACE,
            RESPAWN_ANCHOR_CHARGE,
            RESPAWN_ANCHOR_DEPLETE,
            PARADOX_AMBIENT,
            PORTAL_GUN_CLOSE,
            PORTAL_GUN_OPEN
    };

    private ModSounds() {
    }

    @SubscribeEvent
    public static void onRegisterSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(SOUNDS);
    }

    private static SoundEvent create(String path) {
        ResourceLocation id = new ResourceLocation(Reference.MOD_ID, path);
        return new SoundEvent(id).setRegistryName(id);
    }
}
