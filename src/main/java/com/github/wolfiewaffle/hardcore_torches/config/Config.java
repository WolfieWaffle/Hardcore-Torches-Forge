package com.github.wolfiewaffle.hardcore_torches.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    public static ForgeConfigSpec CLIENT_CONFIG;
    public static ForgeConfigSpec COMMON_CONFIG;
    //public static ForgeConfigSpec SERVER_CONFIG;

    public static ForgeConfigSpec.BooleanValue torchesExtinguishWhenBroken;
    public static ForgeConfigSpec.BooleanValue torchesBurnWhenDropped;
    public static ForgeConfigSpec.BooleanValue torchesRain;
    public static ForgeConfigSpec.BooleanValue torchesSmolder;
    public static ForgeConfigSpec.BooleanValue burntStick;
    public static ForgeConfigSpec.BooleanValue craftUnlit;
    //public static ForgeConfigSpec.BooleanValue unlightInChest;
    public static ForgeConfigSpec.BooleanValue tickInInventory;
    public static ForgeConfigSpec.BooleanValue fuelMessage;
    public static ForgeConfigSpec.BooleanValue lanternsNeedCan;
    public static ForgeConfigSpec.BooleanValue torchesUseCan;
    public static ForgeConfigSpec.BooleanValue animalsDropFat;
    public static ForgeConfigSpec.BooleanValue handUnlightTorch;
    public static ForgeConfigSpec.BooleanValue handUnlightLantern;

    public static ForgeConfigSpec.DoubleValue oilRecipeMultiplier;

    public static ForgeConfigSpec.IntValue defaultTorchFuel;
    public static ForgeConfigSpec.IntValue defaultLanternFuel;
    public static ForgeConfigSpec.IntValue defLanternFuelItem;
    public static ForgeConfigSpec.IntValue minLanternIgnitionFuel;
    public static ForgeConfigSpec.IntValue maxCanFuel;
    public static ForgeConfigSpec.IntValue oilRecipeType;
    public static ForgeConfigSpec.IntValue invExtinguishInWater;
    public static ForgeConfigSpec.IntValue invExtinguishInRain;

    public static void init() {
        //initServer();
        initCommon();
        initClient();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
    }

    private static void initServer() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    }

    private static void initCommon() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("General Settings").push("general");
        tickInInventory = builder.comment("If true, torches and lanterns will continue to lose fuel even while in the players inventory.").define("tickInInventory", false);
        animalsDropFat = builder.comment("If true, certain animals will drop fat as an item, which can be used in lanterns.").define("animalsDropFat", true);
        builder.pop();

        builder.comment("Oil Can Settings").push("oil_can");
        maxCanFuel = builder.comment("The maximum fuel an oil can holds, in ticks.").defineInRange("maxCanFuel", 576000, 1, Integer.MAX_VALUE);
        lanternsNeedCan = builder.comment("Do lanterns require an oil can to be fueled?").define("lanternsNeedCan", true);
        torchesUseCan = builder.comment("Can torches be fueled with an oil can?").define("torchesUseCan", false);
        oilRecipeMultiplier = builder.comment("Globally modify all oil can recipes. 0.5 means all items give half as much oil.").defineInRange("oilRecipeMultiplier", 1, 0, Double.MAX_VALUE);
        oilRecipeType = builder.comment("0: Craft oil using can and animal fat\n1: Craft oil using can and coal\n2: Both enabled\n3: Disable recipes (You must provide custom JSON files, open the mod JAR to see format)").defineInRange("oilRecipeType", 0, 0, 3);
        builder.pop();

        builder.comment("Torch Settings").push("torch");
        defaultTorchFuel = builder.comment("How long a torch lasts when crafted. There are 20 ticks per second so 48000 ticks = 20 minutes.").defineInRange("defaultTorchFuel", 48000, 1, Integer.MAX_VALUE);
        torchesExtinguishWhenBroken = builder.comment("Torches will become unlit when broken.").define("torchesExtinguishWhenBroken", true);
        torchesBurnWhenDropped = builder.comment("Overrides torchesExtinguishWhenBroken. Torches will be fully expended when broken (burnt torch or stick).").define("torchesBurnWhenDropped", true);
        burntStick = builder.comment("Fully expended torches will drop as sticks rather than burnt torches.").define("burntStick", true);
        torchesRain = builder.comment("Torches will be affected when in the rain. Will smolder or become unlit depending on torchesSmolder.").define("torchesRain", true);
        torchesSmolder = builder.comment("If a torch is in the rain and torchesRain = true, then it will smolder and burn fuel at 1/3rd the normal rate instead of becoming unlit.").define("torchesSmolder", true);
        craftUnlit = builder.comment("If true, torches must be lit after crafting.").define("craftUnlit", false);
        handUnlightTorch = builder.comment("Right click a torch without holding fuel or a lighter to unlight it.").define("handUnlightTorch", false);
        invExtinguishInWater = builder.comment("0: When going underwater, torches in your inventory will be unaffected\n1: When going underwater, torches in mainhand or offhand will be extinguished\n2: When going underwater, torches in inventory will be extinguished").defineInRange("invExtinguishInWater", 2, 0, 2);
        invExtinguishInRain = builder.comment("0: When in rain, torches in your inventory will be unaffected\n1: When in rain, torches in mainhand or offhand will be extinguished or smolder\n2: When in rain, torches in inventory will be extinguished or smolder").defineInRange("invExtinguishInRain", 2, 0, 2);
        builder.pop();

        builder.comment("Lantern Settings").push("lantern");
        defaultLanternFuel = builder.comment("The max amount of fuel a lantern can hold. There are 20 ticks per second so 144000 ticks = 60 minutes.").defineInRange("defaultLanternFuel", 144000, 1, Integer.MAX_VALUE);
        minLanternIgnitionFuel = builder.comment("A lantern must have at least this much fuel to be ignited from unlit. Once lit it will continue to burn to 0").defineInRange("minLanternIgnitionFuel", 1, 1, Integer.MAX_VALUE);
        defLanternFuelItem = builder.comment("The amount a fuel item adds to the lantern by default").defineInRange("defLanternFuelItem", 72000, 1, Integer.MAX_VALUE);
        handUnlightLantern = builder.comment("Right click a lantern without holding fuel or a lighter to unlight it.").define("handUnlightLantern", false);
        builder.pop();

        COMMON_CONFIG = builder.build();
    }

    private static void initClient() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("General Settings").push("general");
        fuelMessage = builder.comment("Send an actionbar message when right clicking a torch or lantern to show its fuel amount.").define("fuelMessage", false);
        builder.pop();

        CLIENT_CONFIG = builder.build();
    }

}
