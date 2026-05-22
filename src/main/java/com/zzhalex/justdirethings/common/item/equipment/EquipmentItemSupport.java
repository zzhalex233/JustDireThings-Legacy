package com.zzhalex.justdirethings.common.item.equipment;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.capability.item.StackItemCapabilityProvider;
import com.zzhalex.justdirethings.capability.item.StackItemInventoryHandler;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityAvailability;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.BoundInventoryHelper;
import com.zzhalex.justdirethings.common.item.base.EnergyBackedItem;
import com.zzhalex.justdirethings.common.item.base.PoweredEnergyCostHelper;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class EquipmentItemSupport {

    private static final AbilityProfile EMPTY_PROFILE = new AbilityProfile();
    private static final Map<String, AbilityProfile> ABILITY_PROFILES = createAbilityProfiles();
    private static final Map<Item, AbilityProfile> ITEM_PROFILES = new IdentityHashMap<>();
    private static final Map<Item, String> ITEM_IDS = new IdentityHashMap<>();
    private static final int DEFAULT_POWERED_CAPACITY = 10_000;
    private static final int ECLIPSEALLOY_POWERED_CAPACITY = 500_000;
    private static final int POWERED_BLOCK_BREAK_FE_COST = 50;
    private static final int POWERED_ARMOR_DAMAGE_FE_COST = 100;

    private EquipmentItemSupport() {
    }

    static void configure(Item item, String id) {
        item.setRegistryName(Reference.MOD_ID, id);
        item.setTranslationKey(Reference.MOD_ID + "." + id);
        item.setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        ITEM_PROFILES.put(item, ABILITY_PROFILES.getOrDefault(id, EMPTY_PROFILE));
        ITEM_IDS.put(item, id);
    }

    static boolean matchesRepairItem(ItemStack repairStack, ItemStack expectedRepairStack) {
        return !repairStack.isEmpty() && !expectedRepairStack.isEmpty() && ItemStack.areItemsEqual(repairStack, expectedRepairStack);
    }

    static ActionResult<ItemStack> openSettingsIfSneaking(Item item, World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking() && item instanceof ToggleableTool) {
            if (!world.isRemote) {
                player.openGui(JustDireThingsLegacy.INSTANCE, ModContainers.GUI_TOOL_SETTINGS, world, 0, 0, 0);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return null;
    }

    static boolean bindDrops(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing) {
        if (player == null || world == null || pos == null || facing == null || !player.isSneaking()) {
            return false;
        }

        ItemStack stack = player.getHeldItem(hand);
        if (!com.zzhalex.justdirethings.common.item.ability.AbilityMethods.canUseAbility(stack, Ability.DROPTELEPORT)) {
            return false;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            return false;
        }

        if (world.isRemote) {
            return true;
        }

        BoundInventoryHelper.BoundLocation newBinding = new BoundInventoryHelper.BoundLocation(world.provider.getDimension(), pos, facing);
        BoundInventoryHelper.BoundLocation existing = BoundInventoryHelper.getBoundTo(stack);
        if (newBinding.equals(existing)) {
            BoundInventoryHelper.removeBoundTo(stack);
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.bindremoved"), true);
            world.playSound(null, player.posX, player.posY, player.posZ, net.minecraft.init.SoundEvents.ENTITY_ENDEREYE_DEATH, net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
        } else {
            BoundInventoryHelper.setBoundTo(stack, newBinding);
            player.sendStatusMessage(new TextComponentTranslation(
                    "justdirethings.boundto",
                    newBinding.getDimensionComponent(),
                    "[" + newBinding.toShortString() + "]"
            ), true);
            world.playSound(null, player.posX, player.posY, player.posZ, net.minecraft.init.SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }

    static Set<Ability> getAbilities(Item item) {
        return ITEM_PROFILES.getOrDefault(item, EMPTY_PROFILE).abilities();
    }

    static Map<Ability, AbilityParams> getAbilityParams(Item item) {
        return ITEM_PROFILES.getOrDefault(item, EMPTY_PROFILE).params();
    }

    static void appendEquipmentTooltip(Item item, ItemStack stack, List<String> tooltip) {
        TooltipHelper.appendFEText(tooltip, getStoredEnergy(stack), getEnergyCapacity(item));
        if (item instanceof ToggleableTool && !((ToggleableTool) item).getSupportedAbilities().isEmpty()) {
            TooltipHelper.appendToolEnabled(stack, tooltip);
            if (GuiScreen.isShiftKeyDown()) {
                TooltipHelper.appendAbilityList(stack, tooltip);
            } else {
                TooltipHelper.appendShiftForInfo(stack, tooltip);
            }
        }
    }

    static ICapabilityProvider initEnergyCapabilities(Item item, ItemStack stack) {
        int capacity = getEnergyCapacity(item);
        return capacity <= 0 ? null : new StackItemCapabilityProvider(stack, new EquipmentEnergy(item), null);
    }

    static ICapabilityProvider initBowCapabilities(Item item, ItemStack stack) {
        EnergyBackedItem energyItem = getEnergyCapacity(item) <= 0 ? null : new EquipmentEnergy(item);
        return new StackItemCapabilityProvider(stack, energyItem, null, new StackItemInventoryHandler(stack, JDTDataKeys.TOOL_CONTENTS, getBowSlotCount(item)));
    }

    static boolean showEnergyBar(Item item, ItemStack stack) {
        int capacity = getEnergyCapacity(item);
        return capacity > 0 && getStoredEnergy(stack) < capacity;
    }

    static double getEnergyDurabilityForDisplay(Item item, ItemStack stack) {
        int capacity = getEnergyCapacity(item);
        if (capacity <= 0) {
            return 0.0D;
        }
        return 1.0D - (Math.max(0, Math.min(capacity, getStoredEnergy(stack))) / (double) capacity);
    }

    static int getEnergyBarColor(Item item, ItemStack stack) {
        int capacity = getEnergyCapacity(item);
        if (capacity <= 0) {
            return 0xFFFFFF;
        }
        float filled = Math.max(0.0F, Math.min(1.0F, getStoredEnergy(stack) / (float) capacity));
        return MathHelper.hsvToRGB(filled / 3.0F, 1.0F, 1.0F);
    }

    static boolean isPowered(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.hasCapability(CapabilityEnergy.ENERGY, null);
    }

    static boolean hasPoweredDurability(ItemStack stack, int vanillaDamageAmount) {
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage == null || vanillaDamageAmount <= 0) {
            return false;
        }
        return energyStorage.getEnergyStored() >= POWERED_BLOCK_BREAK_FE_COST * vanillaDamageAmount;
    }

    static boolean consumePoweredDurability(ItemStack stack, int vanillaDamageAmount) {
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage == null || vanillaDamageAmount <= 0) {
            return false;
        }
        extractPoweredDamageEnergy(stack, energyStorage, POWERED_BLOCK_BREAK_FE_COST * vanillaDamageAmount);
        return true;
    }

    static boolean redirectPoweredArmorDamageToEnergy(ItemStack stack, int requestedDamage) {
        if (!isPowered(stack)) {
            return false;
        }
        int damageIncrease = requestedDamage - stack.getItemDamage();
        if (damageIncrease <= 0) {
            return false;
        }
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage == null) {
            return false;
        }
        extractPoweredDamageEnergy(stack, energyStorage, POWERED_ARMOR_DAMAGE_FE_COST * damageIncrease);
        return true;
    }

    private static void extractPoweredDamageEnergy(ItemStack stack, IEnergyStorage energyStorage, int amount) {
        energyStorage.extractEnergy(PoweredEnergyCostHelper.afterUnbreakingDiscount(stack, amount), false);
    }

    static boolean isCreativePlayer(EntityLivingBase entity) {
        return entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode;
    }

    static Multimap<String, AttributeModifier> getPoweredAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack, Multimap<String, AttributeModifier> originalModifiers) {
        if (slot != EntityEquipmentSlot.MAINHAND || !isPowered(stack) || hasPoweredDurability(stack, 1)) {
            return originalModifiers;
        }
        Multimap<String, AttributeModifier> modifiers = ArrayListMultimap.create(originalModifiers);
        modifiers.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
        return modifiers;
    }

    static Multimap<String, AttributeModifier> getPoweredArmorAttributeModifiers(ItemStack stack, Multimap<String, AttributeModifier> originalModifiers) {
        if (!isPowered(stack) || hasPoweredDurability(stack, 1)) {
            return originalModifiers;
        }
        return ArrayListMultimap.create();
    }

    private static int getEnergyCapacity(Item item) {
        String id = ITEM_IDS.get(item);
        if (id == null) {
            return 0;
        }
        if (id.startsWith("eclipsealloy_") || "bow_eclipsealloy".equals(id)) {
            return ECLIPSEALLOY_POWERED_CAPACITY;
        }
        if (id.startsWith("celestigem_") || "bow_celestigem".equals(id)) {
            return DEFAULT_POWERED_CAPACITY;
        }
        return 0;
    }

    private static int getBowSlotCount(Item item) {
        String id = ITEM_IDS.get(item);
        if ("bow_eclipsealloy".equals(id)) {
            return 4;
        }
        if ("bow_celestigem".equals(id)) {
            return 3;
        }
        if ("bow_blazegold".equals(id)) {
            return 2;
        }
        return 1;
    }

    private static int getStoredEnergy(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.FORGE_ENERGY);
    }

    private static void setStoredEnergy(Item item, ItemStack stack, int storedEnergy) {
        int capacity = getEnergyCapacity(item);
        getOrCreateTag(stack).setInteger(JDTDataKeys.FORGE_ENERGY, Math.max(0, Math.min(capacity, storedEnergy)));
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    private static Map<String, AbilityProfile> createAbilityProfiles() {
        Map<String, AbilityProfile> profiles = new LinkedHashMap<>();

        profile(profiles, "ferricore_sword").add(Ability.MOBSCANNER);
        profile(profiles, "ferricore_pickaxe").add(Ability.ORESCANNER).add(Ability.OREMINER);
        profile(profiles, "ferricore_shovel").add(Ability.SKYSWEEPER).add(Ability.LAWNMOWER);
        profile(profiles, "ferricore_axe").add(Ability.TREEFELLER).add(Ability.LEAFBREAKER);
        profile(profiles, "ferricore_boots").add(Ability.STEPHEIGHT).add(Ability.JUMPBOOST, new AbilityParams(1, 1, 1, 1));
        profile(profiles, "ferricore_leggings").add(Ability.RUNSPEED, new AbilityParams(1, 1, 1)).add(Ability.WALKSPEED, new AbilityParams(1, 1, 1));
        profile(profiles, "ferricore_chestplate").add(Ability.INVULNERABILITY, new AbilityParams(1, 1, 1, 1, 200, 1200));
        profile(profiles, "ferricore_helmet").add(Ability.MINDFOG);
        profile(profiles, "bow_ferricore").add(Ability.POTIONARROW);

        profile(profiles, "blazegold_sword").add(Ability.MOBSCANNER).add(Ability.LAVAREPAIR)
                .add(Ability.CAUTERIZEWOUNDS, new AbilityParams(1, 1, 1, 1, 0, 1200)).add(Ability.SMOKER);
        profile(profiles, "blazegold_pickaxe").add(Ability.ORESCANNER).add(Ability.OREMINER).add(Ability.SMELTER)
                .add(Ability.HAMMER, new AbilityParams(3, 3, 2)).add(Ability.LAVAREPAIR);
        profile(profiles, "blazegold_shovel").add(Ability.SKYSWEEPER).add(Ability.LAWNMOWER).add(Ability.SMELTER)
                .add(Ability.HAMMER, new AbilityParams(3, 3, 2)).add(Ability.LAVAREPAIR);
        profile(profiles, "blazegold_axe").add(Ability.TREEFELLER).add(Ability.LEAFBREAKER).add(Ability.SMELTER).add(Ability.LAVAREPAIR);
        profile(profiles, "blazegold_hoe").add(Ability.LAVAREPAIR).add(Ability.HAMMER, new AbilityParams(3, 3, 2));
        profile(profiles, "blazegold_boots").add(Ability.STEPHEIGHT).add(Ability.JUMPBOOST, new AbilityParams(1, 2, 1, 2))
                .add(Ability.LAVAREPAIR).add(Ability.GROUNDSTOMP, new AbilityParams(1, 2, 1, 2, 0, 200));
        profile(profiles, "blazegold_leggings").add(Ability.RUNSPEED, new AbilityParams(1, 2, 1))
                .add(Ability.WALKSPEED, new AbilityParams(1, 2, 1)).add(Ability.SWIMSPEED, new AbilityParams(1, 2, 1)).add(Ability.LAVAREPAIR);
        profile(profiles, "blazegold_chestplate").add(Ability.INVULNERABILITY, new AbilityParams(1, 1, 1, 1, 200, 1200))
                .add(Ability.LAVAREPAIR).add(Ability.EXTINGUISH, new AbilityParams(1, 1, 1, 1, 0, 200));
        profile(profiles, "blazegold_helmet").add(Ability.MINDFOG).add(Ability.LAVAREPAIR)
                .add(Ability.STUPEFY, new AbilityParams(1, 1, 1, 1, 100, 600));
        profile(profiles, "bow_blazegold").add(Ability.POTIONARROW).add(Ability.SPLASH).add(Ability.LAVAREPAIR);

        profile(profiles, "celestigem_sword").add(Ability.MOBSCANNER)
                .add(Ability.CAUTERIZEWOUNDS, new AbilityParams(1, 1, 1, 1, 0, 1200)).add(Ability.DROPTELEPORT).add(Ability.SMOKER);
        profile(profiles, "celestigem_pickaxe").add(Ability.ORESCANNER).add(Ability.OREMINER).add(Ability.SMELTER)
                .add(Ability.HAMMER, new AbilityParams(3, 5, 2)).add(Ability.DROPTELEPORT);
        profile(profiles, "celestigem_shovel").add(Ability.SKYSWEEPER).add(Ability.LAWNMOWER).add(Ability.SMELTER)
                .add(Ability.HAMMER, new AbilityParams(3, 5, 2)).add(Ability.DROPTELEPORT);
        profile(profiles, "celestigem_axe").add(Ability.TREEFELLER).add(Ability.LEAFBREAKER).add(Ability.SMELTER).add(Ability.DROPTELEPORT);
        profile(profiles, "celestigem_hoe").add(Ability.DROPTELEPORT).add(Ability.HAMMER, new AbilityParams(3, 5, 2));
        profile(profiles, "celestigem_paxel").add(Ability.ORESCANNER).add(Ability.OREMINER).add(Ability.SKYSWEEPER).add(Ability.LAWNMOWER)
                .add(Ability.TREEFELLER).add(Ability.LEAFBREAKER).add(Ability.SMELTER).add(Ability.HAMMER, new AbilityParams(3, 5, 2))
                .add(Ability.DROPTELEPORT);
        profile(profiles, "celestigem_boots").add(Ability.STEPHEIGHT).add(Ability.JUMPBOOST, new AbilityParams(1, 3, 1, 3))
                .add(Ability.GROUNDSTOMP, new AbilityParams(1, 3, 1, 3, 0, 200)).add(Ability.NEGATEFALLDAMAGE);
        profile(profiles, "celestigem_leggings").add(Ability.RUNSPEED, new AbilityParams(1, 3, 1))
                .add(Ability.WALKSPEED, new AbilityParams(1, 3, 1)).add(Ability.SWIMSPEED, new AbilityParams(1, 3, 1))
                .add(Ability.DECOY, new AbilityParams(1, 1, 1, 1, 200, 2400));
        profile(profiles, "celestigem_chestplate").add(Ability.INVULNERABILITY, new AbilityParams(1, 1, 1, 1, 200, 600))
                .add(Ability.EXTINGUISH, new AbilityParams(1, 1, 1, 1, 0, 100)).add(Ability.ELYTRA);
        profile(profiles, "celestigem_helmet").add(Ability.MINDFOG).add(Ability.STUPEFY, new AbilityParams(1, 1, 1, 1, 100, 400))
                .add(Ability.NIGHTVISION).add(Ability.WATERBREATHING);
        profile(profiles, "bow_celestigem").add(Ability.POTIONARROW).add(Ability.SPLASH).add(Ability.LINGERING).add(Ability.HOMING);

        profile(profiles, "eclipsealloy_sword").add(Ability.GLOWING)
                .add(Ability.CAUTERIZEWOUNDS, new AbilityParams(1, 1, 1, 1, 0, 1200)).add(Ability.DROPTELEPORT).add(Ability.SMOKER);
        profile(profiles, "eclipsealloy_pickaxe").add(Ability.OREXRAY).add(Ability.OREMINER).add(Ability.SMELTER)
                .add(Ability.HAMMER, new AbilityParams(3, 7, 2)).add(Ability.DROPTELEPORT).add(Ability.INSTABREAK);
        profile(profiles, "eclipsealloy_shovel").add(Ability.SKYSWEEPER).add(Ability.LAWNMOWER).add(Ability.SMELTER)
                .add(Ability.HAMMER, new AbilityParams(3, 7, 2)).add(Ability.DROPTELEPORT).add(Ability.INSTABREAK);
        profile(profiles, "eclipsealloy_axe").add(Ability.TREEFELLER).add(Ability.LEAFBREAKER).add(Ability.SMELTER)
                .add(Ability.DROPTELEPORT).add(Ability.INSTABREAK);
        profile(profiles, "eclipsealloy_hoe").add(Ability.DROPTELEPORT).add(Ability.HAMMER, new AbilityParams(3, 7, 2));
        profile(profiles, "eclipsealloy_paxel").add(Ability.OREXRAY).add(Ability.OREMINER).add(Ability.SKYSWEEPER).add(Ability.LAWNMOWER)
                .add(Ability.TREEFELLER).add(Ability.LEAFBREAKER).add(Ability.SMELTER).add(Ability.HAMMER, new AbilityParams(3, 7, 2))
                .add(Ability.DROPTELEPORT).add(Ability.INSTABREAK);
        profile(profiles, "eclipsealloy_boots").add(Ability.STEPHEIGHT).add(Ability.JUMPBOOST, new AbilityParams(1, 5, 1, 5))
                .add(Ability.GROUNDSTOMP, new AbilityParams(1, 5, 1, 5, 0, 200)).add(Ability.NEGATEFALLDAMAGE)
                .add(Ability.EARTHQUAKE, new AbilityParams(1, 1, 1, 1, 200, 800));
        profile(profiles, "eclipsealloy_leggings").add(Ability.RUNSPEED, new AbilityParams(1, 5, 1))
                .add(Ability.WALKSPEED, new AbilityParams(1, 5, 1)).add(Ability.SWIMSPEED, new AbilityParams(1, 5, 1))
                .add(Ability.DECOY, new AbilityParams(1, 1, 1, 1, 200, 1200)).add(Ability.PHASE);
        profile(profiles, "eclipsealloy_chestplate").add(Ability.INVULNERABILITY, new AbilityParams(1, 1, 1, 1, 200, 400))
                .add(Ability.EXTINGUISH, new AbilityParams(1, 1, 1, 1, 0, 40)).add(Ability.ELYTRA).add(Ability.FLIGHT)
                .add(Ability.LAVAIMMUNITY).add(Ability.DEATHPROTECTION, new AbilityParams(1, 1, 1, 1, 0, 6000)).add(Ability.TIMEPROTECTION);
        profile(profiles, "eclipsealloy_helmet").add(Ability.MINDFOG).add(Ability.STUPEFY, new AbilityParams(1, 1, 1, 1, 100, 200))
                .add(Ability.NIGHTVISION).add(Ability.NOAI, new AbilityParams(1, 1, 1, 1, 0, 2400))
                .add(Ability.DEBUFFREMOVER, new AbilityParams(1, 1, 1, 1, 0, 400)).add(Ability.WATERBREATHING);
        profile(profiles, "bow_eclipsealloy").add(Ability.POTIONARROW).add(Ability.SPLASH).add(Ability.LINGERING).add(Ability.HOMING)
                .add(Ability.EPICARROW, new AbilityParams(1, 1, 1, 1, 200, 2400)).add(Ability.PHASE);

        return Collections.unmodifiableMap(profiles);
    }

    private static AbilityProfile profile(Map<String, AbilityProfile> profiles, String id) {
        AbilityProfile profile = new AbilityProfile();
        profiles.put(id, profile);
        return profile;
    }

    private static final class AbilityProfile {
        private final EnumSet<Ability> abilities = EnumSet.noneOf(Ability.class);
        private final Map<Ability, AbilityParams> params = new EnumMap<>(Ability.class);

        AbilityProfile add(Ability ability) {
            abilities.add(ability);
            return this;
        }

        AbilityProfile add(Ability ability, AbilityParams abilityParams) {
            add(ability);
            params.put(ability, abilityParams);
            return this;
        }

        Set<Ability> abilities() {
            if (abilities.isEmpty()) {
                return Collections.emptySet();
            }
            EnumSet<Ability> available = EnumSet.noneOf(Ability.class);
            for (Ability ability : abilities) {
                if (AbilityAvailability.isAvailable(ability)) {
                    available.add(ability);
                }
            }
            return available.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(available);
        }

        Map<Ability, AbilityParams> params() {
            return Collections.unmodifiableMap(params);
        }
    }

    private static final class EquipmentEnergy implements EnergyBackedItem {

        private final Item item;

        private EquipmentEnergy(Item item) {
            this.item = item;
        }

        @Override
        public int getStoredEnergy(ItemStack stack) {
            return EquipmentItemSupport.getStoredEnergy(stack);
        }

        @Override
        public void setStoredEnergy(ItemStack stack, int storedEnergy) {
            EquipmentItemSupport.setStoredEnergy(item, stack, storedEnergy);
        }

        @Override
        public int getEnergyCapacity(ItemStack stack) {
            return EquipmentItemSupport.getEnergyCapacity(item);
        }

        @Override
        public int getMaxReceive(ItemStack stack) {
            return getEnergyCapacity(stack);
        }

        @Override
        public int getMaxExtract(ItemStack stack) {
            return getEnergyCapacity(stack);
        }
    }
}
