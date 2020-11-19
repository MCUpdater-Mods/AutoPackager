package com.mcupdater.autopackager.setup;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_INSANITY = "insanity";
    public static ForgeConfigSpec.IntValue ENERGY_PER_CYCLE;
    public static ForgeConfigSpec.IntValue DELAY_NORMAL;
    public static ForgeConfigSpec.IntValue DELAY_IDLE;
    public static ForgeConfigSpec.BooleanValue DEEP_SLEEP;
    public static ForgeConfigSpec.IntValue MAX_DEEP_SLEEP;

    public static ForgeConfigSpec.BooleanValue LUDICROUS;
    public static ForgeConfigSpec.BooleanValue UNBALANCED;

    public static ForgeConfigSpec COMMON_CONFIG;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        ENERGY_PER_CYCLE = COMMON_BUILDER.comment("Forge Energy cost per operation").translation("autopackager.config.energy_cost").defineInRange("EnergyUse",10,0,Integer.MAX_VALUE);
        DELAY_NORMAL = COMMON_BUILDER.comment("Number of ticks between cycles when work is successful").translation("autopackager.config.cycle_delay").defineInRange("CycleDelay",10,0,Integer.MAX_VALUE);
        DELAY_IDLE = COMMON_BUILDER.comment("Number of ticks between cycles when no work has been done").translation("autopackager.config.idle_delay").defineInRange("IdleDelay",200,0,Integer.MAX_VALUE);
        DEEP_SLEEP = COMMON_BUILDER.comment("Should the AutoPackager scale up the idle delay on successive idle cycles").define("DeepSleep",false);
        MAX_DEEP_SLEEP = COMMON_BUILDER.comment("Maximum multiplier for deep sleep").defineInRange("MaxDeepCycles",20,0,Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Insane settings").push(CATEGORY_INSANITY);
        LUDICROUS = COMMON_BUILDER.comment("Do everything possible every cycle").define("Turbocharged",false);
        UNBALANCED = COMMON_BUILDER.comment("Energy cost applies only once per cycle").define("Cheapskate",false);
        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
