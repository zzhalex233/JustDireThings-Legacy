package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.MachineFilterHelper;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileFilteredMachine;
import com.zzhalex.justdirethings.common.tile.base.TileInventoryMachineBase;
import com.zzhalex.justdirethings.common.entity.EntityCreatureCatcher;
import com.zzhalex.justdirethings.common.item.misc.ItemCreatureCatcher;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TileSensor extends TileInventoryMachineBase implements ITickable, TileFilteredMachine {

    public static final int FILTER_SLOT_COUNT = 9;

    private int signalStrength;
    private int senseTarget;
    private boolean strongSignal;
    private int senseAmount;
    private int equality;
    private final FilterItemHandler filterHandler = new FilterItemHandler(FILTER_SLOT_COUNT);
    private final List<BlockPos> positionsToSense = new ArrayList<>();
    private final Map<Integer, Map<IProperty<?>, Comparable<?>>> blockStateProperties = new HashMap<>();

    public TileSensor() {
        super(0);
        setTickSpeed(20);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        handleTicks();
        evaluateRedstoneControl();
        if (canRun() && canSense()) {
            setRedstoneSignal(passesComparison(sense()));
        }
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public int getSenseTarget() {
        return senseTarget;
    }

    public void setSenseTarget(int senseTarget) {
        this.senseTarget = Math.max(0, Math.min(8, senseTarget));
        positionsToSense.clear();
    }

    public boolean isStrongSignal() {
        return strongSignal;
    }

    public void setStrongSignal(boolean strongSignal) {
        boolean changed = this.strongSignal != strongSignal;
        this.strongSignal = strongSignal;
        if (changed) {
            notifySensorNeighbors();
        }
    }

    public int getSenseAmount() {
        return senseAmount;
    }

    public void setSenseAmount(int senseAmount) {
        this.senseAmount = Math.max(0, senseAmount);
    }

    public int getEquality() {
        return equality;
    }

    public void setEquality(int equality) {
        this.equality = Math.max(0, Math.min(2, equality));
    }

    @Override
    public FilterItemHandler getFilterHandler() {
        return filterHandler;
    }

    protected boolean canSense() {
        return true;
    }

    protected IItemHandler getSensorFilterHandler() {
        return filterHandler;
    }

    protected boolean canRun() {
        return getOperationTicks() == 0 || getRedstoneState().isPulseMode();
    }

    private void setRedstoneSignal(boolean emit) {
        int nextSignal = emit ? 15 : 0;
        if (nextSignal != signalStrength) {
            signalStrength = nextSignal;
            markDirtyClient();
            notifySensorNeighbors();
        }
    }

    private void notifySensorNeighbors() {
        if (world == null || pos == null) {
            return;
        }
        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), true);
        world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
    }

    protected int sense() {
        if (senseTarget == 0 || senseTarget == 1) {
            if (positionsToSense.isEmpty()) {
                positionsToSense.addAll(findPositions());
            }
            int found = 0;
            while (!positionsToSense.isEmpty()) {
                BlockPos target = positionsToSense.remove(0);
                if (senseBlock(target)) {
                    found++;
                }
            }
            return found;
        }
        return countEntities(getEntitySearchBox());
    }

    protected List<BlockPos> findPositions() {
        List<BlockPos> positions = new ArrayList<>();
        positions.add(MachineActionHelper.targetPos(this));
        return positions;
    }

    protected AxisAlignedBB getEntitySearchBox() {
        return new AxisAlignedBB(MachineActionHelper.targetPos(this));
    }

    protected boolean senseBlock(BlockPos targetPos) {
        if (senseTarget == 0 || senseTarget == 1) {
            boolean air = world.isAirBlock(targetPos);
            if (senseTarget == 1) {
                return air;
            }
            if (air || !matchesFilter(world.getBlockState(targetPos))) {
                return false;
            }
            return true;
        }
        return false;
    }

    protected int countTargets(BlockPos targetPos) {
        if (senseTarget == 0 || senseTarget == 1) {
            return senseBlock(targetPos) ? 1 : 0;
        }
        return countEntities(new AxisAlignedBB(targetPos));
    }

    protected int countEntities(AxisAlignedBB searchBox) {
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, searchBox);
        int count = 0;
        for (Entity entity : entities) {
            if (matchesEntityTarget(entity)) {
                count++;
            }
        }
        return count;
    }

    protected boolean matchesFilter(IBlockState state) {
        ItemStack blockStack = getFilterStackForState(state);
        boolean returnValue = MachineFilterHelper.matchesFilter(getSensorFilterHandler(), getFilterState(), blockStack);
        boolean allowList = getFilterState().isAllowList();

        outerLoop:
        for (Map.Entry<Integer, Map<IProperty<?>, Comparable<?>>> propertyValues : blockStateProperties.entrySet()) {
            ItemStack filterStack = getSensorFilterHandler().getStackInSlot(propertyValues.getKey());
            if (!matchesPropertyFilterTarget(filterStack, blockStack)) {
                continue;
            }
            for (Map.Entry<IProperty<?>, Comparable<?>> propertyValue : propertyValues.getValue().entrySet()) {
                boolean propertyMatch = matchesProperty(state, propertyValue.getKey(), propertyValue.getValue());
                if ((allowList && propertyMatch) || (!allowList && !propertyMatch)) {
                    returnValue = true;
                } else {
                    returnValue = false;
                    break outerLoop;
                }
            }
        }
        return returnValue;
    }

    protected boolean matchesEntityTarget(Entity entity) {
        boolean targetMatches;
        switch (senseTarget) {
            case 2:
                targetMatches = entity instanceof IMob;
                break;
            case 3:
                targetMatches = entity instanceof EntityAnimal;
                break;
            case 4:
                targetMatches = entity instanceof EntityAnimal && !((EntityAnimal) entity).isChild();
                break;
            case 5:
                targetMatches = entity instanceof EntityAnimal && ((EntityAnimal) entity).isChild();
                break;
            case 6:
                targetMatches = entity instanceof EntityPlayer;
                break;
            case 7:
                targetMatches = entity instanceof EntityLivingBase;
                break;
            case 8:
                targetMatches = entity instanceof EntityItem;
                break;
            default:
                return false;
        }
        return targetMatches && matchesEntityFilter(entity);
    }

    protected boolean matchesEntityFilter(Entity entity) {
        boolean allowList = getFilterState().isAllowList();
        boolean hasFilter = false;
        for (int slot = 0; slot < getSensorFilterHandler().getSlots(); slot++) {
            ItemStack filter = getSensorFilterHandler().getStackInSlot(slot);
            if (filter.isEmpty()) {
                continue;
            }
            hasFilter = true;
            if (matchesEntityFilterStack(filter, entity)) {
                return allowList;
            }
        }
        return hasFilter ? !allowList : !allowList;
    }

    private boolean matchesEntityFilterStack(ItemStack filter, Entity entity) {
        if (filter.getItem() instanceof ItemMonsterPlacer) {
            return matchesSpawnEgg(filter, entity);
        }
        if (filter.getItem() instanceof ItemCreatureCatcher) {
            return matchesCreatureCatcher(filter, entity);
        }
        return false;
    }

    private boolean matchesSpawnEgg(ItemStack filter, Entity entity) {
        ResourceLocation eggEntityId = ItemMonsterPlacer.getNamedIdFrom(filter);
        ResourceLocation entityId = EntityList.getKey(entity);
        return eggEntityId != null && eggEntityId.equals(entityId);
    }

    private boolean matchesCreatureCatcher(ItemStack filter, Entity entity) {
        String capturedId = ItemCreatureCatcher.getCapturedEntityId(filter);
        ResourceLocation entityId = EntityList.getKey(entity);
        if (capturedId.isEmpty() || entityId == null || !capturedId.equals(entityId.toString())) {
            return false;
        }
        if (!getFilterState().isCompareNbt()) {
            return true;
        }
        Entity captured = EntityCreatureCatcher.createCapturedEntity(filter, world);
        if (captured == null) {
            return false;
        }
        NBTTagCompound capturedTag = normalizedEntityTag(captured);
        NBTTagCompound targetTag = normalizedEntityTag(entity);
        return capturedTag.equals(targetTag);
    }

    private static NBTTagCompound normalizedEntityTag(Entity entity) {
        NBTTagCompound tag = new NBTTagCompound();
        entity.writeToNBT(tag);
        tag.removeTag("AbsorptionAmount");
        tag.removeTag("Age");
        tag.removeTag("Air");
        tag.removeTag("ArmorDropChances");
        tag.removeTag("ArmorItems");
        tag.removeTag("Attributes");
        tag.removeTag("CanPickUpLoot");
        tag.removeTag("DeathTime");
        tag.removeTag("Dimension");
        tag.removeTag("FallDistance");
        tag.removeTag("Fire");
        tag.removeTag("HandDropChances");
        tag.removeTag("HandItems");
        tag.removeTag("HurtByTimestamp");
        tag.removeTag("HurtTime");
        tag.removeTag("Invulnerable");
        tag.removeTag("Motion");
        tag.removeTag("OnGround");
        tag.removeTag("PortalCooldown");
        tag.removeTag("Pos");
        tag.removeTag("Rotation");
        tag.removeTag("UUIDLeast");
        tag.removeTag("UUIDMost");
        tag.removeTag("id");
        tag.removeTag("NoAI");
        tag.removeTag("Silent");
        tag.removeTag("Glowing");
        tag.removeTag("Tags");
        tag.removeTag("Leashed");
        tag.removeTag("Leash");
        tag.removeTag("CustomName");
        tag.removeTag("ActiveEffects");
        return tag;
    }

    protected boolean passesComparison(int matches) {
        switch (equality) {
            case 1:
                return matches < senseAmount;
            case 2:
                return matches == senseAmount;
            case 0:
            default:
                return matches > senseAmount;
        }
    }

    public IBlockState getStateForStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) stack.getItem()).getBlock();
            return block.getStateFromMeta(stack.getMetadata());
        }
        if (stack.getItem() == Items.WATER_BUCKET) {
            return Blocks.WATER.getDefaultState();
        }
        if (stack.getItem() == Items.LAVA_BUCKET) {
            return Blocks.LAVA.getDefaultState();
        }
        return null;
    }

    public Comparable<?> getPropertyValue(ItemStack stack, IProperty<?> property) {
        int slot = findFilterSlot(stack);
        if (slot < 0) {
            return null;
        }
        return blockStateProperties.getOrDefault(slot, new HashMap<IProperty<?>, Comparable<?>>()).get(property);
    }

    public List<Comparable<?>> getAllowedValues(ItemStack stack, IProperty<?> property) {
        List<Comparable<?>> values = new ArrayList<>();
        if (property == null) {
            return values;
        }
        for (Comparable<?> value : property.getAllowedValues()) {
            values.add(value);
        }
        return values;
    }

    public void setSensorProperty(ItemStack stack, IProperty<?> property, Comparable<?> value, boolean clearWhenDefault) {
        int slot = findFilterSlot(stack);
        setSensorProperty(slot, property, value, clearWhenDefault);
    }

    public void setSensorProperty(int slot, IProperty<?> property, Comparable<?> value, boolean clearWhenDefault) {
        if (slot < 0 || property == null) {
            return;
        }
        IBlockState state = getStateForStack(getSensorFilterHandler().getStackInSlot(slot));
        Map<IProperty<?>, Comparable<?>> properties = blockStateProperties.computeIfAbsent(slot, ignored -> new LinkedHashMap<>());
        if (value == null || (clearWhenDefault && state != null && value.equals(state.getValue(property)))) {
            properties.remove(property);
        } else {
            properties.put(property, value);
        }
        if (properties.isEmpty()) {
            blockStateProperties.remove(slot);
        }
        markDirtyClient();
    }

    public void clearSensorProperties(int slot) {
        if (blockStateProperties.remove(slot) != null) {
            markDirtyClient();
        }
    }

    public Map<IProperty<?>, Comparable<?>> getSensorProperties(int slot) {
        return new LinkedHashMap<>(blockStateProperties.getOrDefault(slot, new LinkedHashMap<IProperty<?>, Comparable<?>>()));
    }

    public void setSensorProperties(int slot, Map<IProperty<?>, Comparable<?>> properties) {
        if (slot < 0) {
            return;
        }
        if (properties == null || properties.isEmpty()) {
            blockStateProperties.remove(slot);
        } else {
            blockStateProperties.put(slot, new LinkedHashMap<>(properties));
        }
        markDirtyClient();
    }

    private int findFilterSlot(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return -1;
        }
        IItemHandler filterHandler = getSensorFilterHandler();
        for (int slot = 0; slot < filterHandler.getSlots(); slot++) {
            if (matchesFilterStack(filterHandler.getStackInSlot(slot), stack)) {
                return slot;
            }
        }
        return -1;
    }

    private boolean matchesFilterStack(ItemStack filterStack, ItemStack stack) {
        return !filterStack.isEmpty() && !stack.isEmpty() && MachineFilterHelper.matchesFilterStack(getFilterState(), filterStack, stack);
    }

    private boolean matchesPropertyFilterTarget(ItemStack filterStack, ItemStack blockStack) {
        if (filterStack.isEmpty() || blockStack.isEmpty() || filterStack.getItem() != blockStack.getItem()) {
            return false;
        }
        return !getFilterState().isCompareNbt() || ItemStack.areItemStackTagsEqual(filterStack, blockStack);
    }

    private ItemStack getFilterStackForState(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockLiquid) {
            if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                return new ItemStack(Items.WATER_BUCKET);
            }
            if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
                return new ItemStack(Items.LAVA_BUCKET);
            }
        }
        return new ItemStack(block, 1, block.getMetaFromState(state));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matchesProperty(IBlockState state, IProperty property, Comparable expected) {
        if (!state.getPropertyKeys().contains(property)) {
            return false;
        }
        Comparable actual = (Comparable) state.getValue(property);
        return actual != null && actual.equals(expected);
    }

    private NBTTagCompound writeBlockStateProperties() {
        NBTTagCompound compound = new NBTTagCompound();
        for (Map.Entry<Integer, Map<IProperty<?>, Comparable<?>>> slotEntry : blockStateProperties.entrySet()) {
            NBTTagList properties = new NBTTagList();
            for (Map.Entry<IProperty<?>, Comparable<?>> propertyEntry : slotEntry.getValue().entrySet()) {
                NBTTagCompound property = new NBTTagCompound();
                property.setString("Name", propertyEntry.getKey().getName());
                property.setString("Value", getPropertyName(propertyEntry.getKey(), propertyEntry.getValue()));
                properties.appendTag(property);
            }
            compound.setTag(Integer.toString(slotEntry.getKey()), properties);
        }
        return compound;
    }

    private void readBlockStateProperties(NBTTagCompound compound) {
        blockStateProperties.clear();
        for (String key : compound.getKeySet()) {
            int slot;
            try {
                slot = Integer.parseInt(key);
            } catch (NumberFormatException ignored) {
                continue;
            }
            ItemStack filterStack = getSensorFilterHandler().getStackInSlot(slot);
            IBlockState state = getStateForStack(filterStack);
            if (state == null) {
                continue;
            }
            NBTTagList properties = compound.getTagList(key, Constants.NBT.TAG_COMPOUND);
            Map<IProperty<?>, Comparable<?>> loaded = new LinkedHashMap<>();
            for (int index = 0; index < properties.tagCount(); index++) {
                NBTTagCompound propertyTag = properties.getCompoundTagAt(index);
                IProperty<?> property = state.getBlock().getBlockState().getProperty(propertyTag.getString("Name"));
                Comparable<?> value = getPropertyValue(property, propertyTag.getString("Value"));
                if (property != null && value != null) {
                    loaded.put(property, value);
                }
            }
            if (!loaded.isEmpty()) {
                blockStateProperties.put(slot, loaded);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String getPropertyName(IProperty property, Comparable value) {
        return property.getName(value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Comparable<?> getPropertyValue(IProperty property, String value) {
        Object parsed = property == null ? null : property.parseValue(value).orNull();
        return parsed instanceof Comparable ? (Comparable<?>) parsed : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("SignalStrength", signalStrength);
        compound.setInteger("SenseTarget", senseTarget);
        compound.setBoolean("StrongSignal", strongSignal);
        compound.setInteger("SenseAmount", senseAmount);
        compound.setInteger("Equality", equality);
        compound.setTag("Filters", filterHandler.serializeNBT());
        compound.setTag("BlockStateProps", writeBlockStateProperties());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        signalStrength = compound.getInteger("SignalStrength");
        setSenseTarget(compound.getInteger("SenseTarget"));
        strongSignal = compound.hasKey("StrongSignal") && compound.getBoolean("StrongSignal");
        senseAmount = compound.getInteger("SenseAmount");
        equality = compound.getInteger("Equality");
        if (compound.hasKey("Filters")) {
            filterHandler.deserializeNBT(compound.getCompoundTag("Filters"));
        } else if (compound.hasKey("AdvancedFilters")) {
            filterHandler.deserializeNBT(compound.getCompoundTag("AdvancedFilters"));
        }
        if (compound.hasKey("BlockStateProps")) {
            readBlockStateProperties(compound.getCompoundTag("BlockStateProps"));
        }
    }

    public static class T1 extends TileSensor {
    }

    public static class T2 extends TileSensor implements TileAdvancedMachine {

        public T2() {
            configureAdvancedMachine();
        }

        @Override
        public int getStandardEnergyCost() {
            return 2;
        }

        @Override
        protected boolean canSense() {
            int cost = getEnergyCost();
            return consumeEnergy(cost, false) >= cost;
        }

        public int getEnergyCost() {
            return Math.max(1, getAreaPositionsNearestFirst().size()) * getStandardEnergyCost();
        }

        @Override
        protected int countTargets(BlockPos targetPos) {
            return super.countTargets(targetPos);
        }

        @Override
        protected List<BlockPos> findPositions() {
            return getAreaPositionsNearestFirst();
        }

        @Override
        protected AxisAlignedBB getEntitySearchBox() {
            return getAreaState().createArea(pos);
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            return super.writeToNBT(compound);
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            super.readFromNBT(compound);
        }
    }
}
