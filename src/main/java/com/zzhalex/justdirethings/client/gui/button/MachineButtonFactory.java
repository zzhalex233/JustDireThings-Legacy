package com.zzhalex.justdirethings.client.gui.button;

import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MachineButtonFactory {

    private static final String BUTTON_ROOT = "textures/gui/buttons/";

    private static final List<ButtonDefinition.State> REDSTONE_STATES = states(
            state("redstoneignore.png", "justdirethings.screen.ignored"),
            state("redstonelow.png", "justdirethings.screen.low"),
            state("redstonehigh.png", "justdirethings.screen.high"),
            state("redstonepulse.png", "justdirethings.screen.pulse")
    );

    private static final List<ButtonDefinition.State> DIRECTION_STATES = states(
            state("direction-down.png", "justdirethings.screen.direction-down"),
            state("direction-up.png", "justdirethings.screen.direction-up"),
            state("direction-north.png", "justdirethings.screen.direction-north"),
            state("direction-south.png", "justdirethings.screen.direction-south"),
            state("direction-west.png", "justdirethings.screen.direction-west"),
            state("direction-east.png", "justdirethings.screen.direction-east")
    );

    private static final List<ButtonDefinition.State> ALLOW_LIST_STATES = states(
            state("allowlistfalse.png", "justdirethings.screen.denylist"),
            state("allowlisttrue.png", "justdirethings.screen.allowlist")
    );

    private static final List<ButtonDefinition.State> BLOCK_ITEM_FILTER_STATES = states(
            state("filter-block.png", "justdirethings.screen.filter-block"),
            state("filter-item.png", "justdirethings.screen.filter-item")
    );

    private static final List<ButtonDefinition.State> LEFT_RIGHT_CLICK_STATES = states(
            state("click-right.png", "justdirethings.screen.click-right"),
            state("click-left.png", "justdirethings.screen.click-left"),
            state("click-hold.png", "justdirethings.screen.click-hold")
    );

    private static final List<ButtonDefinition.State> CLICK_TARGET_STATES = states(
            state("filter-block.png", "justdirethings.screen.target-block"),
            state("filter-air.png", "justdirethings.screen.target-air"),
            state("mobscanner.png", "justdirethings.screen.target-hostile"),
            state("passivemob.png", "justdirethings.screen.target-passive"),
            state("passivemob-adult.png", "justdirethings.screen.target-adult"),
            state("passivemob-child.png", "justdirethings.screen.target-child"),
            state("player.png", "justdirethings.screen.target-player"),
            state("glowing.png", "justdirethings.screen.target-living")
    );

    private static final List<ButtonDefinition.State> SENSOR_TARGET_STATES = states(
            state("filter-block.png", "justdirethings.screen.target-block"),
            state("filter-air.png", "justdirethings.screen.target-air"),
            state("mobscanner.png", "justdirethings.screen.target-hostile"),
            state("passivemob.png", "justdirethings.screen.target-passive"),
            state("passivemob-adult.png", "justdirethings.screen.target-adult"),
            state("passivemob-child.png", "justdirethings.screen.target-child"),
            state("player.png", "justdirethings.screen.target-player"),
            state("glowing.png", "justdirethings.screen.target-living"),
            state("item.png", "justdirethings.screen.target-item")
    );

    private static final List<ButtonDefinition.State> STRONG_WEAK_STATES = states(
            state("redstonelow.png", "justdirethings.screen.redstone-weak"),
            state("redstonehigh.png", "justdirethings.screen.redstone-strong")
    );

    private static final List<ButtonDefinition.State> EQUALITY_STATES = states(
            state("greaterthan.png", "justdirethings.screen.greaterthan"),
            state("lessthan.png", "justdirethings.screen.lessthan"),
            state("equals.png", "justdirethings.screen.equals")
    );

    private static final List<ButtonDefinition.State> SWAPPER_ENTITY_STATES = states(
            state("entity-none.png", "justdirethings.screen.entity-none"),
            state("mobscanner.png", "justdirethings.screen.target-hostile"),
            state("passivemob.png", "justdirethings.screen.target-passive"),
            state("passivemob-adult.png", "justdirethings.screen.target-adult"),
            state("passivemob-child.png", "justdirethings.screen.target-child"),
            state("player.png", "justdirethings.screen.target-player"),
            state("glowing.png", "justdirethings.screen.target-living"),
            state("item.png", "justdirethings.screen.target-item"),
            state("entity-all.png", "justdirethings.screen.entity-all")
    );

    private static final List<ButtonDefinition.State> SWAPPER_BLOCK_STATES = states(
            state("filter-block.png", "justdirethings.screen.target-block"),
            state("filter-air.png", "justdirethings.screen.target-noblock")
    );

    private static final List<ButtonDefinition.State> INVENTORY_CONNECTION_STATES = states(
            state("inv-normal.png", "justdirethings.screen.inv-normal"),
            state("inv-armor.png", "justdirethings.screen.inv-armor"),
            state("inv-offhand.png", "justdirethings.screen.inv-offhand")
    );

    private MachineButtonFactory() {
    }

    public static List<ButtonDefinition.State> redstoneStates() {
        return REDSTONE_STATES;
    }

    public static List<ButtonDefinition.State> directionStates() {
        return DIRECTION_STATES;
    }

    public static List<ButtonDefinition> baseMachineButtons(int tickSpeed, int redstoneMode, int direction) {
        List<ButtonDefinition> buttons = new ArrayList<>();
        buttons.add(tickSpeedButton(tickSpeed));
        buttons.add(redstoneButton(134, 62, redstoneMode));
        buttons.add(directionButton(152, 40, direction));
        return Collections.unmodifiableList(buttons);
    }

    public static List<ButtonDefinition> wideTimedMachineButtons(int tickSpeed, int redstoneMode) {
        List<ButtonDefinition> buttons = new ArrayList<>();
        buttons.add(tickSpeedButton(tickSpeed));
        buttons.add(redstoneButton(134, 62, redstoneMode));
        return Collections.unmodifiableList(buttons);
    }

    public static List<ButtonDefinition> compactTimedMachineButtons(int tickSpeed, int redstoneMode) {
        List<ButtonDefinition> buttons = new ArrayList<>();
        buttons.add(tickSpeedButton(tickSpeed));
        buttons.add(redstoneButton(104, 38, redstoneMode));
        return Collections.unmodifiableList(buttons);
    }

    public static List<ButtonDefinition> compactTimedDirectionalMachineButtons(int tickSpeed, int redstoneMode, int direction) {
        List<ButtonDefinition> buttons = new ArrayList<>();
        buttons.add(tickSpeedButton(tickSpeed));
        buttons.add(redstoneButton(104, 38, redstoneMode));
        buttons.add(directionButton(122, 38, direction));
        return Collections.unmodifiableList(buttons);
    }

    public static ButtonDefinition tickSpeedButton(int tickSpeed) {
        return ButtonDefinition.number(144, 40, MachineSettingKeys.TICK_SPEED, tickSpeed, 1, 1200, "justdirethings.screen.tickspeed");
    }

    public static ButtonDefinition redstoneButton(int x, int y, int redstoneMode) {
        return ButtonDefinition.toggle(x, y, MachineSettingKeys.REDSTONE_MODE, redstoneMode, REDSTONE_STATES);
    }

    public static ButtonDefinition directionButton(int x, int y, int direction) {
        return ButtonDefinition.toggle(x, y, MachineSettingKeys.DIRECTION, direction, DIRECTION_STATES);
    }

    public static List<ButtonDefinition> filterButtons(boolean allowList, boolean compareNbt, int blockItemFilter) {
        List<ButtonDefinition> buttons = new ArrayList<>();
        buttons.add(ButtonDefinition.toggle(8, 62, MachineSettingKeys.FILTER_ALLOWLIST, allowList ? 1 : 0, ALLOW_LIST_STATES));
        buttons.add(ButtonDefinition.grayscale(26, 62, MachineSettingKeys.FILTER_COMPARE_NBT, compareNbt,
                state("matchnbttrue.png", "justdirethings.screen.comparenbt")));
        if (blockItemFilter != -1) {
            buttons.add(ButtonDefinition.toggle(44, 62, MachineSettingKeys.FILTER_BLOCK_ITEM, blockItemFilter, BLOCK_ITEM_FILTER_STATES));
        }
        return Collections.unmodifiableList(buttons);
    }

    public static List<ButtonDefinition> areaButtons(boolean renderArea, double xRadius, double yRadius, double zRadius, int xOffset, int yOffset, int zOffset) {
        List<ButtonDefinition> buttons = new ArrayList<>();
        buttons.add(ButtonDefinition.grayscale(152, 62, MachineSettingKeys.RENDER_AREA, renderArea,
                state("area.png", "justdirethings.screen.renderarea")));
        buttons.addAll(areaValueButtons(25, 12, MachineSettingKeys.X_RADIUS_TENTHS, (int) Math.round(xRadius * 10.0D), 0, 50, 5, "justdirethings.screen.radiusx"));
        buttons.addAll(areaValueButtons(75, 12, MachineSettingKeys.Y_RADIUS_TENTHS, (int) Math.round(yRadius * 10.0D), 0, 50, 5, "justdirethings.screen.radiusy"));
        buttons.addAll(areaValueButtons(125, 12, MachineSettingKeys.Z_RADIUS_TENTHS, (int) Math.round(zRadius * 10.0D), 0, 50, 5, "justdirethings.screen.radiusz"));
        buttons.addAll(areaValueButtons(25, 27, MachineSettingKeys.X_OFFSET, xOffset, -9, 9, 1, "justdirethings.screen.offsetx"));
        buttons.addAll(areaValueButtons(75, 27, MachineSettingKeys.Y_OFFSET, yOffset, -9, 9, 1, "justdirethings.screen.offsety"));
        buttons.addAll(areaValueButtons(125, 27, MachineSettingKeys.Z_OFFSET, zOffset, -9, 9, 1, "justdirethings.screen.offsetz"));
        return Collections.unmodifiableList(buttons);
    }

    public static List<ButtonDefinition> areaValueButtons(int x, int y, String key, int value, int min, int max, int step, String localizationKey) {
        List<ButtonDefinition> buttons = new ArrayList<>();
        buttons.add(ButtonDefinition.valueAdjust(x, y, key, value, min, max, -step,
                state("remove.png", localizationKey)));
        buttons.add(ButtonDefinition.valueAdjust(x + 30, y, key, value, min, max, step,
                state("add.png", localizationKey)));
        return Collections.unmodifiableList(buttons);
    }

    public static ButtonDefinition respectPickupDelayButton(boolean respectPickupDelay) {
        return ButtonDefinition.grayscale(116, 62, MachineSettingKeys.RESPECT_PICKUP_DELAY, respectPickupDelay,
                state("jumpboost.png", "justdirethings.screen.respectpickupdelay"));
    }

    public static ButtonDefinition showParticlesButton(int x, int y, boolean showParticles) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.SHOW_PARTICLES, showParticles,
                state("showfakeplayer.png", "justdirethings.screen.showparticles"));
    }

    public static ButtonDefinition sneakClickButton(int x, int y, boolean sneaking) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.SNEAKING, sneaking,
                state("click-sneak.png", "justdirethings.screen.sneak-click"));
    }

    public static ButtonDefinition showFakePlayerButton(int x, int y, boolean showFakePlayer) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.SHOW_FAKE_PLAYER, showFakePlayer,
                state("showfakeplayer.png", "justdirethings.screen.showfakeplayer"));
    }

    public static ButtonDefinition pickupDelayButton(int x, int y, int pickupDelay) {
        return ButtonDefinition.number(x, y, MachineSettingKeys.PICKUP_DELAY, pickupDelay, 0, 1200, "justdirethings.screen.pickupdelay");
    }

    public static ButtonDefinition dropCountButton(int x, int y, int dropCount) {
        return ButtonDefinition.number(x, y, MachineSettingKeys.DROP_COUNT, dropCount, 1, 64, "justdirethings.screen.dropcount");
    }

    public static ButtonDefinition clickTargetButton(int x, int y, int clickTarget) {
        return ButtonDefinition.toggle(x, y, MachineSettingKeys.CLICK_TARGET, clickTarget, CLICK_TARGET_STATES);
    }

    public static ButtonDefinition clickTypeButton(int x, int y, int clickType) {
        return ButtonDefinition.toggle(x, y, MachineSettingKeys.CLICK_TYPE, clickType, LEFT_RIGHT_CLICK_STATES);
    }

    public static ButtonDefinition swapBlocksButton(int x, int y, boolean swapBlocks) {
        return ButtonDefinition.toggle(x, y, MachineSettingKeys.SWAP_BLOCKS, swapBlocks ? 0 : 1, SWAPPER_BLOCK_STATES);
    }

    public static ButtonDefinition swapEntityTypeButton(int x, int y, int swapEntityType) {
        return ButtonDefinition.toggle(x, y, MachineSettingKeys.SWAP_ENTITY_TYPE, swapEntityType, SWAPPER_ENTITY_STATES);
    }

    public static ButtonDefinition storeExperienceButton(int x, int y) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.STORE_EXPERIENCE, true,
                state("add.png", "justdirethings.screen.storeexp"));
    }

    public static ButtonDefinition extractExperienceButton(int x, int y) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.EXTRACT_EXPERIENCE, true,
                state("remove.png", "justdirethings.screen.retrieveexp"));
    }

    public static ButtonDefinition targetExperienceButton(int x, int y, int targetExperience) {
        return ButtonDefinition.number(x, y, MachineSettingKeys.TARGET_EXPERIENCE, targetExperience, 0, 1000, "justdirethings.screen.targetexp");
    }

    public static ButtonDefinition ownerOnlyButton(int x, int y, boolean ownerOnly) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.OWNER_ONLY, ownerOnly,
                state("player.png", "justdirethings.screen.owneronly"));
    }

    public static ButtonDefinition collectExperienceButton(int x, int y, boolean collectExperience) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.COLLECT_EXPERIENCE, collectExperience,
                state("mindfog.png", "justdirethings.screen.collectexp"));
    }

    public static ButtonDefinition filterOnlyButton(int x, int y, String key, boolean filterOnly) {
        return ButtonDefinition.grayscale(x, y, key, filterOnly,
                state("allowlisttrue.png", "justdirethings.screen.filteronlytrue"));
    }

    public static ButtonDefinition compareCountsButton(int x, int y, String key, boolean compareCounts) {
        return ButtonDefinition.grayscale(x, y, key, compareCounts,
                state("equals.png", "justdirethings.screen.comparecounts"));
    }

    public static ButtonDefinition renderPlayerButton(int x, int y, boolean renderPlayer) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.SHOW_FAKE_PLAYER, renderPlayer,
                state("showfakeplayer.png", "justdirethings.screen.showfakeplayer"));
    }

    public static ButtonDefinition sendInventoryButton(int x, int y) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.SEND_INVENTORY, true,
                state("senditems.png", "justdirethings.screen.senditems"));
    }

    public static ButtonDefinition pullInventoryButton(int x, int y) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.PULL_INVENTORY, true,
                state("pullitems.png", "justdirethings.screen.pullitems"));
    }

    public static ButtonDefinition swapInventoryButton(int x, int y) {
        return ButtonDefinition.grayscale(x, y, MachineSettingKeys.SWAP_INVENTORY, true,
                state("swapitems.png", "justdirethings.screen.swapitems"));
    }

    public static ButtonDefinition inventoryConnectionButton(int x, int y, String key, int value) {
        return ButtonDefinition.toggle(x, y, key, value, INVENTORY_CONNECTION_STATES);
    }

    public static ButtonDefinition sensorTargetButton(int x, int y, int senseTarget) {
        return ButtonDefinition.toggle(x, y, MachineSettingKeys.SENSOR_TARGET, senseTarget, SENSOR_TARGET_STATES);
    }

    public static ButtonDefinition strongWeakRedstoneButton(int x, int y, boolean strongSignal) {
        return ButtonDefinition.toggle(x, y, MachineSettingKeys.STRONG_WEAK_REDSTONE, strongSignal ? 1 : 0, STRONG_WEAK_STATES);
    }

    public static ButtonDefinition senseAmountButton(int x, int y, int senseAmount) {
        return ButtonDefinition.number(x, y, MachineSettingKeys.SENSE_AMOUNT, senseAmount, 0, 9999, "justdirethings.screen.senseamount");
    }

    public static ButtonDefinition equalityButton(int x, int y, int equality) {
        return ButtonDefinition.toggle(x, y, MachineSettingKeys.EQUALITY, equality, EQUALITY_STATES);
    }

    private static ButtonDefinition.State state(String textureName, String localizationKey) {
        return new ButtonDefinition.State(BUTTON_ROOT + textureName, localizationKey);
    }

    private static List<ButtonDefinition.State> states(ButtonDefinition.State... states) {
        return Collections.unmodifiableList(Arrays.asList(states));
    }
}
