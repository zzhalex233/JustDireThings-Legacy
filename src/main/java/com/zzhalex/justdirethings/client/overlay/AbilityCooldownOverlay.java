package com.zzhalex.justdirethings.client.overlay;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.ItemToggleableTool;
import com.zzhalex.justdirethings.data.tool.AbilityCooldown;
import com.zzhalex.justdirethings.data.tool.ToolState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public final class AbilityCooldownOverlay {

    public static final AbilityCooldownOverlay INSTANCE = new AbilityCooldownOverlay();

    private AbilityCooldownOverlay() {
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.player == null || minecraft.gameSettings.hideGUI) {
            return;
        }

        ItemStack stack = minecraft.player.getHeldItemMainhand();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemToggleableTool)) {
            return;
        }

        ItemToggleableTool tool = (ItemToggleableTool) stack.getItem();
        ToolState state = tool.getToolState(stack);
        AbilityCooldown cooldown = findActiveCooldown(state.getAbilityCooldowns());
        if (cooldown == null) {
            return;
        }

        String label = formatCooldownLabel(cooldown);
        ScaledResolution resolution = new ScaledResolution(minecraft);
        int x = (resolution.getScaledWidth() - minecraft.fontRenderer.getStringWidth(label)) / 2;
        int y = resolution.getScaledHeight() - 70;
        minecraft.fontRenderer.drawStringWithShadow(label, x, y, 0xFFFFFFFF);
    }

    private static AbilityCooldown findActiveCooldown(List<AbilityCooldown> cooldowns) {
        if (cooldowns == null || cooldowns.isEmpty()) {
            return null;
        }

        for (AbilityCooldown cooldown : cooldowns) {
            if (cooldown != null && cooldown.isActive() && cooldown.getRemainingTicks() > 0) {
                return cooldown;
            }
        }
        return null;
    }

    private static String formatCooldownLabel(AbilityCooldown cooldown) {
        Ability ability = Ability.byId(cooldown.getAbilityId());
        String name = ability == null ? cooldown.getAbilityId() : I18n.format(ability.getTranslationKey());
        return String.format(Locale.ROOT, "%s: %.1fs", name, cooldown.getRemainingTicks() / 20.0F);
    }
}
