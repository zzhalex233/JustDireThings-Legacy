package com.zzhalex.justdirethings.common.tile.machine;

import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileSensorParityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void comparisonSemanticsMatchUpstreamSenseAmount() {
        TestSensor sensor = new TestSensor();

        sensor.setSenseAmount(0);
        sensor.setEquality(2);
        assertTrue(sensor.check(0), "Upstream equals mode compares directly against zero, not as a special any-match case");
        assertFalse(sensor.check(1));

        sensor.setSenseAmount(2);
        sensor.setEquality(0);
        assertFalse(sensor.check(2));
        assertTrue(sensor.check(3));

        sensor.setEquality(1);
        assertTrue(sensor.check(1));
        assertFalse(sensor.check(2));
    }

    @Test
    void blockFilterUsesAllowlistAndDenylistSemantics() {
        TestSensor sensor = new TestSensor();
        IBlockState stone = Blocks.STONE.getDefaultState();
        sensor.getFilterHandler().setStackInSlot(0, new ItemStack(Blocks.STONE));

        sensor.getFilterState().setAllowList(true);
        assertTrue(sensor.blockMatches(stone));
        assertFalse(sensor.blockMatches(Blocks.DIRT.getDefaultState()));

        sensor.getFilterState().setAllowList(false);
        assertFalse(sensor.blockMatches(stone));
        assertTrue(sensor.blockMatches(Blocks.DIRT.getDefaultState()));
    }

    @Test
    void blockStatePropertiesRefineMatchingLikeUpstream() {
        TestSensor sensor = new TestSensor();
        IBlockState poweredLever = Blocks.LEVER.getDefaultState().withProperty(BlockLever.POWERED, true);
        IBlockState unpoweredLever = Blocks.LEVER.getDefaultState().withProperty(BlockLever.POWERED, false);
        ItemStack leverStack = new ItemStack(Blocks.LEVER);
        sensor.getFilterHandler().setStackInSlot(0, leverStack);
        sensor.setSensorProperty(leverStack, BlockLever.POWERED, true, false);

        sensor.getFilterState().setAllowList(true);
        assertTrue(sensor.blockMatches(poweredLever));
        assertFalse(sensor.blockMatches(unpoweredLever));

        sensor.getFilterState().setAllowList(false);
        assertFalse(sensor.blockMatches(poweredLever));
        assertTrue(sensor.blockMatches(unpoweredLever));
    }

    @Test
    void t2SensorOwnsAndPersistsAdvancedFilterSlots() {
        TileSensor.T2 sensor = new TileSensor.T2();

        assertNotNull(sensor.getFilterHandler());
        assertEquals(9, sensor.getFilterHandler().getSlots());
        sensor.getFilterHandler().setStackInSlot(0, new ItemStack(Blocks.STONE));

        NBTTagCompound tag = sensor.writeMachineStateToNbt(new NBTTagCompound());
        tag.setInteger("SignalStrength", sensor.getSignalStrength());
        tag.setInteger("SenseTarget", sensor.getSenseTarget());
        tag.setBoolean("StrongSignal", sensor.isStrongSignal());
        tag.setInteger("SenseAmount", sensor.getSenseAmount());
        tag.setInteger("Equality", sensor.getEquality());
        tag.setTag("Filters", sensor.getFilterHandler().serializeNBT());
        TileSensor.T2 restored = new TileSensor.T2();
        restored.readFromNBT(tag);

        assertEquals(new ItemStack(Blocks.STONE).getItem(), restored.getFilterHandler().getStackInSlot(0).getItem());
    }

    @Test
    void t2SensorReadsLegacyAdvancedFilterTagForExistingWorlds() {
        TileSensor.T2 sensor = new TileSensor.T2();
        sensor.getFilterHandler().setStackInSlot(0, new ItemStack(Blocks.DIRT));

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("AdvancedFilters", sensor.getFilterHandler().serializeNBT());
        TileSensor.T2 restored = new TileSensor.T2();
        restored.readFromNBT(tag);

        assertEquals(new ItemStack(Blocks.DIRT).getItem(), restored.getFilterHandler().getStackInSlot(0).getItem());
    }

    @Test
    void entityFiltersSupportVanillaSpawnEggsLikeUpstream() {
        TestSensor sensor = new TestSensor();
        ItemStack zombieEgg = new ItemStack(Items.SPAWN_EGG);
        ItemMonsterPlacer.applyEntityIdToItemStack(zombieEgg, new ResourceLocation("minecraft:zombie"));

        sensor.getFilterHandler().setStackInSlot(0, zombieEgg);

        assertTrue(sensor.sourceContains("ItemMonsterPlacer.getNamedIdFrom(filter)"));
        assertTrue(sensor.sourceContains("EntityList.getKey(entity)"));
    }

    @Test
    void sensorDefaultsToWeakModeLikeUpstream() {
        assertFalse(new TileSensor.T1().isStrongSignal());
        assertFalse(new TileSensor.T2().isStrongSignal());
    }

    @Test
    void sensorBlockKeepsWeakAndStrongOutputsSeparate() throws Exception {
        String source = java.nio.file.Files.readString(
                java.nio.file.Paths.get("src/main/java/com/zzhalex/justdirethings/common/block/machine/BlockSensor.java"),
                java.nio.charset.StandardCharsets.UTF_8
        );

        assertTrue(source.contains("isFacingSide(blockState, side)"));
        assertTrue(source.contains("!((TileSensor) tileEntity).isStrongSignal()"));
        assertTrue(source.contains("return getWeakPower(blockState, blockAccess, pos, side);"));
        assertTrue(source.contains("getValue(FACING).getOpposite()"));
    }

    private static final class TestSensor extends TileSensor {
        private boolean check(int matches) {
            return passesComparison(matches);
        }

        private boolean blockMatches(IBlockState state) {
            return matchesFilter(state);
        }

        private boolean sourceContains(String needle) {
            try {
                String source = java.nio.file.Files.readString(
                        java.nio.file.Paths.get("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileSensor.java"),
                        java.nio.charset.StandardCharsets.UTF_8
                );
                return source.contains(needle);
            } catch (java.io.IOException exception) {
                throw new AssertionError(exception);
            }
        }
    }
}
