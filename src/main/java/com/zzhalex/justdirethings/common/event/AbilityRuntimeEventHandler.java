package com.zzhalex.justdirethings.common.event;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityCooldownTracker;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import com.zzhalex.justdirethings.common.item.base.BoundInventoryHelper;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTArmor;
import com.zzhalex.justdirethings.data.tool.ToolState;
import com.zzhalex.justdirethings.registry.ModContentItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AbilityRuntimeEventHandler {

    public static final AbilityRuntimeEventHandler INSTANCE = new AbilityRuntimeEventHandler();
    private static final Map<UUID, Float> STEP_HEIGHT_BASES = new HashMap<>();
    private static final Map<UUID, Boolean> FLIGHT_BASES = new HashMap<>();
    private static final Map<UUID, ArmorBreakSnapshot> ARMOR_BREAK_SNAPSHOTS = new HashMap<>();

    private AbilityRuntimeEventHandler() {
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null) {
            return;
        }

        applyPassiveTickAbilities(event.player);
        if (!event.player.world.isRemote) {
            applyFlight(event.player);
            for (ItemStack stack : inventoryStacks(event.player)) {
                AbilityCooldownTracker.tickCooldowns(stack, event.player);
            }
            AbilityCooldownTracker.syncEquippedCooldowns(event.player, false);
            resolveArmorBreakSnapshot(event.player);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.world.isRemote || !tryBlockDamage(player, event.getSource())) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        ItemStack boots = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        if (!canUseAbilityAndDurability(boots, Ability.JUMPBOOST) || player.isInWater() || player.isElytraFlying()) {
            return;
        }

        ToggleableTool tool = (ToggleableTool) boots.getItem();
        float speed = tool.getToolValue(boots, Ability.JUMPBOOST) / 7.5F;
        player.motionY += speed;
        player.velocityChanged = true;
    }

    @SubscribeEvent
    public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (!(event.getTarget() instanceof EntityPlayer) || !(event.getEntityLiving() instanceof EntityLiving)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getTarget();
        EntityLiving source = (EntityLiving) event.getEntityLiving();
        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (canUseAbilityAndDurability(helmet, Ability.STUPEFY)
                && hasActiveCooldown(helmet, Ability.STUPEFY)
                && AbilityMethods.getStupefyTargets(helmet).contains(source.getUniqueID().toString())) {
            source.setAttackTarget(null);
            event.setNewTarget(null);
            event.setCanceled(true);
            return;
        }

        if (!canUseAbilityAndDurability(helmet, Ability.MINDFOG)) {
            return;
        }

        IAttributeInstance followRange = source.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        double defaultRange = followRange == null ? 16.0D : followRange.getAttributeValue();
        if (source.getDistance(player) > defaultRange / getMindFogDenominator(helmet)) {
            source.setAttackTarget(null);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (supportsAbility(player.getHeldItemMainhand(), Ability.AIRBURST)
                || supportsAbility(player.getHeldItemOffhand(), Ability.AIRBURST)) {
            event.setDistance(0.0F);
            return;
        }

        ItemStack boots = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        if (canUseAbilityAndDurability(boots, Ability.NEGATEFALLDAMAGE)) {
            int distance = (int) event.getDistance();
            AbilityMethods.damageTool(boots, player, Ability.NEGATEFALLDAMAGE, distance);
            event.setDistance(0.0F);
            return;
        }

        if (!canUseAbilityAndDurability(boots, Ability.JUMPBOOST)) {
            return;
        }

        ToggleableTool tool = (ToggleableTool) boots.getItem();
        event.setDistance(Math.max(0.0F, event.getDistance() - tool.getToolValue(boots, Ability.JUMPBOOST)));
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote || event.getAmount() <= 0.0F) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        ArmorBreakSnapshot snapshot = createArmorBreakSnapshot(player, getVanillaArmorDamage(event.getAmount()));
        if (snapshot != null) {
            ARMOR_BREAK_SNAPSHOTS.put(player.getUniqueID(), snapshot);
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && !event.getEntityLiving().world.isRemote) {
            resolveArmorBreakSnapshot((EntityPlayer) event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof ToggleableTool)) {
            return;
        }

        if (canUseAbilityAndDurability(mainHand, Ability.SMOKER)) {
            for (EntityItem itemEntity : event.getDrops()) {
                if (smokeDrop(player, mainHand, itemEntity) && player.world instanceof WorldServer) {
                    spawnSmokerParticles((WorldServer) player.world, itemEntity);
                }
            }
        }

        if (canUseAbility(mainHand, Ability.DROPTELEPORT)) {
            teleportLivingDrops(event, player, mainHand);
            if (event.getDrops().isEmpty()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (hasCooldown(chest, Ability.DEATHPROTECTION) || !canUseAbilityAndDurability(chest, Ability.DEATHPROTECTION)) {
            return;
        }

        ToggleableTool tool = (ToggleableTool) chest.getItem();
        AbilityCooldownTracker.addCooldown(player, chest, Ability.DEATHPROTECTION, cooldownTicks(tool, Ability.DEATHPROTECTION, false, 6000), false);
        AbilityMethods.damageTool(chest, player, Ability.DEATHPROTECTION);
        player.setHealth(Math.min(10.0F, player.getMaxHealth()));
        player.extinguish();
        player.fallDistance = 0.0F;
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        clearPlayerPassiveState(event.getOriginal());
        AbilityCooldownTracker.forgetPlayer(event.getOriginal());
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        clearPlayerPassiveState(event.player);
        AbilityCooldownTracker.forgetPlayer(event.player);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        clearPlayerPassiveState(event.player);
        AbilityCooldownTracker.forgetPlayer(event.player);
    }

    @SubscribeEvent
    public void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        dropInstalledUpgrades(event.getOriginal(), event.getEntityPlayer(), event.getHand());
    }

    private static void applyPassiveTickAbilities(EntityPlayer player) {
        ItemStack leggings = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        if (canUseAbility(leggings, Ability.RUNSPEED)
                && player.isSprinting()
                && !player.isElytraFlying()
                && player.moveForward > 0.0F
                && !player.isInWater()) {
            float speed = ((ToggleableTool) leggings.getItem()).getToolValue(leggings, Ability.RUNSPEED) / 25.0F;
            if (!player.onGround) {
                speed = speed / 4.0F;
            }
            player.moveRelative(0.0F, 0.0F, 1.0F, speed);
        }

        if (canUseAbility(leggings, Ability.WALKSPEED)
                && !player.isSprinting()
                && canApplyWalkSpeed(player)
                && player.moveForward > 0.0F
                && !player.isInWater()) {
            float speed = ((ToggleableTool) leggings.getItem()).getToolValue(leggings, Ability.WALKSPEED) / 25.0F;
            if (!player.onGround) {
                speed = speed / 4.0F;
            }
            player.moveRelative(0.0F, 0.0F, 1.0F, speed);
        }

        if (canUseAbility(leggings, Ability.SWIMSPEED)
                && player.fallDistance <= 0.0F
                && !player.isElytraFlying()
                && player.moveForward > 0.0F
                && player.isInWater()) {
            float speed = ((ToggleableTool) leggings.getItem()).getToolValue(leggings, Ability.SWIMSPEED) / 50.0F;
            player.moveRelative(0.0F, 0.0F, 1.0F, speed);
        }

        applyStepHeight(player);
        applyExtinguish(player);
        applyWaterBreathing(player);
    }

    private static boolean canApplyWalkSpeed(EntityPlayer player) {
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        boolean canBoostElytra = canUseAbilityAndDurability(chest, Ability.ELYTRA);
        return canBoostElytra || (player.fallDistance <= 0.0F && !player.isElytraFlying());
    }

    private static void applyExtinguish(EntityPlayer player) {
        if (player.world.isRemote || player.capabilities.isCreativeMode || !player.isBurning()) {
            return;
        }

        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!canUseAbilityAndDurability(chest, Ability.EXTINGUISH) || hasCooldown(chest, Ability.EXTINGUISH)) {
            return;
        }

        ToggleableTool tool = (ToggleableTool) chest.getItem();
        AbilityCooldownTracker.addCooldown(player, chest, Ability.EXTINGUISH, tool.getAbilityParams(Ability.EXTINGUISH).cooldown, false);
        player.extinguish();
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 0.5F, 1.0F);
        if (player.world instanceof WorldServer) {
            ((WorldServer) player.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, player.posX, player.posY + 0.8D, player.posZ, 20, 0.5D, 0.75D, 0.5D, 0.0D);
        }
        AbilityMethods.damageTool(chest, player, Ability.EXTINGUISH);
    }

    private static void applyWaterBreathing(EntityPlayer player) {
        if (!player.isInWater() || player.getAir() >= 150) {
            return;
        }

        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (!canUseAbilityAndDurability(helmet, Ability.WATERBREATHING)) {
            return;
        }

        player.setAir(300);
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, 0.5F, 1.0F);
        AbilityMethods.damageTool(helmet, player, Ability.WATERBREATHING);
    }

    private static void applyFlight(EntityPlayer player) {
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        UUID playerId = player.getUniqueID();
        boolean canFly = canUseAbilityAndDurability(chest, Ability.FLIGHT);
        if (canFly) {
            if (!FLIGHT_BASES.containsKey(playerId)) {
                FLIGHT_BASES.put(playerId, player.capabilities.allowFlying);
            }
            if (!player.capabilities.allowFlying) {
                player.capabilities.allowFlying = true;
                sendPlayerAbilities(player);
            }
            if (player.capabilities.isFlying) {
                AbilityMethods.damageTool(chest, player, Ability.FLIGHT);
            }
            return;
        }

        Boolean originalAllowFlying = FLIGHT_BASES.remove(playerId);
        if (originalAllowFlying != null && player.capabilities.allowFlying != originalAllowFlying) {
            player.capabilities.allowFlying = originalAllowFlying;
            if (!originalAllowFlying) {
                player.capabilities.isFlying = false;
            }
            sendPlayerAbilities(player);
        }
    }

    private static void sendPlayerAbilities(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).sendPlayerAbilities();
        }
    }

    public static boolean requestElytraFlight(EntityPlayerMP player) {
        if (player == null
                || player.onGround
                || player.motionY >= 0.0D
                || player.isElytraFlying()
                || player.isInWater()
                || player.isRiding()
                || player.capabilities.isFlying) {
            return false;
        }

        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!canUseAbilityAndDurability(chest, Ability.ELYTRA)) {
            return false;
        }

        player.setElytraFlying();
        return true;
    }

    private static void clearPlayerPassiveState(EntityPlayer player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueID();
        FLIGHT_BASES.remove(playerId);
        STEP_HEIGHT_BASES.remove(playerId);
        ARMOR_BREAK_SNAPSHOTS.remove(playerId);
    }

    private static int getVanillaArmorDamage(float hurtAmount) {
        int armorDamage = (int) (hurtAmount / 4.0F);
        return Math.max(1, armorDamage);
    }

    private static ArmorBreakSnapshot createArmorBreakSnapshot(EntityPlayer player, int armorDamage) {
        if (player == null || armorDamage <= 0) {
            return null;
        }

        ArmorBreakSnapshot snapshot = new ArmorBreakSnapshot();
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemStackFromSlot(slot);
            if (!shouldRefundArmorUpgradesOnBreak(stack, armorDamage)) {
                continue;
            }
            snapshot.put(slot, stack.copy());
        }
        return snapshot.isEmpty() ? null : snapshot;
    }

    private static boolean shouldRefundArmorUpgradesOnBreak(ItemStack stack, int armorDamage) {
        return stack != null
                && !stack.isEmpty()
                && stack.getItem() instanceof ItemJDTArmor
                && !stack.hasCapability(CapabilityEnergy.ENERGY, null)
                && stack.isItemStackDamageable()
                && stack.getMaxDamage() - stack.getItemDamage() <= armorDamage;
    }

    private static void resolveArmorBreakSnapshot(EntityPlayer player) {
        if (player == null) {
            return;
        }

        ArmorBreakSnapshot snapshot = ARMOR_BREAK_SNAPSHOTS.remove(player.getUniqueID());
        if (snapshot == null || snapshot.isEmpty()) {
            return;
        }

        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack original = snapshot.get(slot);
            if (original == null || original.isEmpty()) {
                continue;
            }
            ItemStack current = player.getItemStackFromSlot(slot);
            if (current.isEmpty() || current.getItem() != original.getItem()) {
                dropInstalledUpgrades(original, player, false);
            }
        }
    }

    private static void applyStepHeight(EntityPlayer player) {
        ItemStack boots = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        UUID playerId = player.getUniqueID();
        if (canUseAbility(boots, Ability.STEPHEIGHT)) {
            float baseStepHeight = STEP_HEIGHT_BASES.containsKey(playerId) ? STEP_HEIGHT_BASES.get(playerId) : player.stepHeight;
            STEP_HEIGHT_BASES.put(playerId, baseStepHeight);
            player.stepHeight = Math.max(player.stepHeight, baseStepHeight + 1.0F);
            return;
        }

        Float baseStepHeight = STEP_HEIGHT_BASES.remove(playerId);
        if (baseStepHeight != null && player.stepHeight <= baseStepHeight + 1.0001F) {
            player.stepHeight = baseStepHeight;
        }
    }

    private static int getMindFogDenominator(ItemStack helmet) {
        String path = helmet.getItem().getRegistryName() == null ? "" : helmet.getItem().getRegistryName().getPath();
        if (path.contains("eclipsealloy")) {
            return 5;
        }
        if (path.contains("celestigem")) {
            return 4;
        }
        if (path.contains("blazegold")) {
            return 3;
        }
        return 2;
    }

    private static int cooldownTicks(ItemStack stack, Ability ability, boolean active, int fallbackTicks) {
        if (stack == null || stack.isEmpty() || ability == null || !(stack.getItem() instanceof ToggleableTool)) {
            return fallbackTicks;
        }
        return cooldownTicks((ToggleableTool) stack.getItem(), ability, active, fallbackTicks);
    }

    private static int cooldownTicks(ToggleableTool tool, Ability ability, boolean active, int fallbackTicks) {
        if (tool == null || ability == null) {
            return fallbackTicks;
        }
        AbilityParams params = tool.getAbilityParams(ability);
        int configured = active ? params.activeCooldown : params.cooldown;
        return configured > 0 ? configured : fallbackTicks;
    }

    private static boolean hasActiveInvulnerability(EntityPlayer player) {
        for (ItemStack stack : equippedStacks(player)) {
            if (hasActiveCooldown(stack, Ability.INVULNERABILITY)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryBlockDamage(EntityPlayer player, DamageSource source) {
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (source == DamageSource.FLY_INTO_WALL && canUseAbilityAndDurability(chest, Ability.ELYTRA)) {
            AbilityMethods.damageTool(chest, player, Ability.ELYTRA);
            return true;
        }
        if (source == DamageSource.IN_WALL && canUsePhase(player)) {
            return true;
        }
        if (isLavaOrFireDamage(source) && canUseAbilityAndDurability(chest, Ability.LAVAIMMUNITY)) {
            AbilityMethods.damageTool(chest, player, Ability.LAVAIMMUNITY);
            return true;
        }
        if (hasActiveInvulnerability(player)) {
            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    private static boolean isLavaOrFireDamage(DamageSource source) {
        return source == DamageSource.LAVA
                || source == DamageSource.IN_FIRE
                || source == DamageSource.ON_FIRE;
    }

    private static boolean hasActiveCooldown(ItemStack stack, Ability ability) {
        if (stack == null || stack.isEmpty() || ability == null) {
            return false;
        }

        return AbilityCooldownTracker.hasActiveCooldown(stack, ability);
    }

    private static boolean canUsePhase(EntityPlayer player) {
        ItemStack leggings = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        return canUseAbility(leggings, Ability.PHASE);
    }

    private static boolean hasCooldown(ItemStack stack, Ability ability) {
        if (stack == null || stack.isEmpty() || ability == null) {
            return false;
        }

        return AbilityCooldownTracker.hasCooldown(stack, ability);
    }

    private static boolean canUseAbility(ItemStack stack, Ability ability) {
        if (stack == null || stack.isEmpty() || ability == null || !(stack.getItem() instanceof ToggleableTool)) {
            return false;
        }
        ToggleableTool tool = (ToggleableTool) stack.getItem();
        return tool.supportsAbility(ability)
                && tool.hasInstalledAbility(stack, ability)
                && tool.getSetting(stack, ability);
    }

    private static boolean supportsAbility(ItemStack stack, Ability ability) {
        return stack != null
                && !stack.isEmpty()
                && stack.getItem() instanceof ToggleableTool
                && ((ToggleableTool) stack.getItem()).supportsAbility(ability);
    }

    private static boolean canUseAbilityAndDurability(ItemStack stack, Ability ability) {
        return stack != null
                && !stack.isEmpty()
                && stack.getItem() instanceof ToggleableTool
                && AbilityMethods.canUseAbilityAndDurability(stack, ability);
    }

    private static boolean smokeDrop(EntityPlayer player, ItemStack tool, EntityItem itemEntity) {
        ItemStack drop = itemEntity.getItem();
        if (drop == null || drop.isEmpty()) {
            return false;
        }

        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(drop);
        if (result.isEmpty() || !AbilityMethods.canUseAbilityAndDurability(tool, Ability.SMOKER, drop.getCount())) {
            return false;
        }

        ItemStack smoked = result.copy();
        smoked.setCount(drop.getCount());
        itemEntity.setItem(smoked);
        AbilityMethods.damageTool(tool, player, Ability.SMOKER, drop.getCount());
        return true;
    }

    private static void spawnSmokerParticles(WorldServer world, EntityItem itemEntity) {
        ItemStack stack = itemEntity.getItem();
        int iterations = stack.getCount() < 10 ? 1 : 5;
        for (int i = 0; i < iterations; i++) {
            world.spawnParticle(
                    EnumParticleTypes.SMOKE_LARGE,
                    itemEntity.posX + world.rand.nextDouble() - 0.5D,
                    itemEntity.posY + world.rand.nextDouble(),
                    itemEntity.posZ + world.rand.nextDouble() - 0.5D,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private static void teleportLivingDrops(LivingDropsEvent event, EntityPlayer player, ItemStack tool) {
        if (!(player.world instanceof WorldServer)) {
            return;
        }
        IItemHandler handler = getBoundHandler((WorldServer) player.world, tool);
        if (handler == null) {
            return;
        }

        Iterator<EntityItem> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            EntityItem itemEntity = iterator.next();
            ItemStack stack = itemEntity.getItem();
            if (stack == null || stack.isEmpty()) {
                iterator.remove();
                continue;
            }
            if (!AbilityMethods.canUseAbilityAndDurability(tool, Ability.DROPTELEPORT)) {
                continue;
            }

            ItemStack leftover = ItemHandlerHelper.insertItemStacked(handler, stack.copy(), false);
            if (leftover.isEmpty()) {
                iterator.remove();
                AbilityMethods.damageTool(tool, player, Ability.DROPTELEPORT);
            } else {
                itemEntity.setItem(leftover);
            }
        }

        if (event.getDrops().isEmpty()) {
            spawnTeleportParticles((WorldServer) player.world, event.getEntityLiving().getPosition());
        }
    }

    private static IItemHandler getBoundHandler(WorldServer world, ItemStack stack) {
        BoundInventoryHelper.BoundLocation boundLocation = BoundInventoryHelper.getBoundTo(stack);
        if (boundLocation == null || world.getMinecraftServer() == null) {
            return null;
        }

        WorldServer boundWorld = world.getMinecraftServer().getWorld(boundLocation.getDimension());
        if (boundWorld == null) {
            return null;
        }

        TileEntity tileEntity = boundWorld.getTileEntity(boundLocation.getPos());
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, boundLocation.getSide())) {
            return null;
        }
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, boundLocation.getSide());
    }

    private static void spawnTeleportParticles(WorldServer world, BlockPos pos) {
        world.spawnParticle(
                EnumParticleTypes.PORTAL,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D,
                50,
                0.5D,
                0.5D,
                0.5D,
                0.1D
        );
    }

    private static List<ItemStack> inventoryStacks(EntityPlayer player) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.addAll(player.inventory.mainInventory);
        stacks.addAll(player.inventory.armorInventory);
        stacks.addAll(player.inventory.offHandInventory);
        return stacks;
    }

    private static List<ItemStack> equippedStacks(EntityPlayer player) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.addAll(player.inventory.armorInventory);
        stacks.add(player.getHeldItemMainhand());
        stacks.add(player.getHeldItemOffhand());
        return stacks;
    }

    private static void dropInstalledUpgrades(ItemStack original, EntityPlayer player, EnumHand hand) {
        dropInstalledUpgrades(original, player, true);
    }

    private static void dropInstalledUpgrades(ItemStack original, EntityPlayer player, boolean requireBroken) {
        if (original == null || original.isEmpty() || player == null || !(original.getItem() instanceof ToggleableTool)) {
            return;
        }
        if (requireBroken && original.isItemStackDamageable() && original.getMaxDamage() - original.getItemDamage() > 1) {
            return;
        }

        ToggleableTool tool = (ToggleableTool) original.getItem();
        ToolState state = ToggleableTool.readToolState(original);
        for (Ability ability : tool.getSupportedAbilities()) {
            if (ability == null || !ability.requiresUpgrade() || !state.hasInstalledAbility(ability.getId())) {
                continue;
            }
            Item upgradeItem = ModContentItems.getItem(ability.getUpgradeItemId());
            if (upgradeItem != null) {
                player.dropItem(new ItemStack(upgradeItem), true);
            }
        }
    }

    private static final EntityEquipmentSlot[] ARMOR_SLOTS = {
            EntityEquipmentSlot.FEET,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.HEAD
    };

    private static final class ArmorBreakSnapshot {
        private final Map<EntityEquipmentSlot, ItemStack> stacks = new HashMap<>();

        void put(EntityEquipmentSlot slot, ItemStack stack) {
            stacks.put(slot, stack);
        }

        ItemStack get(EntityEquipmentSlot slot) {
            return stacks.get(slot);
        }

        boolean isEmpty() {
            return stacks.isEmpty();
        }
    }
}
