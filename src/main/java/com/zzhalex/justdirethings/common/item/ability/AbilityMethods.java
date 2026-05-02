package com.zzhalex.justdirethings.common.item.ability;

import com.zzhalex.justdirethings.common.block.group.JDTBlockGroups;
import com.zzhalex.justdirethings.common.entity.group.JDTEntityGroups;
import com.zzhalex.justdirethings.common.entity.EntityDecoy;
import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTBow;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.FluidBackedItem;
import com.zzhalex.justdirethings.common.item.base.ItemToggleableTool;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import com.zzhalex.justdirethings.common.tile.TileEclipseGate;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.data.tool.AbilityCooldown;
import com.zzhalex.justdirethings.data.tool.ToolState;
import com.zzhalex.justdirethings.data.tool.ToolStateIO;
import com.zzhalex.justdirethings.registry.ModContentBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;

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
        if (!hasInstalledAbility(stack, Ability.MOBSCANNER)) {
            return false;
        }
        return scanFor(world, player, stack, Ability.MOBSCANNER);
    }

    private static boolean scanForOreScanner(World world, EntityPlayer player, ItemStack stack) {
        if (!hasInstalledAbility(stack, Ability.ORESCANNER)) {
            return false;
        }
        return scanFor(world, player, stack, Ability.ORESCANNER);
    }

    private static boolean scanForOreXRay(World world, EntityPlayer player, ItemStack stack) {
        if (!hasInstalledAbility(stack, Ability.OREXRAY)) {
            return false;
        }
        return scanFor(world, player, stack, Ability.OREXRAY);
    }

    private static boolean scanFor(World world, EntityPlayer player, ItemStack stack, Ability ability) {
        if (world.isRemote) {
            JustDireThingsLegacy.proxy.discoverThings(player, ability, stack);
            world.playSound(player, player.posX, player.posY, player.posZ,
                    ability == Ability.OREXRAY ? SoundEvents.BLOCK_NOTE_XYLOPHONE : SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
            return false;
        }

        stack.damageItem(ability.getDurabilityCost(), player);
        return false;
    }

    private static boolean lawnmower(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.LAWNMOWER)) {
            return false;
        }

        Set<BlockPos> breakBlocks = findLawnmowerBlocks(world, player.getPosition(), 64, 5);
        boolean brokeAny = false;
        for (BlockPos pos : breakBlocks) {
            if (!hasInstalledAbility(stack, Ability.LAWNMOWER)) {
                break;
            }
            if (world.destroyBlock(pos, true)) {
                brokeAny = true;
                if (world.rand.nextFloat() < 0.1F) {
                    stack.damageItem(Ability.LAWNMOWER.getDurabilityCost(), player);
                }
            }
        }

        if (brokeAny) {
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return brokeAny;
    }

    private static boolean airBurst(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.AIRBURST)) {
            return true;
        }

        int multiplier = getToolValue(stack, Ability.AIRBURST);
        Vec3d look = player.getLookVec();
        double addedStrength = (double) multiplier / 2.0D;
        double burstStrength = 1.5D + addedStrength;
        player.motionX = look.x * burstStrength;
        player.motionY = look.y * burstStrength;
        player.motionZ = look.z * burstStrength;
        player.fallDistance = 0.0F;
        player.velocityChanged = true;
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketEntityVelocity(player));
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.5F, 0.125F);
        stack.damageItem(Ability.AIRBURST.getDurabilityCost() * Math.max(1, multiplier), player);
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
        if (distanceTraveled <= 0) {
            return false;
        }

        player.setPositionAndUpdate(shiftPosition.x, shiftPosition.y, shiftPosition.z);
        player.fallDistance = 0.0F;
        world.playSound(null, shiftPosition.x, shiftPosition.y, shiftPosition.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        stack.damageItem(Ability.VOIDSHIFT.getDurabilityCost() * Math.max(1, distanceTraveled), player);
        return false;
    }

    private static boolean cauterizeWounds(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.CAUTERIZEWOUNDS) || hasCooldown(stack, Ability.CAUTERIZEWOUNDS)) {
            return false;
        }
        if (player.getHealth() >= player.getMaxHealth()) {
            return false;
        }

        player.heal(6.0F);
        addCooldownFromParams(stack, Ability.CAUTERIZEWOUNDS, false, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        spawnParticles(world, EnumParticleTypes.FLAME, player.posX, player.posY + player.getEyeHeight(), player.posZ, 10, 0.5D, 0.5D, 0.5D, 0.0D);
        stack.damageItem(Ability.CAUTERIZEWOUNDS.getDurabilityCost(), player);
        return true;
    }

    private static boolean invulnerability(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.INVULNERABILITY) || hasCooldown(stack, Ability.INVULNERABILITY)) {
            return false;
        }

        addCooldownFromParams(stack, Ability.INVULNERABILITY, true, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        stack.damageItem(Ability.INVULNERABILITY.getDurabilityCost(), player);
        return false;
    }

    private static boolean groundstomp(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.GROUNDSTOMP) || hasCooldown(stack, Ability.GROUNDSTOMP)) {
            return false;
        }

        AxisAlignedBB area = new AxisAlignedBB(player.posX - GROUNDSTOMP_RADIUS, player.posY - GROUNDSTOMP_RADIUS, player.posZ - GROUNDSTOMP_RADIUS,
                player.posX + GROUNDSTOMP_RADIUS, player.posY + GROUNDSTOMP_RADIUS, player.posZ + GROUNDSTOMP_RADIUS);
        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, area);
        if (mobs.isEmpty()) {
            return false;
        }

        int strength = getToolValue(stack, Ability.GROUNDSTOMP);
        addCooldownFromParams(stack, Ability.GROUNDSTOMP, false, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        for (EntityLiving mob : mobs) {
            mob.knockBack(player, strength, player.posX - mob.posX, player.posZ - mob.posZ);
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.5F, 1.0F);
        spawnParticles(world, EnumParticleTypes.BLOCK_DUST, player.posX, player.posY, player.posZ, 20, 0.5D, 0.2D, 0.5D, 0.0D, Block.getStateId(Blocks.DIRT.getDefaultState()));
        stack.damageItem(Ability.GROUNDSTOMP.getDurabilityCost(), player);
        return false;
    }

    private static boolean stupefy(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.STUPEFY) || hasCooldown(stack, Ability.STUPEFY)) {
            return false;
        }

        Entity target = getEntityLookedAt(world, player, STUPEFY_RANGE);
        if (!(target instanceof EntityLiving)) {
            return false;
        }

        ((EntityLiving) target).setAttackTarget(null);
        addCooldownFromParams(stack, Ability.STUPEFY, true, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ILLAGER_CAST_SPELL, SoundCategory.PLAYERS, 0.5F, 0.75F);
        spawnParticles(world, EnumParticleTypes.SMOKE_NORMAL, target.posX, target.posY + target.height * 0.75D, target.posZ, 20, 0.25D, 0.2D, 0.25D, 0.0D);
        stack.damageItem(Ability.STUPEFY.getDurabilityCost(), player);
        return false;
    }

    private static boolean polymorphRandom(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.POLYMORPH_RANDOM)) {
            return false;
        }

        Entity entity = getEntityLookedAt(world, player, POLYMORPH_RANGE);
        if (!(entity instanceof EntityLiving)) {
            return false;
        }

        boolean peaceful = JDTEntityGroups.isPolymorphicPeaceful(getEntityId(entity));
        if (!peaceful && !JDTEntityGroups.isPolymorphicHostile(getEntityId(entity))) {
            return false;
        }
        if (!hasEnoughPolymorphicFluid(stack, RANDOM_POLYMORPH_FLUID_COST)) {
            return false;
        }

        String replacementId = getRandomMobTypeByCategory(world, peaceful);
        if (replacementId.isEmpty() || !spawnReplacementMob(world, (EntityLiving) entity, replacementId)) {
            return false;
        }

        consumePolymorphicFluid(stack, RANDOM_POLYMORPH_FLUID_COST);
        world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ILLAGER_CAST_SPELL, SoundCategory.PLAYERS, 0.5F, 0.75F);
        spawnParticles(world, EnumParticleTypes.SMOKE_NORMAL, entity.posX, entity.posY + entity.height * 0.75D, entity.posZ, 20, 0.25D, 0.2D, 0.25D, 0.0D);
        stack.damageItem(Ability.POLYMORPH_RANDOM.getDurabilityCost(), player);
        return false;
    }

    private static boolean polymorphTarget(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.POLYMORPH_TARGET)) {
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
        if (!hasEnoughPolymorphicFluid(stack, TARGET_POLYMORPH_FLUID_COST)) {
            return false;
        }
        if (!spawnReplacementMob(world, (EntityLiving) entity, replacementId)) {
            return false;
        }

        consumePolymorphicFluid(stack, TARGET_POLYMORPH_FLUID_COST);
        world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ILLAGER_CAST_SPELL, SoundCategory.PLAYERS, 0.5F, 0.75F);
        spawnParticles(world, EnumParticleTypes.SMOKE_NORMAL, entity.posX, entity.posY + entity.height * 0.75D, entity.posZ, 20, 0.25D, 0.2D, 0.25D, 0.0D);
        stack.damageItem(Ability.POLYMORPH_TARGET.getDurabilityCost(), player);
        return false;
    }

    private static boolean glowing(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.GLOWING) || hasCooldown(stack, Ability.GLOWING)) {
            return false;
        }

        BlockPos playerPos = player.getPosition();
        AxisAlignedBB searchArea = new AxisAlignedBB(playerPos).grow(GLOWING_RADIUS);
        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, searchArea);
        for (EntityLiving mob : mobs) {
            mob.addPotionEffect(new PotionEffect(MobEffects.GLOWING, GLOWING_DURATION_TICKS, 0));
        }

        addCooldownFromParams(stack, Ability.GLOWING, false, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_XYLOPHONE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        stack.damageItem(Ability.GLOWING.getDurabilityCost(), player);
        return true;
    }

    private static boolean debuffRemover(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.DEBUFFREMOVER) || hasCooldown(stack, Ability.DEBUFFREMOVER)) {
            return false;
        }

        List<Potion> harmfulEffects = new ArrayList<>();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            Potion potion = effect.getPotion();
            if (potion.isBadEffect()) {
                harmfulEffects.add(potion);
            }
        }
        if (harmfulEffects.isEmpty()) {
            return false;
        }

        addCooldownFromParams(stack, Ability.DEBUFFREMOVER, false, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        for (Potion potion : harmfulEffects) {
            player.removePotionEffect(potion);
            stack.damageItem(Ability.DEBUFFREMOVER.getDurabilityCost(), player);
        }
        return false;
    }

    private static boolean earthquake(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.EARTHQUAKE) || hasCooldown(stack, Ability.EARTHQUAKE)) {
            return false;
        }

        AxisAlignedBB area = new AxisAlignedBB(player.posX - EARTHQUAKE_RADIUS, player.posY - EARTHQUAKE_RADIUS, player.posZ - EARTHQUAKE_RADIUS,
                player.posX + EARTHQUAKE_RADIUS, player.posY + EARTHQUAKE_RADIUS, player.posZ + EARTHQUAKE_RADIUS);
        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, area,
                mob -> mob.onGround && !JDTEntityGroups.isEarthquakeDenied(getEntityId(mob)));
        if (mobs.isEmpty()) {
            return false;
        }

        addCooldownFromParams(stack, Ability.EARTHQUAKE, true, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        for (EntityLiving mob : mobs) {
            mob.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, EARTHQUAKE_SLOW_DURATION_TICKS, EARTHQUAKE_SLOW_AMPLIFIER));
            spawnParticles(world, EnumParticleTypes.END_ROD, mob.posX, mob.posY + mob.height * 0.5D, mob.posZ, 20, 0.25D, 0.2D, 0.25D, 0.0D);
            spawnParticles(world, EnumParticleTypes.ENCHANTMENT_TABLE, mob.posX, mob.posY + mob.height * 0.5D, mob.posZ, 20, 0.5D, 0.2D, 0.5D, 0.0D);
            stack.damageItem(Ability.EARTHQUAKE.getDurabilityCost(), player);
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0F, 0.5F);
        return false;
    }

    private static boolean noAI(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.NOAI) || hasCooldown(stack, Ability.NOAI)) {
            return false;
        }

        AxisAlignedBB area = new AxisAlignedBB(player.posX - NOAI_RADIUS, player.posY - NOAI_RADIUS, player.posZ - NOAI_RADIUS,
                player.posX + NOAI_RADIUS, player.posY + NOAI_RADIUS, player.posZ + NOAI_RADIUS);
        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, area,
                mob -> !JDTEntityGroups.isNoAiDenied(getEntityId(mob)));
        if (mobs.isEmpty()) {
            return false;
        }

        addCooldownFromParams(stack, Ability.NOAI, false, DEFAULT_ACTIVE_COOLDOWN_TICKS);
        for (EntityLiving mob : mobs) {
            mob.setNoAI(true);
            spawnParticles(world, EnumParticleTypes.END_ROD, mob.posX, mob.posY + mob.height * 0.75D, mob.posZ, 20, 0.25D, 0.2D, 0.25D, 0.0D);
            spawnParticles(world, EnumParticleTypes.ENCHANTMENT_TABLE, mob.posX, mob.posY + mob.height * 0.75D, mob.posZ, 20, 0.5D, 0.2D, 0.5D, 0.0D);
            stack.damageItem(Ability.NOAI.getDurabilityCost(), player);
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 0.5F);
        return false;
    }

    private static boolean epicArrow(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.EPICARROW) || hasCooldown(stack, Ability.EPICARROW)
                || ItemJDTBow.isEpicArrowPrimed(stack)) {
            return false;
        }

        ItemJDTBow.setEpicArrowPrimed(stack, true);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.EVOCATION_ILLAGER_PREPARE_SUMMON, SoundCategory.PLAYERS, 1.0F, 0.5F);
        stack.damageItem(Ability.EPICARROW.getDurabilityCost(), player);
        return false;
    }

    private static boolean leafbreaker(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing facing, EnumHand hand) {
        if (world.isRemote || !hasInstalledAbility(stack, Ability.LEAFBREAKER)) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        if (!isLeafBlock(state)) {
            return false;
        }

        int maxBreak = getLeafbreakerMaxBreak(stack);
        Set<BlockPos> alsoBreakSet = findLikeBlocks(world, state, pos, maxBreak, 2);
        boolean brokeAny = false;
        for (BlockPos breakPos : alsoBreakSet) {
            if (!hasInstalledAbility(stack, Ability.LEAFBREAKER)) {
                break;
            }
            IBlockState before = world.getBlockState(breakPos);
            if (!isLeafBlock(before)) {
                continue;
            }
            if (world.destroyBlock(breakPos, true)) {
                world.notifyBlockUpdate(breakPos, before, world.getBlockState(breakPos), 3);
                brokeAny = true;
                if (world.rand.nextFloat() < 0.1F) {
                    stack.damageItem(Ability.LEAFBREAKER.getDurabilityCost(), player);
                }
            }
        }

        if (brokeAny) {
            world.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return brokeAny;
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
            if (!hasInstalledAbility(stack, Ability.ECLIPSEGATE)) {
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
            stack.damageItem(Ability.ECLIPSEGATE.getDurabilityCost(), player);
            anyWorked = true;
        }

        if (anyWorked) {
            world.playSound(null, pos, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return anyWorked;
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
        return false;
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

    private static boolean hasCooldown(ItemStack stack, Ability ability) {
        for (AbilityCooldown cooldown : readStackState(stack).getAbilityCooldowns()) {
            if (ability.getId().equals(cooldown.getAbilityId()) && cooldown.getRemainingTicks() > 0) {
                return true;
            }
        }
        return false;
    }

    private static int getToolValue(ItemStack stack, Ability ability) {
        if (stack.getItem() instanceof ToggleableTool) {
            return ((ToggleableTool) stack.getItem()).getToolValue(stack, ability);
        }
        AbilityParams params = new AbilityParams(-1, -1, 1);
        return readStackState(stack).getAbilityValues().getOrDefault(ability.getId(), params.defaultValue);
    }

    private static void addActiveCooldown(ItemStack stack, Ability ability, int ticks) {
        ToolState state = readStackState(stack);
        state.getAbilityCooldowns().removeIf(cooldown -> ability.getId().equals(cooldown.getAbilityId()));
        state.getAbilityCooldowns().add(new AbilityCooldown(ability.getId(), ticks, true));
        writeStackState(stack, state);
    }

    private static void addCooldownFromParams(ItemStack stack, Ability ability, boolean active, int fallbackTicks) {
        int ticks = fallbackTicks;
        if (stack.getItem() instanceof ToggleableTool) {
            AbilityParams params = ((ToggleableTool) stack.getItem()).getAbilityParams(ability);
            int configured = active ? params.activeCooldown : params.cooldown;
            if (configured > 0) {
                ticks = configured;
            }
        }

        ToolState state = readStackState(stack);
        state.getAbilityCooldowns().removeIf(cooldown -> ability.getId().equals(cooldown.getAbilityId()));
        state.getAbilityCooldowns().add(new AbilityCooldown(ability.getId(), ticks, active));
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

    private static Set<BlockPos> findLikeBlocks(World world, IBlockState targetState, BlockPos start, int maxBreak, int radius) {
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
        Material material = state.getMaterial();
        return block instanceof BlockTallGrass
                || block instanceof BlockFlower
                || block instanceof BlockVine
                || block == Blocks.DEADBUSH
                || block == Blocks.DOUBLE_PLANT
                || material == Material.PLANTS
                || material == Material.VINE;
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

    private static boolean hasEnoughPolymorphicFluid(ItemStack stack, int amount) {
        if (!(stack.getItem() instanceof FluidBackedItem)) {
            return false;
        }
        FluidBackedItem fluidItem = (FluidBackedItem) stack.getItem();
        return fluidItem.getStoredFluid(stack) >= amount;
    }

    private static boolean consumePolymorphicFluid(ItemStack stack, int amount) {
        if (!hasEnoughPolymorphicFluid(stack, amount)) {
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
