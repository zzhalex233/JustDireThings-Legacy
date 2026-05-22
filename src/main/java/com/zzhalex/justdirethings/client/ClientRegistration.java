package com.zzhalex.justdirethings.client;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.ClientPortalKeys;
import com.zzhalex.justdirethings.client.event.AbilityRenderHandler;
import com.zzhalex.justdirethings.client.event.ClientPortalGunInputHandler;
import com.zzhalex.justdirethings.client.event.ElytraAbilityInputHandler;
import com.zzhalex.justdirethings.client.event.MachineAreaRenderHandler;
import com.zzhalex.justdirethings.client.event.MiningPreviewRenderHandler;
import com.zzhalex.justdirethings.client.event.PhaseClientEventHandler;
import com.zzhalex.justdirethings.client.overlay.AbilityCooldownOverlay;
import com.zzhalex.justdirethings.client.render.CreatureCatcherModelBakeHandler;
import com.zzhalex.justdirethings.client.render.RenderCreatureCatcher;
import com.zzhalex.justdirethings.client.render.RenderCreatureCatcherItemStack;
import com.zzhalex.justdirethings.client.render.RenderDecoy;
import com.zzhalex.justdirethings.client.render.RenderFireResistantItem;
import com.zzhalex.justdirethings.client.render.RenderParadox;
import com.zzhalex.justdirethings.client.render.RenderPortal;
import com.zzhalex.justdirethings.client.render.RenderPortalProjectile;
import com.zzhalex.justdirethings.client.render.RenderTimeWand;
import com.zzhalex.justdirethings.client.render.ThingFinder;
import com.zzhalex.justdirethings.client.render.tile.RenderEclipseGate;
import com.zzhalex.justdirethings.client.render.tile.RenderExperienceHolder;
import com.zzhalex.justdirethings.client.render.tile.RenderGooBlock;
import com.zzhalex.justdirethings.client.render.tile.RenderInventoryHolder;
import com.zzhalex.justdirethings.common.entity.EntityCreatureCatcher;
import com.zzhalex.justdirethings.common.entity.EntityDecoy;
import com.zzhalex.justdirethings.common.entity.EntityFireResistantItem;
import com.zzhalex.justdirethings.common.entity.EntityJustDireAreaEffectCloud;
import com.zzhalex.justdirethings.common.entity.EntityJustDireArrow;
import com.zzhalex.justdirethings.common.entity.EntityParadox;
import com.zzhalex.justdirethings.common.entity.EntityPortal;
import com.zzhalex.justdirethings.common.entity.EntityPortalProjectile;
import com.zzhalex.justdirethings.common.entity.EntityTimeWand;
import com.zzhalex.justdirethings.common.item.misc.FluidCanisterItem;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import com.zzhalex.justdirethings.common.item.tool.ItemPortalGunV2;
import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import com.zzhalex.justdirethings.common.tile.goo.TileGooBlock;
import com.zzhalex.justdirethings.common.tile.TileEclipseGate;
import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import com.zzhalex.justdirethings.registry.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.entity.RenderAreaEffectCloud;
import net.minecraft.client.renderer.entity.RenderTippedArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.ToIntFunction;

@SideOnly(Side.CLIENT)
public final class ClientRegistration {

    private static final ResourceLocation FULLNESS = new ResourceLocation(Reference.MOD_ID, "fullness");
    private static final ResourceLocation POTION_FULLNESS = new ResourceLocation(Reference.MOD_ID, "potion_fullness");
    private static final ResourceLocation ENABLED = new ResourceLocation(Reference.MOD_ID, "enabled");
    private static boolean initialized;

    private ClientRegistration() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        registerEntityRenderers();
        registerTileEntityRenderers();
        registerItemStackRenderers();
        registerItemProperties();
        ClientRegistry.registerKeyBinding(ClientPortalKeys.TOGGLE_TOOL);
        MinecraftForge.EVENT_BUS.register(CreatureCatcherModelBakeHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(AbilityCooldownOverlay.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ClientPortalGunInputHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ElytraAbilityInputHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PhaseClientEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(MiningPreviewRenderHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(AbilityRenderHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(MachineAreaRenderHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ThingFinder.INSTANCE);
    }

    private static void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityCreatureCatcher.class, RenderCreatureCatcher::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTimeWand.class, RenderTimeWand::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPortalProjectile.class, RenderPortalProjectile::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPortal.class, RenderPortal::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityDecoy.class, RenderDecoy::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityParadox.class, RenderParadox::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityJustDireArrow.class, RenderTippedArrow::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityJustDireAreaEffectCloud.class, RenderAreaEffectCloud::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFireResistantItem.class, RenderFireResistantItem::new);
    }

    private static void registerTileEntityRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileGooBlock.Tier1.class, new RenderGooBlock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileGooBlock.Tier2.class, new RenderGooBlock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileGooBlock.Tier3.class, new RenderGooBlock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileGooBlock.Tier4.class, new RenderGooBlock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileExperienceHolder.class, new RenderExperienceHolder());
        ClientRegistry.bindTileEntitySpecialRenderer(TileInventoryHolder.class, new RenderInventoryHolder());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEclipseGate.class, new RenderEclipseGate());
    }

    private static void registerItemStackRenderers() {
        ModItems.CREATURE_CATCHER.setTileEntityItemStackRenderer(new RenderCreatureCatcherItemStack());
    }

    private static void registerItemProperties() {
        registerIntProperty(ModItems.POCKET_GENERATOR, ENABLED, PocketGeneratorItem::getEnabledModelState);
        registerIntProperty(ModItems.PORTAL_GUN_V2, FULLNESS, ItemPortalGunV2::getFullness);
        registerIntProperty(ModItems.FLUID_CANISTER, FULLNESS, FluidCanisterItem::getFullness);
        registerIntProperty(ModItems.POTION_CANISTER, POTION_FULLNESS, PotionCanisterItem::getFullness);
    }

    private static void registerIntProperty(Item item, ResourceLocation key, ToIntFunction<ItemStack> reader) {
        if (item == null || key == null || reader == null) {
            return;
        }
        item.addPropertyOverride(key, (stack, worldIn, entityIn) -> reader.applyAsInt(stack));
    }
}
