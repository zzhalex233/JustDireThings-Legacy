package com.zzhalex.justdirethings.client.overlay;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.data.tool.AbilityCooldown;
import com.zzhalex.justdirethings.data.tool.ToolState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public final class AbilityCooldownOverlay extends Gui {

    public static final AbilityCooldownOverlay INSTANCE = new AbilityCooldownOverlay();
    private static final EntityEquipmentSlot[] EQUIPMENT_ORDER = {
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET,
            EntityEquipmentSlot.MAINHAND,
            EntityEquipmentSlot.OFFHAND
    };
    private static final int OVERLAY_X = 91;
    private static final int ICON_SIZE = 9;
    private static final int ICON_SPACING = 11;
    private static final int ICONS_PER_ROW = 7;
    private static final int ACTIVE_TRACK_HEIGHT = 18;

    private AbilityCooldownOverlay() {
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ARMOR) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.player == null || minecraft.gameSettings.hideGUI) {
            return;
        }

        ScaledResolution resolution = new ScaledResolution(minecraft);
        int renderedIcons = 0;
        for (EntityEquipmentSlot slot : EQUIPMENT_ORDER) {
            ItemStack stack = minecraft.player.getItemStackFromSlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
                continue;
            }

            ToggleableTool tool = (ToggleableTool) stack.getItem();
            ToolState state = tool.getToolState(stack);
            List<AbilityCooldown> cooldowns = state.getAbilityCooldowns();
            if (cooldowns == null || cooldowns.isEmpty()) {
                continue;
            }

            for (AbilityCooldown cooldown : cooldowns) {
                if (cooldown == null || cooldown.getRemainingTicks() <= 0) {
                    continue;
                }
                Ability ability = Ability.byId(cooldown.getAbilityId());
                if (ability == null) {
                    continue;
                }
                int row = renderedIcons / ICONS_PER_ROW;
                int x = resolution.getScaledWidth() / 2 - OVERLAY_X + ((renderedIcons % ICONS_PER_ROW) * ICON_SPACING);
                int y = resolution.getScaledHeight() - GuiIngameForge.left_height - ICON_SIZE - (row * ICON_SPACING);
                renderCooldownIcon(minecraft, tool, ability, cooldown, x, y);
                renderedIcons++;
            }
        }

        if (renderedIcons > 0) {
            int rows = ((renderedIcons - 1) / ICONS_PER_ROW) + 1;
            GuiIngameForge.left_height += ACTIVE_TRACK_HEIGHT + ((rows - 1) * ICON_SPACING);
        }
    }

    private void renderCooldownIcon(
            Minecraft minecraft,
            ToggleableTool tool,
            Ability ability,
            AbilityCooldown cooldown,
            int x,
            int y
    ) {
        AbilityParams params = tool.getAbilityParams(ability);
        int maxTicks = cooldown.isActive() ? params.activeCooldown : params.cooldown;
        if (maxTicks <= 0) {
            maxTicks = cooldown.getRemainingTicks();
        }

        int iconHeight = cooldown.isActive()
                ? ((cooldown.getRemainingTicks() * 8) / maxTicks) + 1
                : 9 - ((cooldown.getRemainingTicks() * 9) / maxTicks);
        iconHeight = Math.max(0, Math.min(ICON_SIZE, iconHeight));
        int blitY = y + (ACTIVE_TRACK_HEIGHT - iconHeight);
        int textureY = ICON_SIZE - iconHeight;

        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        minecraft.getTextureManager().bindTexture(ability.getCooldownIcon());
        if (cooldown.isActive()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            GlStateManager.color(1.0F, 0.5F, 0.5F, 1.0F);
        }
        if (iconHeight > 0) {
            drawModalRectWithCustomSizedTexture(x, blitY, 0, textureY, ICON_SIZE, iconHeight, ICON_SIZE, ICON_SIZE);
        }
        if (cooldown.isActive()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.25F);
        } else {
            GlStateManager.color(1.0F, 0.5F, 0.5F, 0.25F);
        }
        drawModalRectWithCustomSizedTexture(x, y + ICON_SIZE, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(ICONS);
    }
}
