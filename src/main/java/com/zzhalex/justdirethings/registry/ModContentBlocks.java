package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.block.goo.BlockGooBlock;
import com.zzhalex.justdirethings.common.block.goo.BlockGooPattern;
import com.zzhalex.justdirethings.common.block.goo.BlockGooSoil;
import com.zzhalex.justdirethings.common.block.BlockEclipseGate;
import com.zzhalex.justdirethings.common.block.BlockRawOre;
import com.zzhalex.justdirethings.common.block.BlockSimpleContent;
import com.zzhalex.justdirethings.common.block.BlockTimeCrystalBudding;
import com.zzhalex.justdirethings.common.block.BlockTimeCrystalCluster;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModContentBlocks {

    private static final Map<String, Block> BLOCKS = new LinkedHashMap<>();

    public static final Block FERRICORE_BLOCK = registerSimple("ferricore_block", Material.IRON, SoundType.METAL, 5.0F, 10.0F, 2);
    public static final Block BLAZEGOLD_BLOCK = registerSimple("blazegold_block", Material.IRON, SoundType.METAL, 5.0F, 10.0F, 2);
    public static final Block CELESTIGEM_BLOCK = registerSimple("celestigem_block", Material.IRON, SoundType.METAL, 5.0F, 10.0F, 2);
    public static final Block ECLIPSEALLOY_BLOCK = registerSimple("eclipsealloy_block", Material.IRON, SoundType.METAL, 6.0F, 12.0F, 3);
    public static final Block COALBLOCK_T1 = registerSimple("coalblock_t1", Material.ROCK, SoundType.STONE, 4.0F, 8.0F, 1);
    public static final Block COALBLOCK_T2 = registerSimple("coalblock_t2", Material.ROCK, SoundType.STONE, 4.0F, 8.0F, 2);
    public static final Block COALBLOCK_T3 = registerSimple("coalblock_t3", Material.ROCK, SoundType.STONE, 5.0F, 8.0F, 2);
    public static final Block COALBLOCK_T4 = registerSimple("coalblock_t4", Material.ROCK, SoundType.STONE, 6.0F, 8.0F, 3);
    public static final Block CHARCOAL = registerSimple("charcoal", Material.ROCK, SoundType.STONE, 4.0F, 8.0F, 1);
    public static final Block GOO_BLOCK_TIER1 = register(new BlockGooBlock("gooblock_tier1", 1));
    public static final Block GOO_BLOCK_TIER2 = register(new BlockGooBlock("gooblock_tier2", 2));
    public static final Block GOO_BLOCK_TIER3 = register(new BlockGooBlock("gooblock_tier3", 3));
    public static final Block GOO_BLOCK_TIER4 = register(new BlockGooBlock("gooblock_tier4", 4));
    public static final Block GOO_PATTERN_BLOCK = register(new BlockGooPattern("goopatternblock"));
    public static final Block GOO_SOIL_TIER1 = register(new BlockGooSoil("goosoil_tier1", 1));
    public static final Block GOO_SOIL_TIER2 = register(new BlockGooSoil("goosoil_tier2", 2));
    public static final Block GOO_SOIL_TIER3 = register(new BlockGooSoil("goosoil_tier3", 3));
    public static final Block GOO_SOIL_TIER4 = register(new BlockGooSoil("goosoil_tier4", 4));
    public static final Block ECLIPSE_GATE_BLOCK = register(new BlockEclipseGate());

    public static final Block RAW_FERRICORE_ORE = registerRawOre("raw_ferricore_ore", Material.ROCK, SoundType.STONE, 3.0F, 5.0F, 1);
    public static final Block RAW_BLAZEGOLD_ORE = registerRawOre("raw_blazegold_ore", Material.ROCK, SoundType.STONE, 3.5F, 5.0F, 2);
    public static final Block RAW_CELESTIGEM_ORE = registerRawOre("raw_celestigem_ore", Material.ROCK, SoundType.STONE, 4.0F, 6.0F, 2);
    public static final Block RAW_ECLIPSEALLOY_ORE = registerRawOre("raw_eclipsealloy_ore", Material.ROCK, SoundType.STONE, 5.0F, 6.0F, 3);
    public static final Block RAW_COAL_T1_ORE = registerRawOre("raw_coal_t1_ore", Material.ROCK, SoundType.STONE, 3.0F, 5.0F, 1);
    public static final Block RAW_COAL_T2_ORE = registerRawOre("raw_coal_t2_ore", Material.ROCK, SoundType.STONE, 3.5F, 5.0F, 2);
    public static final Block RAW_COAL_T3_ORE = registerRawOre("raw_coal_t3_ore", Material.ROCK, SoundType.STONE, 4.0F, 6.0F, 2);
    public static final Block RAW_COAL_T4_ORE = registerRawOre("raw_coal_t4_ore", Material.ROCK, SoundType.STONE, 5.0F, 6.0F, 3);

    public static final Block TIME_CRYSTAL_BLOCK = registerSimple("time_crystal_block", Material.GLASS, SoundType.GLASS, 1.5F, 3.0F, 1);
    public static final Block TIME_CRYSTAL_BUDDING_BLOCK = register(new BlockTimeCrystalBudding());
    public static final Block TIME_CRYSTAL_CLUSTER = register(new BlockTimeCrystalCluster("time_crystal_cluster", 0.1875F, 0.375F));
    public static final Block TIME_CRYSTAL_CLUSTER_SMALL = register(new BlockTimeCrystalCluster("time_crystal_cluster_small", 0.125F, 0.25F));
    public static final Block TIME_CRYSTAL_CLUSTER_MEDIUM = register(new BlockTimeCrystalCluster("time_crystal_cluster_medium", 0.15625F, 0.3125F));
    public static final Block TIME_CRYSTAL_CLUSTER_LARGE = register(new BlockTimeCrystalCluster("time_crystal_cluster_large", 0.21875F, 0.4375F));

    private ModContentBlocks() {
    }

    public static List<String> coreContentBlockIds() {
        return new ArrayList<>(BLOCKS.keySet());
    }

    public static Collection<Block> allBlocks() {
        return Collections.unmodifiableCollection(BLOCKS.values());
    }

    public static Block getBlock(String id) {
        return BLOCKS.get(id);
    }

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        for (Block block : BLOCKS.values()) {
            event.getRegistry().register(block);
        }
    }

    private static Block registerSimple(String id, Material material, SoundType soundType, float hardness, float resistance, int harvestLevel) {
        return register(new BlockSimpleContent(id, material, soundType, hardness, resistance, harvestLevel));
    }

    private static Block registerRawOre(String id, Material material, SoundType soundType, float hardness, float resistance, int harvestLevel) {
        return register(new BlockRawOre(id, material, soundType, hardness, resistance, harvestLevel));
    }

    private static Block register(Block block) {
        BLOCKS.put(block.getRegistryName().getPath(), block);
        return block;
    }
}
