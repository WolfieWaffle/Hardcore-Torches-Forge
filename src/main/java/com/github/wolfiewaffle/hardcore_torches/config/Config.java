package com.github.wolfiewaffle.hardcore_torches.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    public static ModConfigSpec CLIENT_CONFIG;
    public static ModConfigSpec COMMON_CONFIG;
    //public static ForgeConfigSpec SERVER_CONFIG;

    public static ModConfigSpec.ConfigValue<Boolean> torchesExtinguishWhenBroken;
    public static ModConfigSpec.ConfigValue<Boolean> torchesBurnWhenDropped;
    public static ModConfigSpec.ConfigValue<Boolean> torchesRain;
    public static ModConfigSpec.ConfigValue<Boolean> torchesSmolder;
    public static ModConfigSpec.ConfigValue<Boolean> craftUnlit;
    //public static ForgeConfigSpec.BooleanValue unlightInChest;
    public static ModConfigSpec.ConfigValue<Boolean> tickInInventory;
    public static ModConfigSpec.ConfigValue<Boolean> fuelMessage;
    public static ModConfigSpec.ConfigValue<Boolean> lanternsNeedCan;
    public static ModConfigSpec.ConfigValue<Boolean> torchesUseCan;
    public static ModConfigSpec.ConfigValue<Boolean> animalsDropFat;
    public static ModConfigSpec.ConfigValue<Boolean> handUnlightTorch;
    public static ModConfigSpec.ConfigValue<Boolean> handUnlightLantern;
    public static ModConfigSpec.ConfigValue<Boolean> starterLightCampfires;
    public static ModConfigSpec.ConfigValue<Boolean> starterLightTorches;
    public static ModConfigSpec.ConfigValue<Boolean> starterStartFires;
    public static ModConfigSpec.ConfigValue<Boolean> starterLightLanterns;
    public static ModConfigSpec.ConfigValue<Boolean> lanternsUseFuel;
    public static ModConfigSpec.ConfigValue<Boolean> pickUpLanterns;
    public static ModConfigSpec.ConfigValue<Boolean> craftLight;
    public static ModConfigSpec.ConfigValue<Boolean> craftHardcoreCampfire;
    public static ModConfigSpec.ConfigValue<Boolean> craftHardcoreStove;
    public static ModConfigSpec.ConfigValue<Boolean> craftBandolier;
    public static ModConfigSpec.ConfigValue<Boolean> showBandolier;
    public static ModConfigSpec.ConfigValue<Boolean> replaceInLootTables;
    public static ModConfigSpec.ConfigValue<Boolean> optimizerWarning;

    public static ModConfigSpec.ConfigValue<Double> oilRecipeMultiplier;
    public static ModConfigSpec.ConfigValue<Double> starterSuccessChance;
    public static ModConfigSpec.ConfigValue<Double> soulExpRatio;
    public static ModConfigSpec.ConfigValue<Double> campfireFuelFactor;

    public static ModConfigSpec.ConfigValue<Integer> defaultTorchFuel;
    public static ModConfigSpec.ConfigValue<Integer> defaultLanternFuel;
    public static ModConfigSpec.ConfigValue<Integer> defLanternFuelItem;
    public static ModConfigSpec.ConfigValue<Integer> minLanternIgnitionFuel;
    public static ModConfigSpec.ConfigValue<Integer> maxCanFuel;
    public static ModConfigSpec.ConfigValue<Integer> oilRecipeType;
    public static ModConfigSpec.ConfigValue<Integer> invExtinguishInWater;
    public static ModConfigSpec.ConfigValue<Integer> invExtinguishInRain;
    public static ModConfigSpec.ConfigValue<Integer> startingLanternFuel;
    public static ModConfigSpec.ConfigValue<Integer> expIncrement;
    public static ModConfigSpec.ConfigValue<Integer> defaultSoulLanternFuel;
    public static ModConfigSpec.ConfigValue<Integer> defaultSoulTorchFuel;
    public static ModConfigSpec.ConfigValue<Integer> torchCraftAmount;
    public static ModConfigSpec.ConfigValue<Integer> campfireMaxFuel;
    public static ModConfigSpec.ConfigValue<Integer> bottleExpAmount;
    public static ModConfigSpec.ConfigValue<Integer> burntDrop;
    public static ModConfigSpec.ConfigValue<Integer> bandolierMaxTorches;
    public static ModConfigSpec.ConfigValue<Integer> bandolierInteractMode;

    public static void init(ModContainer container) {
        //initServer();
        initCommon();
        initClient();

        container.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
    }

    private static void initServer() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    }

    private static void initCommon() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("General Settings").push("general");
        tickInInventory = builder.comment("If true, torches and lanterns will continue to lose fuel even while in the players inventory.").define("tickInInventory", false);
        animalsDropFat = builder.comment("If true, certain animals will drop fat as an item, which can be used in lanterns.").define("animalsDropFat", true);
        soulExpRatio = builder.comment("How many fuel ticks you get for a single experience point in the soul lanterns and torches. Default 4800 (4 minutes).").defineInRange("soulExpRatio", 4800, 1, Double.MAX_VALUE);
        expIncrement = builder.comment("How many experience points are used in a single right click. Default 1.").defineInRange("expIncrement", 1, 1, Integer.MAX_VALUE);
        bottleExpAmount = builder.comment("How much exp points should a bottle of enchanting count as. This is multiplied by soulExpRatio. The wiki says 7 is the average.").defineInRange("bottleExpAmount", 7, 1, Integer.MAX_VALUE);
        craftLight = builder.comment("If true, you can light torches and lanterns within the crafting inventory.").define("craftLight", true);
        replaceInLootTables = builder.comment("If true, replace torches, lanterns, etc. in loot tables with unlit versions.").define("replaceInLootTables", true);
        optimizerWarning = builder.comment("Set to false to disable warning messages about optimizers breaking the campfire.").define("optimizerWarning", true);
        builder.pop();

        builder.comment("Oil Can Settings").push("oil_can");
        maxCanFuel = builder.comment("The maximum fuel an oil can holds, in ticks.").defineInRange("maxCanFuel", 576000, 1, Integer.MAX_VALUE);
        lanternsNeedCan = builder.comment("Do lanterns require an oil can to be fueled?").define("lanternsNeedCan", true);
        torchesUseCan = builder.comment("Can torches be fueled with an oil can?").define("torchesUseCan", false);
        oilRecipeMultiplier = builder.comment("Globally modify all oil can recipes. 0.5 means all items give half as much oil.").defineInRange("oilRecipeMultiplier", 1, 0, Double.MAX_VALUE);
        oilRecipeType = builder.comment("0: Craft oil using can and animal fat\n1: Craft oil using can and coal\n2: Both enabled\n3: Disable recipes (You must provide custom JSON files, open the mod JAR to see format)").defineInRange("oilRecipeType", 0, 0, 3);
        builder.pop();

        builder.comment("Torch Settings").push("torch");
        defaultTorchFuel = builder.comment("How long a torch lasts when crafted. There are 20 ticks per second so 48000 ticks = 40 minutes.").defineInRange("defaultTorchFuel", 48000, 1, Integer.MAX_VALUE);
        defaultSoulTorchFuel = builder.comment("How long a soul torch can last without being refilled, in ticks.").defineInRange("defaultSoulTorchFuel", 48000, 1, Integer.MAX_VALUE);
        torchesExtinguishWhenBroken = builder.comment("Torches will become unlit when broken.").define("torchesExtinguishWhenBroken", true);
        torchesBurnWhenDropped = builder.comment("Overrides torchesExtinguishWhenBroken. Torches will be fully expended when broken (burnt torch or stick).").define("torchesBurnWhenDropped", true);
        burntDrop = builder.comment("0: Burnt torches drop as burnt torches\n1: Burnt torches drop as sticks\n2: Burnt torches drop nothing").defineInRange("burntDrop", 0, 0, 2);
        torchesRain = builder.comment("Torches will be affected when in the rain. Will smolder or become unlit depending on torchesSmolder.").define("torchesRain", true);
        torchesSmolder = builder.comment("If a torch is in the rain and torchesRain = true, then it will smolder and burn fuel at 1/3rd the normal rate instead of becoming unlit.").define("torchesSmolder", true);
        craftUnlit = builder.comment("If true, torches must be lit after crafting.").define("craftUnlit", false);
        handUnlightTorch = builder.comment("Right click a torch without holding fuel or a lighter to unlight it.").define("handUnlightTorch", false);
        invExtinguishInWater = builder.comment("0: When going underwater, torches in your inventory will be unaffected\n1: When going underwater, torches in mainhand or offhand will be extinguished\n2: When going underwater, torches in inventory will be extinguished").defineInRange("invExtinguishInWater", 2, 0, 2);
        invExtinguishInRain = builder.comment("0: When in rain, torches in your inventory will be unaffected\n1: When in rain, torches in mainhand or offhand will be extinguished or smolder\n2: When in rain, torches in inventory will be extinguished or smolder").defineInRange("invExtinguishInRain", 2, 0, 2);
        torchCraftAmount = builder.comment("How many torches are crafted.").defineInRange("torchCraftAmount", 1, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("Lantern Settings").push("lantern");
        lanternsUseFuel = builder.comment("If this mod also affects lanterns.").define("lanternsUseFuel", true);
        defaultLanternFuel = builder.comment("The max amount of fuel a lantern can hold. There are 20 ticks per second so 144000 ticks = 60 minutes.").defineInRange("defaultLanternFuel", 144000, 1, Integer.MAX_VALUE);
        defaultSoulLanternFuel = builder.comment("The max amount of fuel a soul lantern can hold. There are 20 ticks per second so 144000 ticks = 60 minutes.").defineInRange("defaultSoulLanternFuel", 144000, 1, Integer.MAX_VALUE);
        minLanternIgnitionFuel = builder.comment("A lantern must have at least this much fuel to be ignited from unlit. Once lit it will continue to burn to 0").defineInRange("minLanternIgnitionFuel", 1, 1, Integer.MAX_VALUE);
        defLanternFuelItem = builder.comment("The amount a fuel item adds to the lantern by default").defineInRange("defLanternFuelItem", 72000, 1, Integer.MAX_VALUE);
        handUnlightLantern = builder.comment("Right click a lantern without holding fuel or a lighter to unlight it.").define("handUnlightLantern", false);
        startingLanternFuel = builder.comment("How much fuel a newly crafted lantern starts with.").defineInRange("startingLanternFuel", 0, 0, Integer.MAX_VALUE);
        pickUpLanterns = builder.comment("Allow the player to pick up lanterns with sneak-clicking.").define("pickUpLanterns", true);
        builder.pop();

        builder.comment("Fire Starter Settings").push("fire_starter");
        starterLightCampfires = builder.comment("Can the fire starter light campfires").define("canLightCampfires", true);
        starterLightTorches = builder.comment("Can the fire starter light torches").define("canLightTorches", true);
        starterStartFires = builder.comment("Can the fire starter start full-block fires").define("canStartFires", true);
        starterLightLanterns = builder.comment("Can the fire starter light lanterns").define("canLightLanterns", false);
        starterSuccessChance = builder.comment("Percentage chance that the fire starter works").defineInRange("starterSuccessChance", 0.33, 0, 1);
        builder.pop();

        builder.comment("Campfire Settings").push("campfire");
        craftHardcoreCampfire = builder.comment("Whether campfires should be crafted as unlit/hardcore.").define("craftHardcoreCampfire", true);
        campfireMaxFuel = builder.comment("Max fuel that can be added to a campfire").defineInRange("campfireMaxFuel", 24000, 0, Integer.MAX_VALUE);
        campfireFuelFactor = builder.comment("Burn time for campfire fuel items is calculated by this value times its furnace duration").defineInRange("campfireFuelFactor", 8, 1, Double.MAX_VALUE);
        builder.pop();

        builder.comment("Compatibility Settings").push("compat");
        craftHardcoreStove = builder.comment("If true, and Farmer's Delight is installed, the Stove will be crafted as a hardcore version which needs fuel.").define("craftHardcoreStove", true);
        bandolierMaxTorches = builder.comment("Max torches that the Curios bandolier can hold").defineInRange("bandolierMaxTorches", 192, 1, Integer.MAX_VALUE);
        bandolierInteractMode = builder.comment("0: Placement disabled\n1: Place by right clicking a block\n2: Place by right clicking a block with an empty hand").defineInRange("bandolierInteractMode", 2, 0, 2);
        craftBandolier = builder.comment("If true, the bandolier has a crafting recipe.").define("craftBandolier", true);
        showBandolier = builder.comment("If true, the bandolier appears in the creative tab.").define("showBandolier", true);
        builder.pop();

        COMMON_CONFIG = builder.build();
    }

    private static void initClient() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("General Settings").push("general");
        fuelMessage = builder.comment("Send an actionbar message when right clicking a Hardcore Torches block to show its fuel amount.").define("fuelMessage", false);
        builder.pop();

        CLIENT_CONFIG = builder.build();
    }

}
