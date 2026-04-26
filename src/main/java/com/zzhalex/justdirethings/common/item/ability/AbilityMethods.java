package com.zzhalex.justdirethings.common.item.ability;

import com.zzhalex.justdirethings.common.entity.group.JDTEntityGroups;
import com.zzhalex.justdirethings.common.entity.EntityDecoy;
import com.zzhalex.justdirethings.common.item.base.ItemToggleableTool;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.data.tool.AbilityCooldown;
import com.zzhalex.justdirethings.data.tool.ToolState;
import com.zzhalex.justdirethings.data.tool.ToolStateIO;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class AbilityMethods {

    private static final int DEFAULT_ACTIVE_COOLDOWN_TICKS = 1200;
    private static final Map<Ability, AbilityAction> USE_ACTIONS = new EnumMap<>(Ability.class);
    private static final Map<Ability, UseOnAbilityAction> USE_ON_ACTIONS = new EnumMap<>(Ability.class);

    static {
        registerUseAction(Ability.MOBSCANNER, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.ORESCANNER, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.LAWNMOWER, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.INVULNERABILITY, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.CAUTERIZEWOUNDS, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.AIRBURST, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.GROUNDSTOMP, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.STUPEFY, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.POLYMORPH_RANDOM, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.VOIDSHIFT, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.DECOY, AbilityMethods::decoy);
        registerUseAction(Ability.OREXRAY, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.GLOWING, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.DEBUFFREMOVER, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.EARTHQUAKE, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.NOAI, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.POLYMORPH_TARGET, AbilityMethods::notYetImplemented);
        registerUseAction(Ability.EPICARROW, AbilityMethods::notYetImplemented);

        registerUseOnAction(Ability.LEAFBREAKER, AbilityMethods::notYetImplementedUseOn);
        registerUseOnAction(Ability.ECLIPSEGATE, AbilityMethods::notYetImplementedUseOn);
    }

    private AbilityMethods() {
    }

    public static boolean canExecute(Ability ability) {
        return ability != null && USE_ACTIONS.containsKey(ability);
    }

    public static boolean canExecuteUseOn(Ability ability) {
        return ability != null && USE_ON_ACTIONS.containsKey(ability);
    }

    public static boolean execute(Ability ability, World world, EntityPlayer player, ItemStack stack) {
        if (ability == null || world == null || player == null || stack == null || stack.isEmpty()) {
            return false;
        }

        AbilityAction action = USE_ACTIONS.get(ability);
        if (action == null) {
            return false;
        }

        return action.execute(world, player, stack);
    }

    public static boolean executeUseOn(
            Ability ability,
            World world,
            EntityPlayer player,
            ItemStack stack,
            BlockPos pos,
            EnumFacing facing,
            EnumHand hand
    ) {
        if (ability == null || world == null || player == null || stack == null || stack.isEmpty() || pos == null || facing == null || hand == null) {
            return false;
        }

        UseOnAbilityAction action = USE_ON_ACTIONS.get(ability);
        if (action == null) {
            return false;
        }

        return action.execute(world, player, stack, pos, facing, hand);
    }

    public static Set<Ability> getRegisteredUseAbilities() {
        return Collections.unmodifiableSet(USE_ACTIONS.keySet());
    }

    public static Set<Ability> getRegisteredUseOnAbilities() {
        return Collections.unmodifiableSet(USE_ON_ACTIONS.keySet());
    }

    public static boolean canRandomlyPolymorph(String entityId) {
        return JDTEntityGroups.canRandomlyPolymorph(entityId);
    }

    public static boolean isPolymorphicTargetDenied(String entityId) {
        return JDTEntityGroups.isPolymorphicTargetDenied(entityId);
    }

    public static boolean isNoAiDenied(String entityId) {
        return JDTEntityGroups.isNoAiDenied(entityId);
    }

    private static void registerUseAction(Ability ability, AbilityAction action) {
        USE_ACTIONS.put(ability, action);
    }

    private static void registerUseOnAction(Ability ability, UseOnAbilityAction action) {
        USE_ON_ACTIONS.put(ability, action);
    }

    private static boolean notYetImplemented(World world, EntityPlayer player, ItemStack stack) {
        // PARITY STUB: ability is registered for audit/recipes, but its upstream action is not ported yet.
        return false;
    }

    private static boolean decoy(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.DECOY) || hasActiveCooldown(stack, Ability.DECOY)) {
            return false;
        }

        EntityDecoy decoy = new EntityDecoy(world);
        decoy.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        decoy.setSummonerName(player.getName());
        decoy.setOwnerUUID(player.getUniqueID());
        boolean spawned = world.spawnEntity(decoy);
        if (!spawned) {
            return false;
        }

        addActiveCooldown(stack, Ability.DECOY, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.EVOCATION_ILLAGER_PREPARE_SUMMON, SoundCategory.PLAYERS, 1.0F, 1.0F);
        stack.damageItem(Ability.DECOY.getDurabilityCost(), player);
        return true;
    }

    private static boolean hasInstalledAbility(ItemStack stack, Ability ability) {
        if (stack.getItem() instanceof ItemToggleableTool) {
            ItemToggleableTool tool = (ItemToggleableTool) stack.getItem();
            return tool.supportsAbility(ability) && tool.hasInstalledAbility(stack, ability);
        }

        return readStackState(stack).hasInstalledAbility(ability.getId());
    }

    private static boolean hasActiveCooldown(ItemStack stack, Ability ability) {
        for (AbilityCooldown cooldown : readStackState(stack).getAbilityCooldowns()) {
            if (ability.getId().equals(cooldown.getAbilityId()) && cooldown.isActive() && cooldown.getRemainingTicks() > 0) {
                return true;
            }
        }
        return false;
    }

    private static void addActiveCooldown(ItemStack stack, Ability ability, int ticks) {
        ToolState state = readStackState(stack);
        state.getAbilityCooldowns().removeIf(cooldown -> ability.getId().equals(cooldown.getAbilityId()));
        state.getAbilityCooldowns().add(new AbilityCooldown(ability.getId(), ticks, true));
        writeStackState(stack, state);
    }

    private static ToolState readStackState(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return new ToolState();
        }
        if (!stack.getTagCompound().hasKey(JDTDataKeys.TOOL_STATE, Constants.NBT.TAG_COMPOUND)) {
            return new ToolState();
        }
        return ToolStateIO.read(stack.getTagCompound().getCompoundTag(JDTDataKeys.TOOL_STATE));
    }

    private static void writeStackState(ItemStack stack, ToolState state) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
        }
        stack.getTagCompound().setTag(JDTDataKeys.TOOL_STATE, ToolStateIO.write(state));
    }

    private static boolean notYetImplementedUseOn(
            World world,
            EntityPlayer player,
            ItemStack stack,
            BlockPos pos,
            EnumFacing facing,
            EnumHand hand
    ) {
        // PARITY STUB: ability use-on action is registered for audit/recipes, but not ported yet.
        return false;
    }

    @FunctionalInterface
    public interface AbilityAction {
        boolean execute(World world, EntityPlayer player, ItemStack stack);
    }

    @FunctionalInterface
    public interface UseOnAbilityAction {
        boolean execute(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing facing, EnumHand hand);
    }
}
