package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.entity.EntityCreatureCatcher;
import com.zzhalex.justdirethings.common.entity.EntityDecoy;
import com.zzhalex.justdirethings.common.entity.EntityJustDireAreaEffectCloud;
import com.zzhalex.justdirethings.common.entity.EntityJustDireArrow;
import com.zzhalex.justdirethings.common.entity.EntityParadox;
import com.zzhalex.justdirethings.common.entity.EntityPortal;
import com.zzhalex.justdirethings.common.entity.EntityPortalProjectile;
import com.zzhalex.justdirethings.common.entity.EntityTimeWand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.event.RegistryEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModEntities {

    private static int nextEntityId = 0;
    public static final ResourceLocation CREATURE_CATCHER_ENTITY_ID = new ResourceLocation(Reference.MOD_ID, "creature_catcher");
    public static final ResourceLocation JUST_DIRE_ARROW_ENTITY_ID = new ResourceLocation(Reference.MOD_ID, "justdirearrow");
    public static final ResourceLocation DECOY_ENTITY_ID = new ResourceLocation(Reference.MOD_ID, "decoy_entity");
    public static final ResourceLocation AREA_EFFECT_CLOUD_ENTITY_ID = new ResourceLocation(Reference.MOD_ID, "justdireareaeffectcloud");
    public static final ResourceLocation TIME_WAND_ENTITY_ID = new ResourceLocation(Reference.MOD_ID, "time_wand_entity");
    public static final ResourceLocation PORTAL_PROJECTILE_ENTITY_ID = new ResourceLocation(Reference.MOD_ID, "portal_projectile");
    public static final ResourceLocation PORTAL_ENTITY_ID = new ResourceLocation(Reference.MOD_ID, "portal_entity");
    public static final ResourceLocation PARADOX_ENTITY_ID = new ResourceLocation(Reference.MOD_ID, "paradox_entity");

    private ModEntities() {
    }

    @SubscribeEvent
    public static void onRegisterEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(
                EntityEntryBuilder.create()
                        .entity(EntityCreatureCatcher.class)
                        .id(CREATURE_CATCHER_ENTITY_ID, nextEntityId++)
                        .name(CREATURE_CATCHER_ENTITY_ID.toString())
                        .tracker(64, 1, true)
                        .build()
        );
        event.getRegistry().register(
                EntityEntryBuilder.create()
                        .entity(EntityJustDireArrow.class)
                        .id(JUST_DIRE_ARROW_ENTITY_ID, nextEntityId++)
                        .name(JUST_DIRE_ARROW_ENTITY_ID.toString())
                        .tracker(64, 1, true)
                        .build()
        );
        event.getRegistry().register(
                EntityEntryBuilder.create()
                        .entity(EntityDecoy.class)
                        .id(DECOY_ENTITY_ID, nextEntityId++)
                        .name(DECOY_ENTITY_ID.toString())
                        .tracker(64, 1, true)
                        .build()
        );
        event.getRegistry().register(
                EntityEntryBuilder.create()
                        .entity(EntityJustDireAreaEffectCloud.class)
                        .id(AREA_EFFECT_CLOUD_ENTITY_ID, nextEntityId++)
                        .name(AREA_EFFECT_CLOUD_ENTITY_ID.toString())
                        .tracker(64, 1, true)
                        .build()
        );
        event.getRegistry().register(
                EntityEntryBuilder.create()
                        .entity(EntityTimeWand.class)
                        .id(TIME_WAND_ENTITY_ID, nextEntityId++)
                        .name(TIME_WAND_ENTITY_ID.toString())
                        .tracker(64, 1, true)
                        .build()
        );
        event.getRegistry().register(
                EntityEntryBuilder.create()
                        .entity(EntityPortalProjectile.class)
                        .id(PORTAL_PROJECTILE_ENTITY_ID, nextEntityId++)
                        .name(PORTAL_PROJECTILE_ENTITY_ID.toString())
                        .tracker(64, 1, true)
                        .build()
        );
        event.getRegistry().register(
                EntityEntryBuilder.create()
                        .entity(EntityPortal.class)
                        .id(PORTAL_ENTITY_ID, nextEntityId++)
                        .name(PORTAL_ENTITY_ID.toString())
                        .tracker(128, 1, false)
                        .build()
        );
        event.getRegistry().register(
                EntityEntryBuilder.create()
                        .entity(EntityParadox.class)
                        .id(PARADOX_ENTITY_ID, nextEntityId++)
                        .name(PARADOX_ENTITY_ID.toString())
                        .tracker(128, 1, false)
                        .build()
        );
    }

    public static ResourceLocation[] coreEntityIds() {
        return new ResourceLocation[] {
                CREATURE_CATCHER_ENTITY_ID,
                JUST_DIRE_ARROW_ENTITY_ID,
                PORTAL_PROJECTILE_ENTITY_ID,
                PORTAL_ENTITY_ID,
                DECOY_ENTITY_ID,
                AREA_EFFECT_CLOUD_ENTITY_ID,
                TIME_WAND_ENTITY_ID,
                PARADOX_ENTITY_ID
        };
    }
}
