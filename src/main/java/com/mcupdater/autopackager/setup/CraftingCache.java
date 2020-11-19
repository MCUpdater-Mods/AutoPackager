package com.mcupdater.autopackager.setup;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;

import java.util.HashMap;
import java.util.Map;

public class CraftingCache {
    public static Map<ItemStack,ICraftingRecipe> LARGE = new HashMap<>();
    public static Map<ItemStack,ICraftingRecipe> SMALL = new HashMap<>();
    public static Map<ItemStack,ICraftingRecipe> HOLLOW = new HashMap<>();
    public static Map<ItemStack,ICraftingRecipe> SINGLE = new HashMap<>();
    public static Map<ItemStack,ICraftingRecipe> CROSS = new HashMap<>();
    public static Map<ItemStack,ICraftingRecipe> STAIR = new HashMap<>();
    public static Map<ItemStack,ICraftingRecipe> SLAB = new HashMap<>();
    public static Map<ItemStack,ICraftingRecipe> WALL = new HashMap<>();
}
