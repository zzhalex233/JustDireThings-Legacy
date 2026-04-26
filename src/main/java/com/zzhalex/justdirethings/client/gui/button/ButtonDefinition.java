package com.zzhalex.justdirethings.client.gui.button;

import java.util.Collections;
import java.util.List;

public final class ButtonDefinition {

    public enum Kind {
        TOGGLE,
        GRAYSCALE,
        NUMBER,
        VALUE_ADJUST
    }

    private final Kind kind;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final String settingKey;
    private final int value;
    private final int min;
    private final int max;
    private final int step;
    private final String localizationKey;
    private final List<State> states;

    private ButtonDefinition(Kind kind, int x, int y, int width, int height, String settingKey, int value, int min, int max, int step, String localizationKey, List<State> states) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.settingKey = settingKey;
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
        this.localizationKey = localizationKey;
        this.states = states == null ? Collections.<State>emptyList() : Collections.unmodifiableList(states);
    }

    public static ButtonDefinition toggle(int x, int y, String settingKey, int value, List<State> states) {
        return new ButtonDefinition(Kind.TOGGLE, x, y, 16, 16, settingKey, value, 0, states.size() - 1, 1, "", states);
    }

    public static ButtonDefinition grayscale(int x, int y, String settingKey, boolean active, State state) {
        return new ButtonDefinition(Kind.GRAYSCALE, x, y, 16, 16, settingKey, active ? 1 : 0, 0, 1, 1, state.getLocalizationKey(), Collections.singletonList(state));
    }

    public static ButtonDefinition number(int x, int y, String settingKey, int value, int min, int max, String localizationKey) {
        return new ButtonDefinition(Kind.NUMBER, x, y, 24, 12, settingKey, value, min, max, 1, localizationKey, Collections.<State>emptyList());
    }

    public static ButtonDefinition valueAdjust(int x, int y, String settingKey, int value, int min, int max, int step, State state) {
        return new ButtonDefinition(Kind.VALUE_ADJUST, x, y, 12, 12, settingKey, value, min, max, step, state.getLocalizationKey(), Collections.singletonList(state));
    }

    public Kind getKind() {
        return kind;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public int getValue() {
        return value;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

    public String getLocalizationKey() {
        return localizationKey;
    }

    public List<State> getStates() {
        return states;
    }

    public static final class State {
        private final String texturePath;
        private final String localizationKey;

        public State(String texturePath, String localizationKey) {
            this.texturePath = texturePath;
            this.localizationKey = localizationKey;
        }

        public String getTexturePath() {
            return texturePath;
        }

        public String getLocalizationKey() {
            return localizationKey;
        }
    }
}
