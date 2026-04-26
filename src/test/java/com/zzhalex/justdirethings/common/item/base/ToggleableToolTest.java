package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.data.tool.ToolState;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToggleableToolTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void toolsDefaultToEnabled() {
        TestToggleableTool tool = new TestToggleableTool();
        ToolState state = tool.getToolState(new ItemStack(tool));
        assertTrue(state.isEnabled());
    }

    @Test
    void toolStatePersistsOnStack() {
        TestToggleableTool tool = new TestToggleableTool();
        ItemStack stack = new ItemStack(tool);

        tool.updateToolState(stack, state -> state.setEnabled(false));

        assertFalse(tool.getToolState(stack).isEnabled());
    }

    private static final class TestToggleableTool extends ItemToggleableTool {
    }
}
