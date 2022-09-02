package com.mcupdater.autopackager.setup;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

import java.util.HashMap;
import java.util.Map;

public class CraftingCache {
    public static Map<ItemStack, CraftingRecipe> LARGE = new HashMap<>();
    public static Map<ItemStack, CraftingRecipe> SMALL = new HashMap<>();
    public static Map<ItemStack, CraftingRecipe> HOLLOW = new HashMap<>();
    public static Map<ItemStack, CraftingRecipe> SINGLE = new HashMap<>();
    public static Map<ItemStack, CraftingRecipe> CROSS = new HashMap<>();
    public static Map<ItemStack, CraftingRecipe> STAIR = new HashMap<>();
    public static Map<ItemStack, CraftingRecipe> SLAB = new HashMap<>();
    public static Map<ItemStack, CraftingRecipe> WALL = new HashMap<>();
}
