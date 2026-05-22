package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.common.entity.EntityFireResistantItem;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityAvailability;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.data.tool.ToolState;
import com.zzhalex.justdirethings.data.tool.ToolStateIO;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ItemToggleableTool extends Item implements ToggleableTool {

    private final EnumSet<Ability> supportedAbilities = EnumSet.noneOf(Ability.class);
    private final Map<Ability, AbilityParams> abilityParams = new EnumMap<>(Ability.class);

    protected ItemToggleableTool() {
        setMaxStackSize(1);
    }

    public Set<Ability> getSupportedAbilities() {
        if (supportedAbilities.isEmpty()) {
            return Collections.emptySet();
        }
        EnumSet<Ability> abilities = EnumSet.noneOf(Ability.class);
        for (Ability ability : supportedAbilities) {
            if (AbilityAvailability.isAvailable(ability)) {
                abilities.add(ability);
            }
        }
        return abilities.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(abilities);
    }

    @Override
    public Map<Ability, AbilityParams> getAbilityParamsMap() {
        return Collections.unmodifiableMap(abilityParams);
    }

    public boolean supportsAbility(Ability ability) {
        return ability != null && supportedAbilities.contains(ability) && AbilityAvailability.isAvailable(ability);
    }

    public ToolState getToolState(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return createDefaultToolState();
        }

        NBTTagCompound rootTag = stack.getTagCompound();
        if (rootTag == null || !rootTag.hasKey(JDTDataKeys.TOOL_STATE, Constants.NBT.TAG_COMPOUND)) {
            return createDefaultToolState();
        }

        return ToolStateIO.read(rootTag.getCompoundTag(JDTDataKeys.TOOL_STATE));
    }

    public void setToolState(ItemStack stack, ToolState state) {
        if (stack == null || stack.isEmpty() || state == null) {
            return;
        }

        NBTTagCompound rootTag = stack.getTagCompound();
        if (rootTag == null) {
            rootTag = new NBTTagCompound();
        }

        rootTag.setTag(JDTDataKeys.TOOL_STATE, ToolStateIO.write(state));
        stack.setTagCompound(rootTag);
    }

    public void updateToolState(ItemStack stack, Consumer<ToolState> updater) {
        if (updater == null || stack == null || stack.isEmpty()) {
            return;
        }

        ToolState state = getToolState(stack);
        updater.accept(state);
        setToolState(stack, state);
    }

    public boolean isEnabled(ItemStack stack) {
        return getToolState(stack).isEnabled();
    }

    public void setEnabled(ItemStack stack, boolean enabled) {
        updateToolState(stack, state -> state.setEnabled(enabled));
    }

    public boolean hasInstalledAbility(ItemStack stack, Ability ability) {
        return ability != null && (!ability.requiresUpgrade() || getToolState(stack).hasInstalledAbility(ability.getId()));
    }

    public void installAbility(ItemStack stack, Ability ability) {
        if (ability == null) {
            return;
        }
        updateToolState(stack, state -> state.getInstalledAbilities().add(ability.getId()));
    }

    public int getAbilityValue(ItemStack stack, Ability ability, int defaultValue) {
        if (ability == null) {
            return defaultValue;
        }
        return getToolState(stack).getAbilityValues().getOrDefault(ability.getId(), defaultValue);
    }

    public void setAbilityValue(ItemStack stack, Ability ability, int value) {
        if (ability == null) {
            return;
        }
        updateToolState(stack, state -> state.getAbilityValues().put(ability.getId(), value));
    }

    protected final void addSupportedAbility(Ability ability) {
        if (ability != null) {
            supportedAbilities.add(ability);
        }
    }

    protected final void addSupportedAbility(Ability ability, AbilityParams params) {
        addSupportedAbility(ability);
        if (ability != null && params != null) {
            abilityParams.put(ability, params);
        }
    }

    protected final void addSupportedAbilities(Ability... abilities) {
        supportedAbilities.addAll(Arrays.asList(abilities));
    }

    protected ToolState createDefaultToolState() {
        return new ToolState();
    }

    protected ActionResult<ItemStack> openSettingsIfSneaking(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!world.isRemote) {
                player.openGui(JustDireThingsLegacy.INSTANCE, ModContainers.GUI_TOOL_SETTINGS, world, 0, 0, 0);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return null;
    }

    public boolean isFireResistantDrop(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return isFireResistantDrop(stack);
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        if (!isFireResistantDrop(itemstack)) {
            return null;
        }
        EntityFireResistantItem entity = new EntityFireResistantItem(world, location.posX, location.posY, location.posZ, itemstack);
        if (location instanceof EntityItem) {
            NBTTagCompound tag = new NBTTagCompound();
            location.writeToNBT(tag);
            tag.removeTag("UUIDMost");
            tag.removeTag("UUIDLeast");
            entity.readFromNBT(tag);
            entity.setItem(itemstack);
            entity.setPosition(location.posX, location.posY, location.posZ);
        }
        entity.motionX = location.motionX;
        entity.motionY = location.motionY;
        entity.motionZ = location.motionZ;
        entity.rotationYaw = location.rotationYaw;
        entity.rotationPitch = location.rotationPitch;
        entity.prevRotationYaw = location.prevRotationYaw;
        entity.prevRotationPitch = location.prevRotationPitch;
        return entity;
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if (entityItem != null && isFireResistantDrop(entityItem.getItem())) {
            entityItem.extinguish();
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (getSupportedAbilities().isEmpty()) {
            return;
        }
        TooltipHelper.appendToolEnabled(stack, tooltip);
        if (GuiScreen.isShiftKeyDown()) {
            TooltipHelper.appendAbilityList(stack, tooltip);
        } else {
            TooltipHelper.appendShiftForInfo(stack, tooltip);
        }
    }
}
