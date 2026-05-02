package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GooFluidJeiParityTest {

    @Test
    void gooBlocksPortUpstreamAliveStateRevivalAndTileRuntime() throws IOException {
        String gooBlock = read("src/main/java/com/zzhalex/justdirethings/common/block/goo/BlockGooBlock.java");
        String gooTile = read("src/main/java/com/zzhalex/justdirethings/common/tile/goo/TileGooBlock.java");
        String contentBlocks = read("src/main/java/com/zzhalex/justdirethings/registry/ModContentBlocks.java");
        String tiles = read("src/main/java/com/zzhalex/justdirethings/registry/ModTileEntities.java");

        for (String token : Arrays.asList("PropertyBool ALIVE", "onBlockActivated", "validRevivalItem", "createNewTileEntity")) {
            assertTrue(gooBlock.contains(token), "Goo block should port upstream alive/revival behavior: " + token);
        }
        assertTrue(gooBlock.contains("hasTileEntity(IBlockState state)") && gooBlock.contains("createTileEntity(World world, IBlockState state)"),
                "Goo block should explicitly expose its tile entity hooks in the 1.12 Forge style used by the references");
        assertTrue(gooBlock.contains("withProperty(ALIVE, false)") && !gooBlock.contains("withProperty(ALIVE, !JDTConfig.gooCanDie)"),
                "Upstream goo blocks always place dead first; gooCanDie only controls whether completed crafts can kill them later");
        for (String token : Arrays.asList("sideCounters", "sideDurations", "counterReducer", "findGooSpreadRecipe", "setBlockToTarget")) {
            assertTrue(gooTile.contains(token), "Goo tile should port upstream sided spread runtime: " + token);
        }
        assertTrue(contentBlocks.contains("new BlockGooBlock"), "Goo content blocks should use the real goo block class, not a simple placeholder block");
        assertTrue(tiles.contains("TileGooBlock.Tier1") && tiles.contains("TileGooBlock.Tier4"),
                "All goo tiers should have tile entities registered");
    }

    @Test
    void gooTileRuntimePortsUpstreamCacheAndSideCounterLifecycle() throws IOException {
        String gooTile = read("src/main/java/com/zzhalex/justdirethings/common/tile/goo/TileGooBlock.java");

        for (String token : Arrays.asList("outputCache", "durationCache", "updateSideCounter", "findOutput", "findDuration", "populateCaches")) {
            assertTrue(gooTile.contains(token), "Goo tile should mirror upstream cached side recipe lookup: " + token);
        }
        assertTrue(gooTile.contains("oldCounter") && gooTile.contains("spawnParticles"),
                "Side counter updates should keep the upstream reset transition hook used by the client renderer");
        assertTrue(!gooTile.contains("nextCounter == 0 || updateSideCounter"),
                "Goo spread countdown must write sideCounters=0 instead of short-circuiting before updateSideCounter");
        assertTrue(gooTile.contains("boolean sideChanged = updateSideCounter(facing, nextCounter)")
                        && gooTile.contains("if (nextCounter == 0 || sideChanged)"),
                "Goo spread countdown should update the counter first, then decide whether to sync");
    }

    @Test
    void gooRendererPortsUpstreamRevivalItemsAndInfectionPreview() throws IOException {
        Path rendererPath = path("src/main/java/com/zzhalex/justdirethings/client/render/tile/RenderGooBlock.java");
        assertTrue(Files.exists(rendererPath), "Goo block needs a 1.12 TESR port of upstream GooBlockRender_Base");

        String renderer = read("src/main/java/com/zzhalex/justdirethings/client/render/tile/RenderGooBlock.java");
        String clientRegistration = read("src/main/java/com/zzhalex/justdirethings/client/ClientRegistration.java");

        assertTrue(renderer.contains("extends TileEntitySpecialRenderer<TileGooBlock>"),
                "Goo renderer should be a tile entity special renderer in 1.12");
        for (String token : Arrays.asList(
                "renderFloatingItem",
                "getNextItemFromTier",
                "getOffsetPositionForSide",
                "applyRotationForSide",
                "renderTextures",
                "renderTexturePattern",
                "renderPatternDepthOnly",
                "renderTargetDepthEqual",
                "renderModelQuads",
                "tintQuadColor",
                "applyDirectionRotation",
                "BlockGooPattern.GOOSTAGE",
                "getRemainingTimeFor",
                "getCraftingDuration"
        )) {
            assertTrue(renderer.contains(token), "Goo renderer should port upstream render behavior: " + token);
        }
        assertTrue(renderer.contains("isGlobalRenderer(TileGooBlock"),
                "Goo TESR should opt out of the local block-bound culling because infection previews render on neighboring blocks");
        assertTrue(renderer.contains("colorMask(false, false, false, false)") && renderer.contains("depthMask(true)")
                        && renderer.contains("GlStateManager.depthFunc(GL11.GL_LEQUAL)"),
                "Goo pattern pass should mirror upstream GooPattern: alpha-tested pattern model writes depth only");
        assertTrue(renderer.contains("GlStateManager.depthFunc(GL11.GL_EQUAL)") && renderer.contains("depthMask(false)")
                        && renderer.contains("enableBlend"),
                "Goo target pass should mirror upstream RenderBlockBackface: blended target model draws only where pattern wrote depth");
        assertTrue(renderer.contains("getBlockRendererDispatcher().getModelForState(pattern)")
                        && renderer.contains("getBlockRendererDispatcher().getModelForState(renderState)")
                        && renderer.contains("model.getQuads(state, face, 0L)")
                        && renderer.contains("model.getQuads(state, null, 0L)"),
                "Goo renderer should render baked pattern and goo models, not handmade decal quads");
        assertTrue(renderer.contains("blockentity.getWorld().getBlockState(blockentity.getPos())")
                        && !renderer.contains("IBlockState renderState = blockentity.getRenderStateFor(direction)"),
                "Upstream renders the goo block through the pattern mask; target block colors must not drive the vine preview");
        assertTrue(renderer.contains("getBlockColors()") && renderer.contains("colorMultiplier")
                        && renderer.contains("quad.getTintIndex()"),
                "Goo target pass should keep upstream goo block tint and alpha");
        assertTrue(renderer.contains("GlStateManager.translate(-translateF, -translateF, -translateF)")
                        && renderer.contains("GlStateManager.scale(1.0F + scaleF, 1.0F + scaleF, 1.0F + scaleF)"),
                "Goo infection renderer should apply upstream tier-based inflate transform");
        assertTrue(renderer.contains("applyDirectionRotation") && renderer.contains("getAoDirection"),
                "Goo infection renderer should rotate the shared pattern/target model and remap AO directions like upstream");
        for (String token : Arrays.asList(
                "case DOWN:",
                "GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);",
                "case NORTH:",
                "GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);",
                "GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);",
                "case SOUTH:",
                "case WEST:",
                "GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);",
                "case EAST:",
                "GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);"
        )) {
            assertTrue(renderer.contains(token), "Goo renderer should mirror Direction#getRotation in 1.12 GL calls: " + token);
        }
        assertTrue(!renderer.contains("renderOverlay") && !renderer.contains("addFaceQuad")
                        && !renderer.contains("POSITION_TEX") && !renderer.contains("TEX_TOP"),
                "Goo infection preview should not regress to the handmade six-face goo texture decal");
        assertTrue(clientRegistration.contains("ClientRegistry.bindTileEntitySpecialRenderer")
                        && clientRegistration.contains("TileGooBlock.Tier1")
                        && clientRegistration.contains("TileGooBlock.Tier4")
                        && clientRegistration.contains("new RenderGooBlock()"),
                "All goo tile tiers should be bound to the goo renderer during client registration");
    }

    @Test
    void creativeTabMirrorsUpstreamGroupingAndIncludesFluidBuckets() throws IOException {
        String tab = read("src/main/java/com/zzhalex/justdirethings/registry/ModCreativeTabs.java");
        String modItems = read("src/main/java/com/zzhalex/justdirethings/registry/ModItems.java");
        String contentItems = read("src/main/java/com/zzhalex/justdirethings/registry/ModContentItems.java");
        String equipmentItems = read("src/main/java/com/zzhalex/justdirethings/registry/ModEquipmentItems.java");

        assertTrue(tab.contains("displayAllRelevantItems(NonNullList<ItemStack> items)"),
                "1.12 creative tab should override displayAllRelevantItems so ordering is stable instead of registry/default order");
        assertTrue(tab.contains("addItems(items, ModContentItems.blockItems())")
                        && tab.contains("addItems(items, ModItems.machineBlockItems())")
                        && tab.contains("addItems(items, ModContentItems.resourceItems())")
                        && tab.contains("addItems(items, ModItems.generalItems())")
                        && tab.contains("addItems(items, ModContentItems.templateItems())"),
                "Creative tab ITEMS section should be manually grouped before buckets, matching upstream ITEMS register before BUCKET_ITEMS");
        assertTrue(tab.indexOf("addItems(items, ModContentItems.templateItems())") < tab.indexOf("addFluidBuckets(items)")
                        && tab.indexOf("addFluidBuckets(items)") < tab.indexOf("addItems(items, ModEquipmentItems.toolItems())")
                        && tab.indexOf("addItems(items, ModEquipmentItems.toolItems())") < tab.indexOf("addItems(items, ModEquipmentItems.bowItems())")
                        && tab.indexOf("addItems(items, ModEquipmentItems.bowItems())") < tab.indexOf("addItems(items, ModEquipmentItems.armorItems())")
                        && tab.indexOf("addItems(items, ModEquipmentItems.armorItems())") < tab.indexOf("addItems(items, ModContentItems.upgradeItems())"),
                "Creative tab order should mirror upstream: ITEMS, BUCKET_ITEMS, TOOLS, BOWS, ARMORS, UPGRADES");
        assertTrue(tab.contains("ModFluids.coreFluidIds()") && tab.contains("ModFluids.getFluid(id)")
                        && tab.contains("FluidUtil.getFilledBucket(new FluidStack(fluid, 1000))")
                        && tab.contains("!bucket.isEmpty()"),
                "Creative tab should add Forge universal bucket stacks for each JDT fluid");
        assertTrue(modItems.contains("machineBlockItems()") && modItems.contains("generalItems()"),
                "Legacy item registry should expose machine block items and general items separately for tab ordering");
        assertTrue(contentItems.contains("blockItems()") && contentItems.contains("resourceItems()")
                        && contentItems.contains("templateItems()") && contentItems.contains("upgradeItems()"),
                "Content item registry should expose upstream creative tab groups without leaking map internals");
        assertTrue(equipmentItems.contains("toolItems()") && equipmentItems.contains("bowItems()")
                        && equipmentItems.contains("armorItems()"),
                "Equipment registry should expose the upstream TOOLS/BOWS/ARMORS tab groups separately");
    }

    @Test
    void fluidDropRecipesHaveRuntimeEntityConsumer() throws IOException {
        String handler = read("src/main/java/com/zzhalex/justdirethings/common/event/FluidDropEventHandler.java");
        String proxy = read("src/main/java/com/zzhalex/justdirethings/CommonProxy.java");

        for (String token : Arrays.asList("WorldTickEvent", "EntityItem", "FluidDropDataRecipe", "setBlockState", "shrink")) {
            assertTrue(handler.contains(token), "Fluid drop runtime should mirror upstream item-in-fluid conversion: " + token);
        }
        for (String token : Arrays.asList("FluidInputs", "fluidCraftCache", "findRecipe", "clearCache", "candidateFluidPositions")) {
            assertTrue(handler.contains(token), "Fluid drop runtime should mirror upstream cached entity fluid lookup: " + token);
        }
        assertTrue(proxy.contains("FluidDropEventHandler"), "Fluid drop runtime event handler should be registered during common startup");
    }

    @Test
    void gooSoilPortsUpstreamHoeTillingEntryPoint() throws IOException {
        String handler = read("src/main/java/com/zzhalex/justdirethings/common/event/GooSoilEventHandler.java");
        String proxy = read("src/main/java/com/zzhalex/justdirethings/CommonProxy.java");

        for (String token : Arrays.asList("UseHoeEvent", "ferricore_hoe", "blazegold_hoe", "celestigem_hoe", "eclipsealloy_hoe", "GOO_SOIL_TIER4")) {
            assertTrue(handler.contains(token), "Goo soil should be produced by matching JDT hoes like upstream: " + token);
        }
        assertTrue(proxy.contains("GooSoilEventHandler"), "Goo soil hoe event handler should be registered during common startup");
    }

    @Test
    void jeiPluginPortsUpstreamGooAndFluidRecipeCategories() throws IOException {
        String plugin = read("src/main/java/com/zzhalex/justdirethings/client/jei/JDTJeiPlugin.java");

        assertTrue(plugin.contains("@JEIPlugin") && plugin.contains("IModPlugin"),
                "JEI integration should be a real 1.12 JEI plugin");
        assertTrue(plugin.contains("GooSpreadRecipeCategory") && plugin.contains("GooSpreadTagRecipeCategory") && plugin.contains("FluidDropRecipeCategory"),
                "JEI plugin should register all upstream goo/fluid categories");
        assertTrue(plugin.contains("addRecipeCatalyst") && plugin.contains("GOO_SPREAD_UID") && plugin.contains("FLUID_DROP_UID"),
                "JEI plugin should register goo catalysts and stable category UIDs");

        for (String path : Arrays.asList(
                "src/main/java/com/zzhalex/justdirethings/client/jei/GooSpreadRecipeCategory.java",
                "src/main/java/com/zzhalex/justdirethings/client/jei/GooSpreadTagRecipeCategory.java",
                "src/main/java/com/zzhalex/justdirethings/client/jei/FluidDropRecipeCategory.java",
                "src/main/java/com/zzhalex/justdirethings/client/jei/GooSpreadRecipeWrapper.java",
                "src/main/java/com/zzhalex/justdirethings/client/jei/GooSpreadTagRecipeWrapper.java",
                "src/main/java/com/zzhalex/justdirethings/client/jei/FluidDropRecipeWrapper.java"
        )) {
            assertTrue(Files.exists(path(path)), "Missing JEI parity file: " + path);
        }
    }

    @Test
    void jeiPluginPortsUpstreamOreToResourceCategory() throws IOException {
        String plugin = read("src/main/java/com/zzhalex/justdirethings/client/jei/JDTJeiPlugin.java");
        String category = read("src/main/java/com/zzhalex/justdirethings/client/jei/OreToResourceCategory.java");
        String recipe = read("src/main/java/com/zzhalex/justdirethings/client/jei/OreToResourceRecipe.java");
        String en = read("src/main/resources/assets/justdirethings/lang/en_us.lang");
        String zh = read("src/main/resources/assets/justdirethings/lang/zh_cn.lang");

        assertTrue(plugin.contains("ORE_TO_RESOURCE_UID") && plugin.contains("new OreToResourceCategory(guiHelper)"),
                "JEI plugin should register upstream ore-to-resource category");
        assertTrue(plugin.contains("oreToResourceRecipes()") && plugin.contains("raw_ferricore_ore") && plugin.contains("raw_coal_t4_ore"),
                "JEI plugin should publish all upstream ore-to-resource display recipes");
        assertTrue(recipe.contains("implements IRecipeWrapper") && recipe.contains("getIngredients") && recipe.contains("getOreBlock") && recipe.contains("getOutput"),
                "Ore-to-resource recipes should be JEI wrappers in 1.12");
        assertTrue(category.contains("createBlankDrawable(120, 30)") && category.contains("createAnimatedDrawable")
                        && category.contains("IDrawableAnimated.StartDirection.LEFT"),
                "Ore-to-resource category should mirror upstream blank background and animated arrow");
        assertTrue(category.contains("animatedArrow.draw(minecraft, 46, 10)") && category.contains("pickaxeIcon.draw(minecraft, 50, -2)"),
                "Ore-to-resource category should mirror upstream icon positions");
        assertTrue(category.contains("items.init(0, true, 20, 10)") && category.contains("items.init(1, false, 80, 10)"),
                "Ore-to-resource category should mirror upstream ingredient positions");
        assertTrue(!category.contains("slot.draw(") && !category.contains("getSlotDrawable()"),
                "Upstream ore-to-resource category does not draw item slot frames");
        assertTrue(en.contains("justdirethings.oretoresource.title="), "Missing English ore-to-resource JEI title");
        assertTrue(zh.contains("justdirethings.oretoresource.title="), "Missing Chinese ore-to-resource JEI title");
    }

    @Test
    void fluidAssetsLocalizationAndCreativeTabArePorted() throws IOException {
        String fluids = read("src/main/java/com/zzhalex/justdirethings/registry/ModFluids.java");
        String en = read("src/main/resources/assets/justdirethings/lang/en_us.lang");
        String zh = read("src/main/resources/assets/justdirethings/lang/zh_cn.lang");

        assertTrue(en.contains("itemGroup.justdirethings="), "Creative tab needs English localization");
        assertTrue(zh.contains("itemGroup.justdirethings="), "Creative tab needs Chinese localization");
        assertTrue(fluids.contains("new ModelResourceLocation(block.getRegistryName(), \"fluid\")"),
                "Each fluid block should use its own blockstate so forge:fluid can read custom.fluid");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"polymorphic_fluid\", 0xFFFFFFFF"), "Polymorphic fluid tint should match upstream");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"portal_fluid\", 0xFF00DD00"), "Portal fluid tint should match upstream");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"time_fluid\", 0x7700FF00"), "Time fluid world tint should match upstream");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"unstable_portal_fluid\", 0xFF9400D3"), "Unstable portal fluid tint should match upstream");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"unrefined_t2_fluid\", 0xFF8B4500"), "Unrefined T2 fluid tint should match upstream");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"refined_t2_fluid\", 0xFF8B0000"), "Refined T2 fluid tint should match upstream");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"unrefined_t3_fluid\", 0xFF64D5AD"), "Unrefined T3 fluid tint should match upstream");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"refined_t3_fluid\", 0xFF40C7C7"), "Refined T3 fluid tint should match upstream");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"unrefined_t4_fluid\", 0xFF36484A"), "Unrefined T4 fluid tint should match upstream");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"refined_t4_fluid\", 0xFF1B2027"), "Refined T4 fluid tint should match upstream");
        assertTrue(fluids.contains("new JDTFluidDefinition(\"xp_fluid\", 0xFF32CD32"), "XP fluid tint should match upstream");

        for (String id : Arrays.asList(
                "polymorphic_fluid",
                "portal_fluid",
                "time_fluid",
                "unstable_portal_fluid",
                "unrefined_t2_fluid",
                "refined_t2_fluid",
                "unrefined_t3_fluid",
                "refined_t3_fluid",
                "unrefined_t4_fluid",
                "refined_t4_fluid",
                "xp_fluid"
        )) {
            String blockstate = read("src/main/resources/assets/justdirethings/blockstates/" + id + "_block.json");
            assertTrue(blockstate.contains("\"model\": \"forge:fluid\"") && blockstate.contains("\"fluid\": \"" + id + "\""),
                    "Fluid blockstate should bind forge:fluid to the registered fluid: " + id);
            assertTrue(en.contains("fluid." + id + "="), "Missing English fluid name: " + id);
            assertTrue(zh.contains("fluid." + id + "="), "Missing Chinese fluid name: " + id);
            assertTrue(en.contains("tile.justdirethings." + id + "_block.name="), "Missing English fluid block name: " + id);
            assertTrue(zh.contains("tile.justdirethings." + id + "_block.name="), "Missing Chinese fluid block name: " + id);
            assertTrue(en.contains("item.forge.bucketFilled." + id + "="), "Missing English universal bucket name: " + id);
            assertTrue(zh.contains("item.forge.bucketFilled." + id + "="), "Missing Chinese universal bucket name: " + id);
        }
    }

    @Test
    void jeiCategoriesUseUpstreamIngredientCoordinatesWithoutLegacySlotFrames() throws IOException {
        String goo = read("src/main/java/com/zzhalex/justdirethings/client/jei/GooSpreadRecipeCategory.java");
        String gooTag = read("src/main/java/com/zzhalex/justdirethings/client/jei/GooSpreadTagRecipeCategory.java");
        String fluid = read("src/main/java/com/zzhalex/justdirethings/client/jei/FluidDropRecipeCategory.java");

        for (String category : Arrays.asList(goo, gooTag)) {
            assertTrue(!category.contains("slot.draw(") && !category.contains("getSlotDrawable()"),
                    "Upstream JEI goo categories do not draw item slot frames.");
            assertTrue(category.contains("background.draw(minecraft, 17, 0)"),
                    "Upstream JEI goo categories draw only the arrow plus blank background offset.");
            assertTrue(category.contains("items.init(0, true, 9, 12)")
                            && category.contains("items.init(1, true, 29, 12)")
                            && category.contains("items.init(2, false, 88, 12)"),
                    "JEI ingredients should keep upstream literal coordinates.");
        }
        assertTrue(!fluid.contains("slot.draw(") && !fluid.contains("getSlotDrawable()"),
                "Upstream JEI fluid drop category does not draw item slot frames.");
        assertTrue(fluid.contains("background.draw(minecraft, 17, 0)"),
                "Upstream JEI fluid drop category draws only the arrow plus blank background offset.");
        assertTrue(fluid.contains("items.init(0, true, 9, 0)")
                        && fluid.contains("fluids.init(0, true, 9, 20")
                        && (fluid.contains("items.init(1, false, 68, 20)") || fluid.contains("fluids.init(1, false, 68, 20")),
                "Fluid drop JEI ingredients should keep upstream literal coordinates.");
    }

    @Test
    void gooAssetsExposeAlivePatternAndMoistureVariants() throws IOException {
        String gooBlock = read("src/main/resources/assets/justdirethings/blockstates/gooblock_tier1.json");
        String pattern = read("src/main/resources/assets/justdirethings/blockstates/goopatternblock.json");
        String soil = read("src/main/resources/assets/justdirethings/blockstates/goosoil_tier1.json");
        String patternParent = read("src/main/resources/assets/justdirethings/models/block/goopattern_orientable_with_bottom.json");
        String en = read("src/main/resources/assets/justdirethings/lang/en_us.lang");
        String zh = read("src/main/resources/assets/justdirethings/lang/zh_cn.lang");

        assertTrue(gooBlock.contains("alive=false") && gooBlock.contains("gooblock_tier1_dead"),
                "Goo blockstate should expose dead/alive variants like upstream");
        assertTrue(pattern.contains("goostage=0") && pattern.contains("goostage=11"),
                "Goo pattern blockstate should expose all 12 upstream render stages");
        assertTrue(patternParent.contains("\"down\"") && patternParent.contains("\"#bottom\"")
                        && patternParent.contains("\"north\"") && patternParent.contains("\"#front\"")
                        && patternParent.contains("\"up\"") && patternParent.contains("\"#top\""),
                "1.12 needs a local orientable_with_bottom parent so goo pattern masks keep upstream face semantics");
        for (int stage = 0; stage <= 11; stage++) {
            String model = read("src/main/resources/assets/justdirethings/models/block/goopatternblock" + stage + ".json");
            assertTrue(!model.contains("cube_bottom_top") && model.contains("goopattern_orientable_with_bottom"),
                    "Goo pattern stage should not use the legacy opaque cube_bottom_top parent: " + stage);
            assertTrue(model.contains("\"bottom\": \"justdirethings:block/goopatterns/goorender_full\""),
                    "Goo pattern bottom should be full mask like upstream: " + stage);
            if (stage >= 10) {
                assertTrue(model.contains("\"front\": \"justdirethings:block/goopatterns/goorender_full\"")
                                && model.contains("\"side\": \"justdirethings:block/goopatterns/goorender_full\"")
                                && model.contains("\"top\": \"justdirethings:block/goopatterns/goorender_full\""),
                        "Final goo pattern stages should use the full mask on all faces: " + stage);
            } else if (stage == 9) {
                assertTrue(model.contains("\"front\": \"justdirethings:block/goopatterns/goorender_full\"")
                                && model.contains("\"side\": \"justdirethings:block/goopatterns/goorender_full\"")
                                && model.contains("\"top\": \"justdirethings:block/goopatterns/goopatterblock_top\""),
                        "Goo pattern stage 9 should use full mask on sides with patterned top like upstream: " + stage);
            } else {
                assertTrue(model.contains("\"front\": \"justdirethings:block/goopatterns/goorender_side" + stage + "\"")
                                && model.contains("\"side\": \"justdirethings:block/goopatterns/goorender_side" + stage + "\"")
                                && model.contains("\"top\": \"justdirethings:block/goopatterns/goorender_blank\""),
                        "Partial goo pattern stages should use side/front vines with a blank top like upstream: " + stage);
            }
        }
        assertTrue(soil.contains("moisture=0") && soil.contains("moisture=7") && soil.contains("_moist"),
                "Goo soil blockstate should retain farmland moisture variants");
        for (String key : Arrays.asList(
                "justdirethings.goospreadrecipe.title",
                "justdirethings.goospreadrecipetag.title",
                "justdirethings.fluiddroprecipe.title"
        )) {
            assertTrue(en.contains(key + "="), "Missing English JEI title: " + key);
            assertTrue(zh.contains(key + "="), "Missing Chinese JEI title: " + key);
        }
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
