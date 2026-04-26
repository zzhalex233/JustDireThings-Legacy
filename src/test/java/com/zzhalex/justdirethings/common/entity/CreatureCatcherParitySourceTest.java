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
    void capturedEntityPreviewUsesDepthBypassWithoutAddingAnExtraShieldOverlay() throws IOException {
        String itemRegistration = read("src/main/java/com/zzhalex/justdirethings/registry/ModItems.java");
        String itemRenderer = read("src/main/java/com/zzhalex/justdirethings/client/render/RenderCreatureCatcherItemStack.java");

        assertFalse(itemRegistration.contains("creaturecatcher_bottom"), "Do not bake a split bottom model just for the catcher preview.");
        assertFalse(itemRegistration.contains("creaturecatcher_shield"), "A second shield pass creates a visible outer film around the item.");
        assertFalse(itemRenderer.contains("BOTTOM_MODEL"), "Captured stacks should use the same full catcher model as upstream.");
        assertFalse(itemRenderer.contains("SHIELD_MODEL"), "The renderer must not add an extra transparent shield overlay.");
        assertFalse(itemRenderer.contains("renderShieldModel"), "The shield overlay was the source of the membrane artifact.");
        assertTrue(itemRenderer.indexOf("renderBaseModel(stack);") < itemRenderer.indexOf("renderCapturedEntity(stack, partialTicks);"), "The base must render before the entity.");
        assertTrue(itemRenderer.contains("GL11.glIsEnabled(GL11.GL_DEPTH_TEST)"), "Capture preview should restore the previous depth state after rendering.");
        assertTrue(itemRenderer.contains("GlStateManager.disableDepth()"), "The entity preview needs a depth bypass so the full catcher model cannot hide it.");
        assertTrue(itemRenderer.contains("restoreDepthState"), "Depth state restoration must be explicit to avoid leaking render state.");
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }
}
