package com.zzhalex.justdirethings.common.entity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreatureCatcherParitySourceTest {

    @Test
    void releaseUsesProjectilePositionWithoutOverwritingCapturedEntityRotation() throws IOException {
        String source = read("src/main/java/com/zzhalex/justdirethings/common/entity/EntityCreatureCatcher.java");

        assertTrue(source.contains("snapToImpact(result);"), "1.12 impact handling must snap the projectile to the exact hit vector before release.");
        assertTrue(source.contains("result.hitVec"), "Release/capture animation should use the actual ray trace hit location, not the previous tick projectile position.");
        assertTrue(source.contains("getPositionVector()"), "Release should mirror upstream by moving to the projectile position vector.");
        assertFalse(
                source.contains("setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch)"),
                "Release must not overwrite the captured mob's saved yaw/pitch with the catcher's projectile rotation."
        );
    }

    @Test
    void creatureCatcherItemUsesCustomItemRendererAndBakedModelWrapperForCapturedEntityPreview() throws IOException {
        String clientRegistration = read("src/main/java/com/zzhalex/justdirethings/client/ClientRegistration.java");
        String itemRegistration = read("src/main/java/com/zzhalex/justdirethings/registry/ModItems.java");
        String bakeHandler = read("src/main/java/com/zzhalex/justdirethings/client/render/CreatureCatcherModelBakeHandler.java");
        String bakedModel = read("src/main/java/com/zzhalex/justdirethings/client/render/CreatureCatcherBuiltinModel.java");
        String renderer = read("src/main/java/com/zzhalex/justdirethings/client/render/RenderCreatureCatcherItemStack.java");

        assertTrue(clientRegistration.contains("RenderCreatureCatcherItemStack"), "Client setup should install the item stack renderer.");
        assertTrue(clientRegistration.contains("setTileEntityItemStackRenderer"), "Creature Catcher needs a TEISR on 1.12.");
        assertTrue(clientRegistration.contains("CreatureCatcherModelBakeHandler"), "The baked model must be replaced on ModelBakeEvent.");
        assertTrue(itemRegistration.contains("registerItemVariants(CREATURE_CATCHER"), "The base model must be baked for the custom renderer.");
        assertTrue(bakeHandler.contains("ModelBakeEvent"), "ModelBakeEvent should install the built-in renderer wrapper.");
        assertTrue(bakedModel.contains("isBuiltInRenderer()") && bakedModel.contains("return true;"), "The wrapper must force the TEISR render path.");
        assertTrue(renderer.contains("public void renderByItem(ItemStack stack)"), "Override the exact method RenderItem calls in 1.12.");
    }

    @Test
    void capturedEntityPreviewClearsTransientHurtStateSoItDoesNotRenderRed() throws IOException {
        String entitySource = read("src/main/java/com/zzhalex/justdirethings/common/entity/EntityCreatureCatcher.java");
        String itemRenderer = read("src/main/java/com/zzhalex/justdirethings/client/render/RenderCreatureCatcherItemStack.java");
        String projectileRenderer = read("src/main/java/com/zzhalex/justdirethings/client/render/RenderCreatureCatcher.java");

        assertTrue(entitySource.contains("sanitizeCapturedEntityData(entityData)"), "Stored catcher NBT should drop transient hurt/death render state.");
        assertTrue(entitySource.contains("\"HurtTime\""), "HurtTime is what causes RenderLivingBase's red overlay.");
        assertTrue(entitySource.contains("resetCapturedEntityVisualState(entity)"), "Entities created from catcher NBT need transient render state reset.");
        assertTrue(itemRenderer.contains("resetCapturedEntityVisualState(living)"), "Item preview renderer must reset red hurt overlay state.");
        assertTrue(projectileRenderer.contains("resetCapturedEntityVisualState(living)"), "Capture/release animation renderer must reset red hurt overlay state.");
    }

    @Test
    void capturedEntityPreviewSplitsOriginalModelIntoEquivalentRenderPasses() throws IOException {
        String itemRegistration = read("src/main/java/com/zzhalex/justdirethings/registry/ModItems.java");
        String itemRenderer = read("src/main/java/com/zzhalex/justdirethings/client/render/RenderCreatureCatcherItemStack.java");
        String bottomModel = read("src/main/resources/assets/justdirethings/models/item/creaturecatcher_bottom.json");
        String shieldModel = read("src/main/resources/assets/justdirethings/models/item/creaturecatcher_shield.json");

        assertTrue(itemRegistration.contains("creaturecatcher_shield"), "The transparent shell must be baked separately for captured stacks.");
        assertTrue(itemRegistration.contains("creaturecatcher_bottom"), "1.12 needs a bottom-only pass to emulate upstream's buffered render ordering.");
        assertTrue(itemRenderer.contains("BOTTOM_MODEL"), "Captured stacks should draw the solid base separately.");
        assertTrue(itemRenderer.indexOf("renderBottomModel(stack);") < itemRenderer.indexOf("renderCapturedEntity(stack, partialTicks);"), "The solid base pass must draw before the captured entity.");
        assertTrue(itemRenderer.indexOf("renderCapturedEntity(stack, partialTicks);") < itemRenderer.indexOf("renderShieldModel(stack);"), "The transparent shell pass must draw after the captured entity.");
        assertFalse(itemRenderer.contains("ensureStencilAvailable()"), "Stencil masking hides the captured mob behind the shell in item rendering.");
        assertFalse(itemRenderer.contains("glStencil"), "Stencil state is too fragile for this TEISR path.");
        assertFalse(itemRenderer.contains("GlStateManager.disableDepth()"), "Depth bypass makes the mob overwrite item faces; split passes avoid needing it.");
        assertTrue(itemRenderer.contains("GlStateManager.disableCull()"), "Captured mobs need culling disabled so the view-facing skin layer does not disappear.");
        assertTrue(itemRenderer.contains("GlStateManager.enableCull()"), "The transparent shell pass should cull inner/back faces instead of drawing a second membrane.");
        assertTrue(itemRenderer.contains("GlStateManager.depthMask(false)"), "The transparent shell should not write over entity depth.");
        assertTrue(itemRenderer.contains("GlStateManager.disableLighting()"), "Shield must render without lighting so texture alpha is not clamped by the fixed-function material pipeline.");
        assertTrue(itemRenderer.contains("renderModelDirectly(model)"), "Shield bypasses RenderItem to render quads directly with correct texture alpha.");
        assertTrue(itemRenderer.contains("TextureMap.LOCATION_BLOCKS_TEXTURE"), "Shield pass must explicitly bind the block atlas before direct quad rendering.");
        assertTrue(itemRenderer.contains("glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)"), "All GL state must be saved via glPushAttrib so entity rendering cannot leak any state to the main pipeline.");
        assertTrue(itemRenderer.contains("glPopAttrib()"), "glPopAttrib restores every GL attribute the entity renderer could have dirtied.");
        assertTrue(itemRenderer.contains("syncGlStateManagerCache()"), "After glPopAttrib the GlStateManager cache is stale and must be force-synced with actual GL state.");
        assertTrue(bottomModel.contains("\"to\": [16, 2, 16]"), "The bottom pass should contain only the base slab.");
        assertFalse(bottomModel.contains("\"to\": [15, 16, 15]"), "The bottom pass must not contain the shell.");
        assertTrue(shieldModel.contains("\"to\": [15, 16, 15]"), "The shield model supplies the transparent container shell.");
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }
}
