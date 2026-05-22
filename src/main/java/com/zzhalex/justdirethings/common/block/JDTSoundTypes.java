package com.zzhalex.justdirethings.common.block;

import com.zzhalex.justdirethings.registry.ModSounds;
import net.minecraft.block.SoundType;

public final class JDTSoundTypes {

    public static final SoundType AMETHYST = new SoundType(
            1.0F,
            1.0F,
            ModSounds.AMETHYST_BLOCK_BREAK,
            ModSounds.AMETHYST_BLOCK_STEP,
            ModSounds.AMETHYST_BLOCK_PLACE,
            ModSounds.AMETHYST_BLOCK_HIT,
            ModSounds.AMETHYST_BLOCK_FALL);

    public static final SoundType AMETHYST_CLUSTER = new SoundType(
            1.0F,
            1.0F,
            ModSounds.AMETHYST_CLUSTER_BREAK,
            ModSounds.AMETHYST_CLUSTER_STEP,
            ModSounds.AMETHYST_CLUSTER_PLACE,
            ModSounds.AMETHYST_CLUSTER_HIT,
            ModSounds.AMETHYST_CLUSTER_FALL);

    public static final SoundType SMALL_AMETHYST_BUD = new SoundType(
            1.0F,
            1.0F,
            ModSounds.SMALL_AMETHYST_BUD_BREAK,
            ModSounds.AMETHYST_CLUSTER_STEP,
            ModSounds.SMALL_AMETHYST_BUD_PLACE,
            ModSounds.AMETHYST_CLUSTER_HIT,
            ModSounds.AMETHYST_CLUSTER_FALL);

    public static final SoundType MEDIUM_AMETHYST_BUD = new SoundType(
            1.0F,
            1.0F,
            ModSounds.MEDIUM_AMETHYST_BUD_BREAK,
            ModSounds.AMETHYST_CLUSTER_STEP,
            ModSounds.MEDIUM_AMETHYST_BUD_PLACE,
            ModSounds.AMETHYST_CLUSTER_HIT,
            ModSounds.AMETHYST_CLUSTER_FALL);

    public static final SoundType LARGE_AMETHYST_BUD = new SoundType(
            1.0F,
            1.0F,
            ModSounds.LARGE_AMETHYST_BUD_BREAK,
            ModSounds.AMETHYST_CLUSTER_STEP,
            ModSounds.LARGE_AMETHYST_BUD_PLACE,
            ModSounds.AMETHYST_CLUSTER_HIT,
            ModSounds.AMETHYST_CLUSTER_FALL);

    private JDTSoundTypes() {
    }
}
