package com.zzhalex.justdirethings.common.item.ability;

import com.zzhalex.justdirethings.common.block.group.JDTBlockGroups;
import com.zzhalex.justdirethings.common.entity.group.JDTEntityGroups;
import com.zzhalex.justdirethings.common.entity.EntityDecoy;
import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTBow;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.BoundInventoryHelper;
import com.zzhalex.justdirethings.common.item.base.FluidBackedItem;
import com.zzhalex.justdirethings.common.item.base.ItemToggleableTool;
import com.zzhalex.justdirethings.common.item.base.PoweredEnergyCostHelper;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.event.ToolMiningAbilityHandler;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import com.zzhalex.justdirethings.common.tile.TileEclipseGate;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.data.tool.ToolState;
import com.zzhalex.justdirethings.data.tool.ToolStateIO;
import com.zzhalex.justdirethings.registry.ModContentBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class AbilityMethods {

    private static final int DEFAULT_ACTIVE_COOLDOWN_TICKS = 1200;
    private static final int GLOWING_RADIUS = 20;
    private static final int GLOWING_DURATION_TICKS = 200;
    private static final int LAWNMOWER_MAX_BREAK = 64;
    private static final int LAWNMOWER_RADIUS = 5;
    private static final int GROUNDSTOMP_RADIUS = 3;
    private static final int STUPEFY_RANGE = 32;
    private static final int LEAFBREAKER_RADIUS = 2;
    private static final int POLYMORPH_RANGE = 4;
    private static final int RANDOM_POLYMORPH_FLUID_COST = 100;
    private static final int TARGET_POLYMORPH_FLUID_COST = 250;
    private static final int EARTHQUAKE_RADIUS = 5;
    private static final int NOAI_RADIUS = 5;
    private static final int EARTHQUAKE_SLOW_DURATION_TICKS = 200;
    private static final int EARTHQUAKE_SLOW_AMPLIFIER = 3;
    private static final Map<Ability, AbilityAction> USE_ACTIONS = new EnumMap<>(Ability.class);
    private static final Map<Ability, UseOnAbilityAction> USE_ON_ACTIONS = new EnumMap<>(Ability.class);

    static {
        registerUseAction(Ability.MOBSCANNER, AbilityMethods::scanForMobScanner);
        registerUseAction(Ability.ORESCANNER, AbilityMethods::scanForOreScanner);
        registerUseAction(Ability.LAWNMOWER, AbilityMethods::lawnmower);
        registerUseAction(Ability.INVULNERABILITY, AbilityMethods::invulnerability);
        registerUseAction(Ability.CAUTERIZEWOUNDS, AbilityMethods::cauterizeWounds);
        registerUseAction(Ability.AIRBURST, AbilityMethods::airBurst);
        registerUseAction(Ability.GROUNDSTOMP, AbilityMethods::groundstomp);
        registerUseAction(Ability.STUPEFY, AbilityMethods::stupefy);
        registerUseAction(Ability.POLYMORPH_RANDOM, AbilityMethods::polymorphRandom);
        registerUseAction(Ability.VOIDSHIFT, AbilityMethods::voidShift);
        registerUseAction(Ability.DECOY, AbilityMethods::decoy);
        registerUseAction(Ability.OREXRAY, AbilityMethods::scanForOreXRay);
        registerUseAction(Ability.GLOWING, AbilityMethods::glowing);
        registerUseAction(Ability.DEBUFFREMOVER, AbilityMethods::debuffRemover);
        registerUseAction(Ability.EARTHQUAKE, AbilityMethods::earthquake);
        registerUseAction(Ability.NOAI, AbilityMethods::noAI);
        registerUseAction(Ability.POLYMORPH_TARGET, AbilityMethods::polymorphTarget);
        registerUseAction(Ability.EPICARROW, AbilityMethods::epicArrow);

        registerUseOnAction(Ability.LEAFBREAKER, AbilityMethods::leafbreaker);
        registerUseOnAction(Ability.ECLIPSEGATE, AbilityMethods::eclipseGate);
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

    public static Entity getLookedAtEntity(World world, EntityPlayer player, double maxDistance) {
        return getEntityLookedAt(world, player, maxDistance);
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

    private static boolean scanForMobScanner(World world, EntityPlayer player, ItemStack stack) {
        if (!canUseAbilityAndDurability(stack, Ability.MOBSCANNER)) {
            return false;
        }
        return scanFor(world, player, stack, Ability.MOBSCANNER);
    }

    private static boolean scanForOreScanner(World world, EntityPlayer player, ItemStack stack) {
        if (!canUseAbilityAndDurability(stack, Ability.ORESCANNER)) {
            return false;
        }
        return scanFor(world, player, stack, Ability.ORESCANNER);
    }

    private static boolean scanForOreXRay(World world, EntityPlayer player, ItemStack stack) {
        if (!canUseAbilityAndDurability(stack, Ability.OREXRAY)) {
            return false;
        }
        return scanFor(world, player, stack, Ability.OREXRAY);
    }

    private static boolean scanFor(World world, EntityPlayer player, ItemStack stack, Ability ability) {
        if (world.isRemote) {
            JustDireThingsLegacy.proxy.discoverThings(player, ability, stack);
            world.playSound(player, player.posX, player.posY, player.posZ,
                    ability == Ability.OREXRAY ? SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON : SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
            return false;
        }

        damageTool(stack, player, ability);
        return false;
    }

    private static boolean lawnmower(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.LAWNMOWER)) {
            return false;
        }

        Set<BlockPos> breakBlocks = findLawnmowerBlocks(world, getPlayerOnPos(player), LAWNMOWER_MAX_BREAK, LAWNMOWER_RADIUS);
        List<ItemStack> drops = new ArrayList<>();
        for (BlockPos pos : breakBlocks) {
            if (!canUseAbilityAndDurability(stack, Ability.LAWNMOWER)) {
                break;
            }
            IBlockState state = world.getBlockState(pos);
            if (world.destroyBlock(pos, false)) {
                drops.addAll(collectBlockDrops(world, pos, state, 0));
                if (world.rand.nextFloat() < 0.1F) {
                    damageTool(stack, player, Ability.LAWNMOWER);
                }
            }
        }

        if (!breakBlocks.isEmpty()) {
            BlockPos firstPos = breakBlocks.iterator().next();
            handleAbilityDrops(stack, world, firstPos, player, drops, breakBlocks);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }

    private static boolean airBurst(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote) {
            return true;
        }

        int multiplier = getToolValue(stack, Ability.AIRBURST);
        if (!canUseAbilityAndDurability(stack, Ability.AIRBURST, Math.max(1, multiplier))) {
            return false;
        }

        Vec3d look = player.getLookVec();
        double addedStrength = (double) multiplier / 2.0D;
        double burstStrength = 1.5D + addedStrength;
        player.motionX = look.x * burstStrength;
        player.motionY = look.y * burstStrength;
        player.motionZ = look.z * burstStrength;
        player.fallDistance = 0.0F;
        player.velocityChanged = true;
        EntityPlayerMP serverPlayer = player instanceof EntityPlayerMP ? (EntityPlayerMP) player : null;
        if (serverPlayer != null && serverPlayer.connection != null) {
            serverPlayer.connection.sendPacket(new SPacketEntityVelocity(player));
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.5F, 0.125F);
        damageTool(stack, player, Ability.AIRBURST, Math.max(1, multiplier));
        return true;
    }

    private static boolean voidShift(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.VOIDSHIFT)) {
            return false;
        }

        Vec3d shiftPosition = getShiftPosition(world, player, stack);
        if (Vec3d.ZERO.equals(shiftPosition) || !world.getWorldBorder().contains(new BlockPos(shiftPosition))) {
            return false;
        }

        int distanceTraveled = (int) player.getPositionVector().distanceTo(shiftPosition);
        if (distanceTraveled <= 0 || !canUseAbilityAndDurability(stack, Ability.VOIDSHIFT, distanceTraveled)) {
            return false;
        }

        teleportPlayerSafely(player, shiftPosition);
        player.fallDistance = 0.0F;
        world.playSound(null, shiftPosition.x, shiftPosition.y, shiftPosition.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        damageTool(stack, player, Ability.VOIDSHIFT, Math.max(1, distanceTraveled));
        return false;
    }

    private static void teleportPlayerSafely(EntityPlayer player, Vec3d position) {
        EntityPlayerMP serverPlayer = player instanceof EntityPlayerMP ? (EntityPlayerMP) player : null;
        if (serverPlayer != null && serverPlayer.connection != null) {
            serverPlayer.connection.setPlayerLocation(position.x, position.y, position.z, player.rotationYaw, player.rotationPitch);
            return;
        }
        player.setPosition(position.x, position.y, position.z);
    }

    public static boolean canStartVoidShift(World world, EntityPlayer player, ItemStack stack) {
        if (world == null || player == null || stack == null || stack.isEmpty() || !canUseAbility(stack, Ability.VOIDSHIFT)) {
            return false;
        }
        Vec3d shiftPosition = getShiftPosition(world, player, stack);
        if (Vec3d.ZERO.equals(shiftPosition) || !world.getWorldBorder().contains(new BlockPos(shiftPosition))) {
            return false;
        }
        int distanceTraveled = (int) player.getPositionVector().distanceTo(shiftPosition);
        return distanceTraveled > 0 && canUseAbilityAndDurability(stack, Ability.VOIDSHIFT, distanceTraveled);
    }

    private static boolean cauterizeWounds(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.CAUTERIZEWOUNDS) || hasCooldown(stack, Ability.CAUTERIZEWOUNDS)) {
            return false;
        }
        if (player.getHealth() >= player.getMaxHealth()) {
            return false;
        }

        player.heal(6.0F);
        addCooldownFromParams(player, stack, Ability.CAUTERIZEWOUNDS, false, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        spawnRandomEyeParticles(world, player, EnumParticleTypes.FLAME, 10);
        damageTool(stack, player, Ability.CAUTERIZEWOUNDS);
        return true;
    }

    private static boolean invulnerability(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.INVULNERABILITY) || hasCooldown(stack, Ability.INVULNERABILITY)) {
            return false;
        }

        addCooldownFromParams(player, stack, Ability.INVULNERABILITY, true, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        damageTool(stack, player, Ability.INVULNERABILITY);
        return false;
    }

    private static boolean groundstomp(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.GROUNDSTOMP) || hasCooldown(stack, Ability.GROUNDSTOMP)) {
            return false;
        }

        addCooldownFromParams(player, stack, Ability.GROUNDSTOMP, false, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        int strength = getToolValue(stack, Ability.GROUNDSTOMP);
        AxisAlignedBB area = new AxisAlignedBB(player.posX - GROUNDSTOMP_RADIUS, player.posY - GROUNDSTOMP_RADIUS, player.posZ - GROUNDSTOMP_RADIUS,
                player.posX + GROUNDSTOMP_RADIUS, player.posY + GROUNDSTOMP_RADIUS, player.posZ + GROUNDSTOMP_RADIUS);
        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, area, AbilityMethods::isValidStompEntity);
        for (EntityLiving mob : mobs) {
            mob.knockBack(player, strength, player.posX - mob.posX, player.posZ - mob.posZ);
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.5F, 1.0F);
        spawnParticles(world, EnumParticleTypes.BLOCK_DUST, player.posX, player.posY, player.posZ, 20, 0.5D, 0.2D, 0.5D, 0.0D, Block.getStateId(Blocks.DIRT.getDefaultState()));
        damageTool(stack, player, Ability.GROUNDSTOMP);
        return false;
    }

    private static boolean stupefy(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.STUPEFY) || hasCooldown(stack, Ability.STUPEFY)) {
            return false;
        }

        Entity target = getEntityLookedAt(world, player, STUPEFY_RANGE);
        if (!(target instanceof EntityLiving)) {
            return false;
        }

        EntityLiving livingTarget = (EntityLiving) target;
        livingTarget.setAttackTarget(null);
        addStupefyTarget(stack, livingTarget.getUniqueID().toString());
        addCooldownFromParams(player, stack, Ability.STUPEFY, true, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ILLAGER_CAST_SPELL, SoundCategory.PLAYERS, 0.5F, 0.75F);
        spawnParticles(world, EnumParticleTypes.SMOKE_NORMAL, target.posX, target.posY + target.height * 0.75D, target.posZ, 20, 0.25D, 0.2D, 0.25D, 0.0D);
        damageTool(stack, player, Ability.STUPEFY);
        return false;
    }

    private static boolean polymorphRandom(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.POLYMORPH_RANDOM)) {
            return false;
        }

        Entity entity = getEntityLookedAt(world, player, POLYMORPH_RANGE);
        if (!(entity instanceof EntityLiving)) {
            return false;
        }

        boolean peaceful = JDTEntityGroups.isPolymorphicPeaceful(getEntityId(entity));
        if (!peaceful && !JDTEntityGroups.isPolymorphicHostile(getEntityId(entity))) {
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.invalidpolymorphentity"), true);
            return false;
        }
        if (!hasEnoughPolymorphicFluid(player, stack, RANDOM_POLYMORPH_FLUID_COST)) {
            return false;
        }

        String replacementId = getRandomMobTypeByCategory(world, peaceful);
        if (replacementId.isEmpty() || !spawnReplacementMob(world, (EntityLiving) entity, replacementId)) {
            return false;
        }

        consumePolymorphicFluid(player, stack, RANDOM_POLYMORPH_FLUID_COST);
        world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ILLAGER_CAST_SPELL, SoundCategory.PLAYERS, 0.5F, 0.75F);
        spawnParticles(world, EnumParticleTypes.SMOKE_NORMAL, entity.posX, entity.posY + entity.height * 0.75D, entity.posZ, 20, 0.25D, 0.2D, 0.25D, 0.0D);
        damageTool(stack, player, Ability.POLYMORPH_RANDOM);
        return false;
    }

    private static boolean polymorphTarget(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.POLYMORPH_TARGET)) {
            return false;
        }

        Entity entity = getEntityLookedAt(world, player, POLYMORPH_RANGE);
        if (!(entity instanceof EntityLiving)) {
            return false;
        }

        String replacementId = getSavedPolymorphTarget(stack);
        if (replacementId.isEmpty() || JDTEntityGroups.isPolymorphicTargetDenied(replacementId)) {
            return false;
        }
        if (!hasEnoughPolymorphicFluid(player, stack, TARGET_POLYMORPH_FLUID_COST)) {
            return false;
        }
        if (!spawnReplacementMob(world, (EntityLiving) entity, replacementId)) {
            return false;
        }

        consumePolymorphicFluid(player, stack, TARGET_POLYMORPH_FLUID_COST);
        world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ILLAGER_CAST_SPELL, SoundCategory.PLAYERS, 0.5F, 0.75F);
        spawnParticles(world, EnumParticleTypes.SMOKE_NORMAL, entity.posX, entity.posY + entity.height * 0.75D, entity.posZ, 20, 0.25D, 0.2D, 0.25D, 0.0D);
        damageTool(stack, player, Ability.POLYMORPH_TARGET);
        return false;
    }

    private static boolean glowing(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.GLOWING)) {
            return false;
        }

        BlockPos playerPos = player.getPosition();
        AxisAlignedBB searchArea = new AxisAlignedBB(playerPos).grow(GLOWING_RADIUS);
        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, searchArea);
        for (EntityLiving mob : mobs) {
            mob.addPotionEffect(new PotionEffect(MobEffects.GLOWING, GLOWING_DURATION_TICKS, 0));
        }

        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_XYLOPHONE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        damageTool(stack, player, Ability.GLOWING);
        return true;
    }

    private static boolean debuffRemover(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.DEBUFFREMOVER) || hasCooldown(stack, Ability.DEBUFFREMOVER)) {
            return false;
        }

        addCooldownFromParams(player, stack, Ability.DEBUFFREMOVER, false, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        List<Potion> harmfulEffects = new ArrayList<>();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            Potion potion = effect.getPotion();
            if (potion.isBadEffect()) {
                harmfulEffects.add(potion);
            }
        }
        for (Potion potion : harmfulEffects) {
            if (!canUseAbilityAndDurability(stack, Ability.DEBUFFREMOVER)) {
                break;
            }
            player.removePotionEffect(potion);
            damageTool(stack, player, Ability.DEBUFFREMOVER);
        }
        return false;
    }

    private static boolean earthquake(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.EARTHQUAKE) || hasCooldown(stack, Ability.EARTHQUAKE)) {
            return false;
        }

        addCooldownFromParams(player, stack, Ability.EARTHQUAKE, true, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0F, 0.5F);
        AxisAlignedBB area = new AxisAlignedBB(player.posX - EARTHQUAKE_RADIUS, player.posY - EARTHQUAKE_RADIUS, player.posZ - EARTHQUAKE_RADIUS,
                player.posX + EARTHQUAKE_RADIUS, player.posY + EARTHQUAKE_RADIUS, player.posZ + EARTHQUAKE_RADIUS);
        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, area, AbilityMethods::isValidEarthquakeEntity);
        for (EntityLiving mob : mobs) {
            if (!canUseAbilityAndDurability(stack, Ability.EARTHQUAKE)) {
                break;
            }
            mob.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, EARTHQUAKE_SLOW_DURATION_TICKS, EARTHQUAKE_SLOW_AMPLIFIER));
            spawnParticles(world, EnumParticleTypes.END_ROD, mob.posX, mob.posY + mob.height * 0.5D, mob.posZ, 20, 0.25D, 0.2D, 0.25D, 0.0D);
            spawnParticles(world, EnumParticleTypes.ENCHANTMENT_TABLE, mob.posX, mob.posY + mob.height * 0.5D, mob.posZ, 20, 0.5D, 0.2D, 0.5D, 0.0D);
            damageTool(stack, player, Ability.EARTHQUAKE);
        }
        return false;
    }

    private static boolean noAI(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.NOAI) || hasCooldown(stack, Ability.NOAI)) {
            return false;
        }

        addCooldownFromParams(player, stack, Ability.NOAI, false, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        AxisAlignedBB area = new AxisAlignedBB(player.posX - NOAI_RADIUS, player.posY - NOAI_RADIUS, player.posZ - NOAI_RADIUS,
                player.posX + NOAI_RADIUS, player.posY + NOAI_RADIUS, player.posZ + NOAI_RADIUS);
        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, area, AbilityMethods::isValidNoAiEntity);
        for (EntityLiving mob : mobs) {
            if (!canUseAbilityAndDurability(stack, Ability.NOAI)) {
                break;
            }
            mob.setNoAI(true);
            spawnParticles(world, EnumParticleTypes.END_ROD, mob.posX, mob.posY + mob.height * 0.75D, mob.posZ, 20, 0.25D, 0.2D, 0.25D, 0.0D);
            spawnParticles(world, EnumParticleTypes.ENCHANTMENT_TABLE, mob.posX, mob.posY + mob.height * 0.75D, mob.posZ, 20, 0.5D, 0.2D, 0.5D, 0.0D);
            damageTool(stack, player, Ability.NOAI);
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 0.5F);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 1.0F, 0.25F);
        return false;
    }

    private static boolean isValidStompEntity(Entity entity) {
        return !isMultipartEntity(entity);
    }

    private static boolean isValidNoAiEntity(Entity entity) {
        return !isMultipartEntity(entity) && !JDTEntityGroups.isNoAiDenied(getEntityId(entity));
    }

    private static boolean isValidEarthquakeEntity(Entity entity) {
        return entity.onGround && !isMultipartEntity(entity) && !JDTEntityGroups.isEarthquakeDenied(getEntityId(entity));
    }

    private static boolean isMultipartEntity(Entity entity) {
        return entity instanceof MultiPartEntityPart || entity.getParts() != null;
    }

    private static boolean epicArrow(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.EPICARROW) || hasCooldown(stack, Ability.EPICARROW)
                || ItemJDTBow.isEpicArrowPrimed(stack)) {
            return false;
        }

        ItemJDTBow.setEpicArrowPrimed(stack, true);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.EVOCATION_ILLAGER_PREPARE_SUMMON, SoundCategory.PLAYERS, 1.0F, 0.5F);
        damageTool(stack, player, Ability.EPICARROW);
        return false;
    }

    private static boolean leafbreaker(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing facing, EnumHand hand) {
        if (!canUseAbilityAndDurability(stack, Ability.LEAFBREAKER)) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        if (!isLeafBlock(state)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }

        int maxBreak = getLeafbreakerMaxBreak(stack);
        Set<BlockPos> alsoBreakSet = findLikeBlocks(world, state, pos, maxBreak, LEAFBREAKER_RADIUS);
        ToolMiningAbilityHandler.beginManualDropBatch(pos);
        try {
            for (BlockPos breakPos : alsoBreakSet) {
                if (!canUseAbilityAndDurability(stack, Ability.LEAFBREAKER)) {
                    break;
                }
                IBlockState before = world.getBlockState(breakPos);
                if (!isLeafBlock(before)) {
                    continue;
                }
                if (ToolMiningAbilityHandler.breakBlockForAbilities(world, breakPos, player, stack, false)) {
                    world.notifyBlockUpdate(breakPos, before, world.getBlockState(breakPos), 3);
                    if (world.rand.nextFloat() < 0.1F) {
                        damageTool(stack, player, Ability.LEAFBREAKER);
                    }
                }
            }
        } finally {
            ToolMiningAbilityHandler.finishManualDropBatch(stack, world, pos, player);
        }
        return true;
    }

    private static boolean eclipseGate(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing facing, EnumHand hand) {
        if (world.isRemote) {
            return true;
        }
        if (!hasInstalledAbility(stack, Ability.ECLIPSEGATE)) {
            return false;
        }

        int distance = getToolValue(stack, Ability.ECLIPSEGATE);
        Set<BlockPos> posList = getEclipseGateBlocks(world, pos, facing, distance);
        boolean anyWorked = false;
        for (BlockPos blockPos : posList) {
            if (!canUseAbilityAndDurability(stack, Ability.ECLIPSEGATE)) {
                break;
            }
            IBlockState blockState = world.getBlockState(blockPos);
            if (!world.setBlockState(blockPos, ModContentBlocks.ECLIPSE_GATE_BLOCK.getDefaultState(), 3)) {
                continue;
            }
            world.notifyBlockUpdate(blockPos, blockState, ModContentBlocks.ECLIPSE_GATE_BLOCK.getDefaultState(), 3);
            TileEntity tileEntity = world.getTileEntity(blockPos);
            if (tileEntity instanceof TileEclipseGate) {
                ((TileEclipseGate) tileEntity).setSourceBlock(blockState);
            }
            damageTool(stack, player, Ability.ECLIPSEGATE);
            anyWorked = true;
        }

        if (anyWorked) {
            world.playSound(null, pos, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);
            spawnEclipseGateParticles(world, posList);
        }
        return anyWorked;
    }

    private static boolean decoy(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !canUseAbilityAndDurability(stack, Ability.DECOY) || hasActiveCooldown(stack, Ability.DECOY)) {
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

        addCooldownFromParams(player, stack, Ability.DECOY, true, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.EVOCATION_ILLAGER_PREPARE_SUMMON, SoundCategory.PLAYERS, 1.0F, 1.0F);
        damageTool(stack, player, Ability.DECOY);
        return false;
    }

    private static boolean hasInstalledAbility(ItemStack stack, Ability ability) {
        if (stack.getItem() instanceof ItemToggleableTool) {
            ItemToggleableTool tool = (ItemToggleableTool) stack.getItem();
            return tool.supportsAbility(ability) && tool.hasInstalledAbility(stack, ability);
        }

        return ability != null && (!ability.requiresUpgrade() || readStackState(stack).hasInstalledAbility(ability.getId()));
    }

    public static boolean canUseAbilityAndDurability(ItemStack stack, Ability ability) {
        return canUseAbilityAndDurability(stack, ability, 1);
    }

    public static boolean canUseAbilityAndDurability(ItemStack stack, Ability ability, int multiplier) {
        if (!canUseAbility(stack, ability)) {
            return false;
        }
        return testUseTool(stack, ability, multiplier) >= 0;
    }

    public static boolean canUseAbility(ItemStack stack, Ability ability) {
        if (stack == null || stack.isEmpty() || ability == null) {
            return false;
        }
        if (stack.getItem() instanceof ToggleableTool) {
            ToggleableTool tool = (ToggleableTool) stack.getItem();
            return tool.supportsAbility(ability) && tool.hasInstalledAbility(stack, ability) && tool.getSetting(stack, ability);
        }
        return hasInstalledAbility(stack, ability);
    }

    public static int testUseTool(ItemStack stack, Ability ability) {
        return testUseTool(stack, ability, 1);
    }

    public static int testUseTool(ItemStack stack, Ability ability, int multiplier) {
        if (stack == null || stack.isEmpty() || ability == null) {
            return -1;
        }
        int cost = ability.getDurabilityCost() * Math.max(1, multiplier);
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            return energyStorage.getEnergyStored() - ability.getFeCost() * Math.max(1, multiplier);
        }
        if (!stack.isItemStackDamageable()) {
            return 1;
        }
        return stack.getMaxDamage() - stack.getItemDamage() - cost;
    }

    public static void damageTool(ItemStack stack, EntityLivingBase player, Ability ability) {
        damageTool(stack, player, ability, 1);
    }

    public static void damageTool(ItemStack stack, EntityLivingBase player, Ability ability, int multiplier) {
        if (player instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) player;
            if (entityPlayer.capabilities.isCreativeMode || entityPlayer.isSpectator()) {
                return;
            }
        }
        int count = Math.max(1, multiplier);
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            int energyCost = PoweredEnergyCostHelper.afterUnbreakingDiscount(stack, ability.getFeCost() * count);
            energyStorage.extractEnergy(energyCost, false);
            return;
        }
        ItemStack before = stack.copy();
        stack.damageItem(ability.getDurabilityCost() * count, player);
        if (player instanceof EntityPlayer && stack.isEmpty() && !before.isEmpty()) {
            ForgeEventFactory.onPlayerDestroyItem((EntityPlayer) player, before, EnumHand.MAIN_HAND);
        }
    }

    private static boolean hasActiveCooldown(ItemStack stack, Ability ability) {
        return AbilityCooldownTracker.hasActiveCooldown(stack, ability);
    }

    private static boolean hasCooldown(ItemStack stack, Ability ability) {
        return AbilityCooldownTracker.hasCooldown(stack, ability);
    }

    private static int getToolValue(ItemStack stack, Ability ability) {
        if (stack.getItem() instanceof ToggleableTool) {
            return ((ToggleableTool) stack.getItem()).getToolValue(stack, ability);
        }
        AbilityParams params = new AbilityParams(-1, -1, 1);
        return readStackState(stack).getAbilityValues().getOrDefault(ability.getId(), params.defaultValue);
    }

    private static void addCooldownFromParams(EntityPlayer player, ItemStack stack, Ability ability, boolean active, int fallbackTicks) {
        int ticks = fallbackTicks;
        if (stack.getItem() instanceof ToggleableTool) {
            AbilityParams params = ((ToggleableTool) stack.getItem()).getAbilityParams(ability);
            int configured = active ? params.activeCooldown : params.cooldown;
            if (configured > 0) {
                ticks = configured;
            }
        }

        AbilityCooldownTracker.addCooldown(player, stack, ability, ticks, active);
    }

    public static List<String> getStupefyTargets(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return Collections.emptyList();
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(JDTDataKeys.STUPEFY_TARGETS, Constants.NBT.TAG_LIST)) {
            return Collections.emptyList();
        }

        NBTTagList list = tag.getTagList(JDTDataKeys.STUPEFY_TARGETS, Constants.NBT.TAG_STRING);
        List<String> targets = new ArrayList<>();
        for (int i = 0; i < list.tagCount(); i++) {
            targets.add(list.getStringTagAt(i));
        }
        return targets;
    }

    public static void addStupefyTarget(ItemStack stack, String entityUuid) {
        if (stack == null || stack.isEmpty() || entityUuid == null || entityUuid.isEmpty()) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        NBTTagList list = tag.getTagList(JDTDataKeys.STUPEFY_TARGETS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < list.tagCount(); i++) {
            if (entityUuid.equals(list.getStringTagAt(i))) {
                return;
            }
        }
        list.appendTag(new NBTTagString(entityUuid));
        tag.setTag(JDTDataKeys.STUPEFY_TARGETS, list);
    }

    public static void clearStupefyTargets(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }
        stack.getTagCompound().removeTag(JDTDataKeys.STUPEFY_TARGETS);
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

    private static Set<BlockPos> findLawnmowerBlocks(World world, BlockPos start, int maxBreak, int radius) {
        Set<BlockPos> foundBlocks = new HashSet<>();
        Queue<BlockPos> blocksToCheck = new LinkedList<>();
        Set<BlockPos> checkedBlocks = new HashSet<>();

        blocksToCheck.add(start);
        while (!blocksToCheck.isEmpty()) {
            BlockPos posToCheck = blocksToCheck.poll();
            if (!checkedBlocks.add(posToCheck)) {
                continue;
            }

            for (BlockPos pos : BlockPos.getAllInBox(posToCheck.add(-radius, -radius, -radius), posToCheck.add(radius, radius, radius))) {
                BlockPos immutablePos = pos.toImmutable();
                if (!isLawnmowerBlock(world.getBlockState(immutablePos))) {
                    continue;
                }
                if (foundBlocks.size() >= maxBreak) {
                    return foundBlocks;
                }
                if (foundBlocks.add(immutablePos) && !checkedBlocks.contains(immutablePos)) {
                    blocksToCheck.add(immutablePos);
                }
            }
        }
        return foundBlocks;
    }

    private static BlockPos getPlayerOnPos(EntityPlayer player) {
        return new BlockPos(player.posX, player.posY - 0.2D, player.posZ);
    }

    private static List<ItemStack> collectBlockDrops(World world, BlockPos pos, IBlockState state, int fortune) {
        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, world, pos, state, fortune);
        return drops;
    }

    public static void handleAbilityDrops(ItemStack stack, World world, BlockPos dropPos, EntityPlayer player, List<ItemStack> drops) {
        handleAbilityDrops(stack, world, dropPos, player, drops, Collections.singleton(dropPos.toImmutable()));
    }

    public static void handleAbilityDrops(ItemStack stack, World world, BlockPos dropPos, EntityPlayer player, List<ItemStack> drops, Set<BlockPos> breakPositions) {
        if (drops.isEmpty()) {
            return;
        }

        if (canUseAbilityAndDurability(stack, Ability.SMELTER)) {
            SmeltResult smeltResult = smeltAbilityDrops(stack, player, drops);
            drops = compactDrops(smeltResult.drops);
            if (smeltResult.didSmelt) {
                spawnSmelterParticles(world, breakPositions);
            }
        } else {
            drops = compactDrops(drops);
        }

        if (!drops.isEmpty() && canUseAbilityAndDurability(stack, Ability.DROPTELEPORT)) {
            IItemHandler handler = getBoundDropHandler(stack, world);
            if (handler != null) {
                drops = compactDrops(teleportAbilityDrops(stack, player, handler, drops));
            }
        }

        for (ItemStack drop : drops) {
            if (drop == null || drop.isEmpty()) {
                continue;
            }
            EntityItem itemEntity = new EntityItem(
                    world,
                    dropPos.getX() + 0.5D,
                    dropPos.getY() + 0.5D,
                    dropPos.getZ() + 0.5D,
                    drop.copy()
            );
            world.spawnEntity(itemEntity);
        }
    }

    private static List<ItemStack> compactDrops(List<ItemStack> drops) {
        List<ItemStack> compacted = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (drop == null || drop.isEmpty()) {
                continue;
            }

            ItemStack remaining = drop.copy();
            for (ItemStack existing : compacted) {
                if (!ItemHandlerHelper.canItemStacksStack(existing, remaining)) {
                    continue;
                }

                int moved = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (moved <= 0) {
                    continue;
                }
                existing.grow(moved);
                remaining.shrink(moved);
                if (remaining.isEmpty()) {
                    break;
                }
            }

            while (!remaining.isEmpty()) {
                ItemStack split = remaining.copy();
                split.setCount(Math.min(split.getCount(), split.getMaxStackSize()));
                compacted.add(split);
                remaining.shrink(split.getCount());
            }
        }
        return compacted;
    }

    private static SmeltResult smeltAbilityDrops(ItemStack stack, EntityPlayer player, List<ItemStack> drops) {
        List<ItemStack> smelted = new ArrayList<>();
        boolean didSmelt = false;
        for (ItemStack drop : drops) {
            if (drop == null || drop.isEmpty()) {
                continue;
            }
            ItemStack result = FurnaceRecipes.instance().getSmeltingResult(drop);
            int smeltCount = result.isEmpty() ? 0 : Math.min(drop.getCount(), getAffordableAbilityUses(stack, Ability.SMELTER, drop.getCount()));
            if (!result.isEmpty() && smeltCount > 0) {
                ItemStack resultCopy = result.copy();
                resultCopy.setCount(smeltCount);
                smelted.add(resultCopy);
                damageTool(stack, player, Ability.SMELTER, smeltCount);
                didSmelt = true;
                int unsmeltedCount = drop.getCount() - smeltCount;
                if (unsmeltedCount > 0) {
                    ItemStack unsmelted = drop.copy();
                    unsmelted.setCount(unsmeltedCount);
                    smelted.add(unsmelted);
                }
            } else {
                smelted.add(drop);
            }
        }
        return new SmeltResult(smelted, didSmelt);
    }

    private static int getAffordableAbilityUses(ItemStack stack, Ability ability, int requestedUses) {
        if (requestedUses <= 0 || !canUseAbility(stack, ability)) {
            return 0;
        }
        int cappedUses = requestedUses;
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null && ability.getFeCost() > 0) {
            cappedUses = Math.min(cappedUses, energyStorage.getEnergyStored() / ability.getFeCost());
        }
        if (stack.isItemStackDamageable() && ability.getDurabilityCost() > 0) {
            int remainingDurability = stack.getMaxDamage() - stack.getItemDamage();
            cappedUses = Math.min(cappedUses, remainingDurability / ability.getDurabilityCost());
        }
        return Math.max(0, cappedUses);
    }

    private static void spawnSmelterParticles(World world, Set<BlockPos> breakPositions) {
        if (!(world instanceof WorldServer) || breakPositions == null || breakPositions.isEmpty()) {
            return;
        }
        int iterations = breakPositions.size() > 10 ? 1 : 5;
        WorldServer serverWorld = (WorldServer) world;
        for (int i = 0; i < iterations; i++) {
            for (BlockPos pos : breakPositions) {
                serverWorld.spawnParticle(
                        EnumParticleTypes.FLAME,
                        pos.getX() + world.rand.nextDouble(),
                        pos.getY() + world.rand.nextDouble(),
                        pos.getZ() + world.rand.nextDouble(),
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
    }

    private static void spawnRandomEyeParticles(World world, EntityPlayer player, EnumParticleTypes particle, int count) {
        if (!(world instanceof WorldServer)) {
            return;
        }
        Vec3d eye = player.getPositionEyes(1.0F);
        WorldServer serverWorld = (WorldServer) world;
        for (int i = 0; i < count; i++) {
            serverWorld.spawnParticle(
                    particle,
                    eye.x + world.rand.nextDouble(),
                    eye.y + world.rand.nextDouble(),
                    eye.z + world.rand.nextDouble(),
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private static void spawnEclipseGateParticles(World world, Set<BlockPos> positions) {
        if (!(world instanceof WorldServer) || positions == null || positions.isEmpty()) {
            return;
        }
        int iterations = positions.size() > 10 ? 1 : 5;
        WorldServer serverWorld = (WorldServer) world;
        for (BlockPos pos : positions) {
            for (int i = 0; i < iterations; i++) {
                serverWorld.spawnParticle(
                        EnumParticleTypes.PORTAL,
                        pos.getX() + world.rand.nextDouble(),
                        pos.getY() - 0.5D + world.rand.nextDouble(),
                        pos.getZ() + world.rand.nextDouble(),
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
    }

    private static IItemHandler getBoundDropHandler(ItemStack stack, World world) {
        BoundInventoryHelper.BoundLocation boundLocation = BoundInventoryHelper.getBoundTo(stack);
        if (boundLocation == null || world.getMinecraftServer() == null) {
            return null;
        }
        WorldServer boundWorld = world.getMinecraftServer().getWorld(boundLocation.getDimension());
        if (boundWorld == null) {
            return null;
        }
        TileEntity tileEntity = boundWorld.getTileEntity(boundLocation.getPos());
        if (tileEntity == null) {
            return null;
        }
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, boundLocation.getSide());
    }

    private static List<ItemStack> teleportAbilityDrops(ItemStack stack, EntityPlayer player, IItemHandler handler, List<ItemStack> drops) {
        List<ItemStack> leftovers = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (drop == null || drop.isEmpty()) {
                continue;
            }
            if (!canUseAbilityAndDurability(stack, Ability.DROPTELEPORT)) {
                leftovers.add(drop);
                continue;
            }
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, drop.copy(), false);
            if (remainder.isEmpty()) {
                damageTool(stack, player, Ability.DROPTELEPORT);
            } else {
                leftovers.add(remainder);
            }
        }
        return leftovers;
    }

    private static Set<BlockPos> findLikeBlocks(World world, IBlockState targetState, BlockPos start, int maxBreak, int radius) {
        Set<BlockPos> foundBlocks = new HashSet<>();
        Queue<BlockPos> blocksToCheck = new LinkedList<>();
        Set<BlockPos> checkedBlocks = new HashSet<>();

        foundBlocks.add(start.toImmutable());
        blocksToCheck.add(start);
        while (!blocksToCheck.isEmpty()) {
            BlockPos posToCheck = blocksToCheck.poll();
            if (!checkedBlocks.add(posToCheck)) {
                continue;
            }

            for (BlockPos pos : BlockPos.getAllInBox(posToCheck.add(-radius, -radius, -radius), posToCheck.add(radius, radius, radius))) {
                BlockPos immutablePos = pos.toImmutable();
                IBlockState candidate = world.getBlockState(immutablePos);
                if (candidate.getBlock() != targetState.getBlock() || !isLeafBlock(candidate)) {
                    continue;
                }
                if (foundBlocks.size() >= maxBreak) {
                    return foundBlocks;
                }
                if (foundBlocks.add(immutablePos) && !checkedBlocks.contains(immutablePos)) {
                    blocksToCheck.add(immutablePos);
                }
            }
        }
        return foundBlocks;
    }

    private static boolean isLawnmowerBlock(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.DOUBLE_PLANT) {
            BlockDoublePlant.EnumPlantType variant = state.getValue(BlockDoublePlant.VARIANT);
            return variant == BlockDoublePlant.EnumPlantType.GRASS
                    || variant == BlockDoublePlant.EnumPlantType.FERN;
        }
        return block instanceof BlockTallGrass
                || block instanceof BlockFlower
                || block == Blocks.DEADBUSH;
    }

    private static boolean isLeafBlock(IBlockState state) {
        return state.getBlock() instanceof BlockLeaves || state.getMaterial() == Material.LEAVES;
    }

    private static int getLeafbreakerMaxBreak(ItemStack stack) {
        Item item = stack.getItem();
        String registryPath = item.getRegistryName() == null ? "" : item.getRegistryName().getPath();
        if (registryPath.contains(JDTToolTier.ECLIPSEALLOY.getId())) {
            return 256;
        }
        if (registryPath.contains(JDTToolTier.CELESTIGEM.getId())) {
            return 192;
        }
        if (registryPath.contains(JDTToolTier.BLAZEGOLD.getId())) {
            return 128;
        }
        return 64;
    }

    private static Set<BlockPos> getEclipseGateBlocks(World world, BlockPos clickedPos, EnumFacing clickedFace, int distance) {
        EnumFacing direction = clickedFace.getOpposite();
        BlockPos startPos = clickedPos.add(
                direction.getAxis() == EnumFacing.Axis.X ? 0 : -1,
                direction.getAxis() == EnumFacing.Axis.Y ? 0 : -1,
                direction.getAxis() == EnumFacing.Axis.Z ? 0 : -1
        );
        BlockPos endPos = clickedPos.offset(direction, Math.max(1, distance) - 1).add(
                direction.getAxis() == EnumFacing.Axis.X ? 0 : 1,
                direction.getAxis() == EnumFacing.Axis.Y ? 0 : 1,
                direction.getAxis() == EnumFacing.Axis.Z ? 0 : 1
        );

        Set<BlockPos> posSet = new HashSet<>();
        for (BlockPos blockPos : BlockPos.getAllInBox(startPos, endPos)) {
            if (isValidGateBlock(world, blockPos)) {
                posSet.add(blockPos.toImmutable());
            }
        }
        return posSet;
    }

    private static boolean isValidGateBlock(World world, BlockPos blockPos) {
        if (world.getTileEntity(blockPos) != null) {
            return false;
        }
        IBlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() == ModContentBlocks.ECLIPSE_GATE_BLOCK) {
            return false;
        }
        if (blockState.getMaterial() == Material.AIR) {
            return false;
        }
        if (blockState.getBlockHardness(world, blockPos) < 0) {
            return false;
        }
        if (JDTBlockGroups.isEclipseGateDenied(blockState.getBlock())) {
            return false;
        }
        return true;
    }

    private static boolean hasEnoughPolymorphicFluid(EntityPlayer player, ItemStack stack, int amount) {
        if (player != null && player.capabilities.isCreativeMode) {
            return true;
        }
        if (!(stack.getItem() instanceof FluidBackedItem)) {
            return false;
        }
        FluidBackedItem fluidItem = (FluidBackedItem) stack.getItem();
        return fluidItem.getStoredFluid(stack) >= amount;
    }

    private static boolean consumePolymorphicFluid(EntityPlayer player, ItemStack stack, int amount) {
        if (player != null && player.capabilities.isCreativeMode) {
            return true;
        }
        if (!hasEnoughPolymorphicFluid(player, stack, amount)) {
            return false;
        }
        FluidBackedItem fluidItem = (FluidBackedItem) stack.getItem();
        fluidItem.setStoredFluid(stack, fluidItem.getStoredFluid(stack) - amount);
        return true;
    }

    private static String getSavedPolymorphTarget(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return "";
        }
        return stack.getTagCompound().getString(JDTDataKeys.POLYMORPHIC_TARGET_ENTITY);
    }

    private static String getRandomMobTypeByCategory(World world, boolean peaceful) {
        Set<String> candidates = peaceful
                ? JDTEntityGroups.getPolymorphicPeacefulEntities()
                : JDTEntityGroups.getPolymorphicHostileEntities();
        if (candidates.isEmpty()) {
            return "";
        }
        int index = world.rand.nextInt(candidates.size());
        int current = 0;
        for (String candidate : candidates) {
            if (current == index) {
                return candidate;
            }
            current++;
        }
        return "";
    }

    private static boolean spawnReplacementMob(World world, EntityLiving original, String replacementId) {
        Entity replacement = EntityList.createEntityByIDFromName(new ResourceLocation(replacementId), world);
        if (!(replacement instanceof EntityLiving)) {
            return false;
        }

        EntityLiving newMob = (EntityLiving) replacement;
        newMob.setLocationAndAngles(original.posX, original.posY, original.posZ, original.rotationYaw, original.rotationPitch);
        newMob.onInitialSpawn(world.getDifficultyForLocation(original.getPosition()), null);
        newMob.setHealth(newMob.getMaxHealth());
        if (!world.spawnEntity(newMob)) {
            return false;
        }
        original.setDead();
        return true;
    }

    private static Vec3d getShiftPosition(World world, EntityPlayer player, ItemStack stack) {
        int distance = Math.max(1, getToolValue(stack, Ability.VOIDSHIFT));
        Vec3d eyePosition = player.getPositionEyes(1.0F);
        Vec3d lookVector = player.getLookVec();
        Vec3d endPosition = eyePosition.add(lookVector.scale(distance));
        RayTraceResult result = world.rayTraceBlocks(eyePosition, endPosition, false, true, false);
        if (result == null || result.typeOfHit == RayTraceResult.Type.MISS) {
            return getShapeAdjustedPosition(world, player, endPosition);
        }
        return getShapeAdjustedPosition(world, player, result.getBlockPos().down().offset(result.sideHit));
    }

    private static Vec3d getShapeAdjustedPosition(World world, EntityPlayer player, Vec3d missPosition) {
        BlockPos landingPos = new BlockPos(missPosition).down();
        return getShapeAdjustedPosition(world, player, landingPos);
    }

    private static Vec3d getShapeAdjustedPosition(World world, EntityPlayer player, BlockPos landingPos) {
        AxisAlignedBB collisionBox = world.getBlockState(landingPos).getCollisionBoundingBox(world, landingPos);
        double yOffset = collisionBox == null ? 0.0D : collisionBox.maxY;
        return new Vec3d(landingPos.getX() + 0.5D, landingPos.getY() + yOffset, landingPos.getZ() + 0.5D);
    }

    private static Entity getEntityLookedAt(World world, EntityPlayer player, double maxDistance) {
        Vec3d eyePosition = player.getPositionEyes(1.0F);
        Vec3d lookVector = player.getLookVec();
        Vec3d endPosition = eyePosition.add(lookVector.scale(maxDistance));
        RayTraceResult blockHit = world.rayTraceBlocks(eyePosition, endPosition, false, true, false);
        if (blockHit != null && blockHit.typeOfHit != RayTraceResult.Type.MISS) {
            endPosition = blockHit.hitVec;
        }

        Entity closestEntity = null;
        double closestDistance = maxDistance;
        AxisAlignedBB searchBox = player.getEntityBoundingBox()
                .expand(lookVector.x * maxDistance, lookVector.y * maxDistance, lookVector.z * maxDistance)
                .grow(1.0D, 1.0D, 1.0D);
        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(player, searchBox)) {
            if (!entity.canBeCollidedWith()) {
                continue;
            }
            AxisAlignedBB entityBox = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            RayTraceResult entityHit = entityBox.calculateIntercept(eyePosition, endPosition);
            if (entityBox.contains(eyePosition)) {
                if (closestDistance >= 0.0D) {
                    closestEntity = entity;
                    closestDistance = 0.0D;
                }
            } else if (entityHit != null) {
                double distance = eyePosition.distanceTo(entityHit.hitVec);
                if (distance < closestDistance || closestDistance == 0.0D) {
                    closestEntity = entity;
                    closestDistance = distance;
                }
            }
        }
        return closestEntity;
    }

    private static String getEntityId(Entity entity) {
        ResourceLocation id = EntityList.getKey(entity);
        return id == null ? "" : id.toString();
    }

    private static void spawnParticles(
            World world,
            EnumParticleTypes particle,
            double x,
            double y,
            double z,
            int count,
            double xOffset,
            double yOffset,
            double zOffset,
            double speed,
            int... parameters
    ) {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(particle, x, y, z, count, xOffset, yOffset, zOffset, speed, parameters);
        }
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

    private static final class SmeltResult {
        private final List<ItemStack> drops;
        private final boolean didSmelt;

        private SmeltResult(List<ItemStack> drops, boolean didSmelt) {
            this.drops = drops;
            this.didSmelt = didSmelt;
        }
    }
}
