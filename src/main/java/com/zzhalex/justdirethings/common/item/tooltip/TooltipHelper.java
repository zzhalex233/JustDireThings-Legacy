package com.zzhalex.justdirethings.common.item.tooltip;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.EnergyBackedItem;
import com.zzhalex.justdirethings.common.item.base.BoundInventoryHelper;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public final class TooltipHelper {

    private static final NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance(Locale.ROOT);
    private static final DecimalFormat SUFFIX_FORMAT = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private TooltipHelper() {
    }

    public static void appendFEText(ItemStack stack, List<String> tooltip) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof EnergyBackedItem)) {
            return;
        }

        EnergyBackedItem item = (EnergyBackedItem) stack.getItem();
        appendFEText(tooltip, item.getStoredEnergy(stack), item.getEnergyCapacity(stack));
    }

    public static void appendFEText(List<String> tooltip, int storedEnergy, int maxEnergy) {
        if (maxEnergy <= 0) {
            return;
        }

        tooltip.add(TextFormatting.GREEN + I18n.format(
                "justdirethings.festored",
                formatTooltipValue(storedEnergy),
                formatTooltipValue(maxEnergy)
        ));
    }

    public static void appendToolEnabled(ItemStack stack, List<String> tooltip) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return;
        }

        appendToolEnabled(((ToggleableTool) stack.getItem()).isEnabled(stack), tooltip);
    }

    public static void appendToolEnabled(boolean enabled, List<String> tooltip) {
        String stateKey = enabled ? "justdirethings.enabled" : "justdirethings.disabled";
        TextFormatting stateColor = enabled ? TextFormatting.GREEN : TextFormatting.DARK_RED;
        tooltip.add(stateColor
                + I18n.format(stateKey)
                + TextFormatting.DARK_GRAY
                + " "
                + I18n.format("justdirethings.presshotkey", I18n.format("justdirethings.gui.tool_settings")));
    }

    public static void appendAbilityList(ItemStack stack, List<String> tooltip) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return;
        }

        ToggleableTool tool = (ToggleableTool) stack.getItem();
        for (Ability ability : tool.getSupportedAbilities()) {
            boolean installed = tool.hasInstalledAbility(stack, ability);
            if (!installed && ability.requiresUpgrade()) {
                tooltip.add(TextFormatting.GRAY
                        + abilityName(ability)
                        + I18n.format("justdirethings.missingupgrade"));
                continue;
            }

            TextFormatting color = tool.getSetting(stack, ability) ? TextFormatting.GREEN : TextFormatting.DARK_RED;
            tooltip.add(color + abilityName(ability));
            appendAbilityDetails(stack, ability, tooltip);
        }
    }

    public static void appendShiftForInfo(ItemStack stack, List<String> tooltip) {
        tooltip.add(TextFormatting.GRAY + I18n.format("justdirethings.shiftmoreinfo"));
    }

    public static void appendUpgradeDetails(String abilityId, List<String> tooltip) {
        Ability ability = Ability.byId(abilityId);
        if (ability == null) {
            return;
        }

        appendIfTranslated(tooltip, "justdirethings." + ability.getId() + ".detailtext", TextFormatting.GREEN);
        appendIfTranslated(tooltip, "justdirethings." + ability.getId() + ".flavortext", TextFormatting.GRAY.toString() + TextFormatting.ITALIC);
    }

    public static void appendGeneratorDetails(ItemStack stack, PocketGeneratorItem item, List<String> tooltip) {
        ItemStack fuelStack = item.getFuelStack(stack);
        if (GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.DARK_RED + I18n.format(
                    "justdirethings.pocketgeneratorburntime",
                    formatNumber(item.getCounter(stack)),
                    formatNumber(item.getMaxBurn(stack))
            ));
            if (fuelStack.isEmpty()) {
                tooltip.add(TextFormatting.RED + I18n.format("justdirethings.pocketgeneratornofuel"));
            } else {
                tooltip.add(TextFormatting.DARK_AQUA + I18n.format(
                        "justdirethings.pocketgeneratorfuelstack",
                        formatNumber(fuelStack.getCount()),
                        fuelStack.getDisplayName()
                ));
            }
        } else if (fuelStack.isEmpty()) {
            tooltip.add(TextFormatting.RED + I18n.format("justdirethings.pocketgeneratornofuel"));
        }
    }

    public static String abilityIdFromUpgradeItem(Item item) {
        if (item == null || item.getRegistryName() == null) {
            return "";
        }
        String path = item.getRegistryName().getPath();
        if (!path.startsWith("upgrade_") || "upgrade_blank".equals(path)) {
            return "";
        }
        return path.substring("upgrade_".length()).replace("_", "");
    }

    public static String formatNumber(double value) {
        if (Math.rint(value) == value) {
            return INTEGER_FORMAT.format((long) value);
        }
        return SUFFIX_FORMAT.format(value);
    }

    public static String formatTooltipValue(int value) {
        return GuiScreen.isShiftKeyDown() ? formatNumber(value) : formatCompact(value);
    }

    public static String formatCompact(int value) {
        int abs = Math.abs(value);
        if (abs >= 1_000_000) {
            return SUFFIX_FORMAT.format(value / 1_000_000.0D) + "M";
        }
        if (abs >= 1_000) {
            return SUFFIX_FORMAT.format(value / 1_000.0D) + "K";
        }
        return formatNumber(value);
    }

    private static void appendAbilityDetails(ItemStack stack, Ability ability, List<String> tooltip) {
        if (ability != Ability.DROPTELEPORT) {
            return;
        }

        BoundInventoryHelper.BoundLocation boundLocation = BoundInventoryHelper.getBoundTo(stack);
        if (boundLocation == null) {
            tooltip.add(TextFormatting.DARK_PURPLE + I18n.format("justdirethings.unbound"));
            return;
        }

        tooltip.add(TextFormatting.DARK_PURPLE + I18n.format(
                "justdirethings.boundto",
                boundLocation.getDimensionName(),
                "[" + boundLocation.toShortString() + "]"
        ));
        EnumFacing side = boundLocation.getSide();
        if (side != null) {
            tooltip.add(TextFormatting.DARK_PURPLE
                    + I18n.format("justdirethings.boundside")
                    + I18n.format("justdirethings.screen.direction-" + side.getName()));
        }
    }

    private static void appendIfTranslated(List<String> tooltip, String key, Object colorPrefix) {
        if (!I18n.hasKey(key)) {
            return;
        }
        tooltip.add(colorPrefix.toString() + I18n.format(key));
    }

    private static String abilityName(Ability ability) {
        return I18n.format(ability.getTranslationKey());
    }
}
