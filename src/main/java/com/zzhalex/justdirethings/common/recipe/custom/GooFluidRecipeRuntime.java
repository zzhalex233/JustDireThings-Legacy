package com.zzhalex.justdirethings.common.recipe.custom;

import com.zzhalex.justdirethings.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class GooFluidRecipeRuntime {

    private GooFluidRecipeRuntime() {
    }

    public static GooSpreadDataRecipe findGooSpreadRecipe(IBlockState sourceState, int gooTier) {
        return findGooSpreadRecipe(sourceState, gooTier, null);
    }

    public static GooSpreadDataRecipe findGooSpreadRecipe(IBlockState sourceState, int gooTier, JDTBlockStateSpec gooCatalyst) {
        for (GooSpreadDataRecipe recipe : gooSpreadRecipes()) {
            if (recipe.matches(sourceState, gooTier, gooCatalyst)) {
                return recipe;
            }
        }
        return null;
    }

    public static GooSpreadTagDataRecipe findGooSpreadTagRecipe(IBlockState sourceState, int gooTier) {
        return findGooSpreadTagRecipe(sourceState, gooTier, null);
    }

    public static GooSpreadTagDataRecipe findGooSpreadTagRecipe(IBlockState sourceState, int gooTier, JDTBlockStateSpec gooCatalyst) {
        for (GooSpreadTagDataRecipe recipe : gooSpreadTagRecipes()) {
            if (recipe.matches(sourceState, gooTier, gooCatalyst)) {
                return recipe;
            }
        }
        return null;
    }

    public static FluidDropDataRecipe findFluidDropRecipe(IBlockState fluidState, ItemStack catalystStack) {
        for (FluidDropDataRecipe recipe : fluidDropRecipes()) {
            if (recipe.matches(fluidState, catalystStack)) {
                return recipe;
            }
        }
        return null;
    }

    public static List<GooSpreadDataRecipe> gooSpreadRecipes() {
        return recipesOfType(GooSpreadDataRecipe.class);
    }

    public static List<GooSpreadTagDataRecipe> gooSpreadTagRecipes() {
        return recipesOfType(GooSpreadTagDataRecipe.class);
    }

    public static List<FluidDropDataRecipe> fluidDropRecipes() {
        return recipesOfType(FluidDropDataRecipe.class);
    }

    public static List<ItemStack> gooCatalystsForTier(int tierRequirement) {
        return GooCatalystRegistry.catalystsForTier(tierRequirement);
    }

    public static List<ItemStack> itemStacksForBlockState(JDTBlockStateSpec spec) {
        return itemStacksForBlockState(spec.toBlockState());
    }

    public static List<ItemStack> itemStacksForBlockState(IBlockState state) {
        Item item = Item.getItemFromBlock(state.getBlock());
        if (item == Items.AIR) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new ItemStack(item, 1, item.getHasSubtypes() ? state.getBlock().getMetaFromState(state) : 0));
    }

    public static List<ItemStack> itemStacksForBlockTag(String tag) {
        Set<ItemStackKey> seen = new LinkedHashSet<>();
        List<ItemStack> stacks = new ArrayList<>();
        for (Block block : Block.REGISTRY) {
            if (block == Blocks.AIR || !matchesBlockTag(tag, block.getDefaultState())) {
                continue;
            }
            Item item = Item.getItemFromBlock(block);
            if (item != Items.AIR && seen.add(new ItemStackKey(item, 0))) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks;
    }

    public static boolean matchesBlockTag(String tag, IBlockState state) {
        ResourceLocation blockId = Block.REGISTRY.getNameForObject(state.getBlock());
        if (blockId == null) {
            return false;
        }

        String normalizedTag = normalize(tag);
        String path = blockId.getPath().toLowerCase(Locale.ROOT);
        String tagPath = tagPath(normalizedTag);
        String tagLeaf = tagLeaf(tagPath);
        if (path.equals(tagLeaf) || path.equals(tagLeaf + "_block") || path.equals("block_" + tagLeaf)) {
            return true;
        }
        if (Reference.MOD_ID.equals(blockId.getNamespace()) && normalizedTag.equals("c:storage_blocks/charcoal") && "charcoal".equals(path)) {
            return true;
        }

        ItemStack stack = itemStacksForBlockState(state).stream().findFirst().orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) {
            return false;
        }
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            String oreName = OreDictionary.getOreName(oreId);
            if (oreName.equalsIgnoreCase(normalizedTag)
                    || oreName.equalsIgnoreCase(tagPath)
                    || oreName.equalsIgnoreCase("block" + toUpperCamel(tagLeaf))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSourceFluidBlock(IBlockState state) {
        int level = getLevel(state);
        if (level >= 0) {
            return level == 0;
        }
        return state.getBlock() instanceof IFluidBlock || state.getBlock() instanceof BlockLiquid;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int getLevel(IBlockState state) {
        for (IProperty property : state.getPropertyKeys()) {
            if (!"level".equals(property.getName())) {
                continue;
            }
            Object value = state.getValue(property);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            try {
                return Integer.parseInt(property.getName((Comparable) value));
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        return -1;
    }

    private static <T extends IRecipe> List<T> recipesOfType(Class<T> type) {
        Collection<IRecipe> recipes = ForgeRegistries.RECIPES.getValuesCollection();
        List<T> matches = new ArrayList<>();
        for (IRecipe recipe : recipes) {
            if (type.isInstance(recipe)) {
                matches.add(type.cast(recipe));
            }
        }
        matches.sort(Comparator.comparing(recipe -> String.valueOf(recipe.getRegistryName())));
        return matches;
    }

    private static String normalize(String tag) {
        return tag.indexOf(':') >= 0 ? tag.toLowerCase(Locale.ROOT) : "c:" + tag.toLowerCase(Locale.ROOT);
    }

    private static String tagPath(String tag) {
        int namespaceEnd = tag.indexOf(':');
        return namespaceEnd >= 0 ? tag.substring(namespaceEnd + 1) : tag;
    }

    private static String tagLeaf(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String toUpperCamel(String value) {
        StringBuilder builder = new StringBuilder();
        boolean upperNext = true;
        for (char c : value.toCharArray()) {
            if (c == '_' || c == '-' || c == '/') {
                upperNext = true;
            } else if (upperNext) {
                builder.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static final class ItemStackKey {
        private final Item item;
        private final int meta;

        private ItemStackKey(Item item, int meta) {
            this.item = item;
            this.meta = meta;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ItemStackKey)) {
                return false;
            }
            ItemStackKey that = (ItemStackKey) other;
            return meta == that.meta && item == that.item;
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(item) + meta;
        }
    }
}
