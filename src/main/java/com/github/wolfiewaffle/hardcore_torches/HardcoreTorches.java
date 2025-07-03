package com.github.wolfiewaffle.hardcore_torches;

import com.github.wolfiewaffle.hardcore_torches.compat.amendments.AmendmentsCommonCompat;
import com.github.wolfiewaffle.hardcore_torches.compat.curio.CuriosCommonCompat;
import com.github.wolfiewaffle.hardcore_torches.compat.farmersdelight.FarmersCommonCompat;
import com.github.wolfiewaffle.hardcore_torches.component.DataTypes;
import com.github.wolfiewaffle.hardcore_torches.config.*;
import com.github.wolfiewaffle.hardcore_torches.event.PlayerEventHandler;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.github.wolfiewaffle.hardcore_torches.loot.FatModifier;
import com.github.wolfiewaffle.hardcore_torches.loot.SetFuelLootFunction;
import com.github.wolfiewaffle.hardcore_torches.loot.TorchLootFunction;
import com.github.wolfiewaffle.hardcore_torches.recipe.DamageLightRecipe;
import com.github.wolfiewaffle.hardcore_torches.recipe.OilCanRecipe;
import com.github.wolfiewaffle.hardcore_torches.recipe.TorchRecipe;
import com.github.wolfiewaffle.hardcore_torches.util.LanternGroup;
import com.github.wolfiewaffle.hardcore_torches.util.TorchGroup;
import com.github.wolfiewaffle.hardcore_torches.world.ReplaceAllBiomeModifier;
import com.github.wolfiewaffle.hardcore_torches.world.ReplaceAllFeature;
import com.ibm.icu.impl.ValidIdentifiers;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(HardcoreTorches.MOD_ID)
public class HardcoreTorches
{
    private static ModContainer HCTcontainer;

    public static final String MOD_ID = "hardcore_torches";

    // Groups
    public static TorchGroup basicTorches = new TorchGroup("basic");
    public static TorchGroup soulTorches = new TorchGroup("soul");
    public static LanternGroup basicLanterns = new LanternGroup("basic");
    public static LanternGroup soulLanterns = new LanternGroup("soul");

    // Tags
    @SuppressWarnings("unused")
    public static final TagKey<Item> MC_ANIMAL_FAT = ItemTags.create(ResourceLocation.parse("minecraft:animal_fat"));
    @SuppressWarnings("unused")
    public static final TagKey<Item> ALL_TORCH_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:torches"));
    public static final TagKey<Item> ACTIVE_TORCHES = ItemTags.create(ResourceLocation.parse("hardcore_torches:active_torches"));
    public static final TagKey<Item> INACTIVE_TORCHES = ItemTags.create(ResourceLocation.parse("hardcore_torches:inactive_torches"));
    public static final TagKey<Block> FREE_TORCH_LIGHT_BLOCKS = BlockTags.create(ResourceLocation.parse("hardcore_torches:free_torch_light_blocks"));
    public static final TagKey<Item> FREE_TORCH_LIGHT_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:free_torch_light_items"));
    public static final TagKey<Item> DAMAGE_TORCH_LIGHT_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:damage_torch_light_items"));
    public static final TagKey<Item> CONSUME_TORCH_LIGHT_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:consume_torch_light_items"));
    public static final TagKey<Item> FREE_TORCH_EXTINGUISH_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:free_torch_extinguish_items"));
    public static final TagKey<Item> DAMAGE_TORCH_EXTINGUISH_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:damage_torch_extinguish_items"));
    public static final TagKey<Item> CONSUME_TORCH_EXTINGUISH_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:consume_torch_extinguish_items"));
    public static final TagKey<Item> FREE_TORCH_SMOTHER_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:free_torch_smother_items"));
    public static final TagKey<Item> DAMAGE_TORCH_SMOTHER_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:damage_torch_smother_items"));
    public static final TagKey<Item> CONSUME_TORCH_SMOTHER_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:consume_torch_smother_items"));
    public static final TagKey<Item> FREE_LANTERN_LIGHT_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:free_lantern_light_items"));
    public static final TagKey<Item> DAMAGE_LANTERN_LIGHT_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:damage_lantern_light_items"));
    public static final TagKey<Item> CONSUME_LANTERN_LIGHT_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:consume_lantern_light_items"));
    public static final TagKey<Item> SOUL_ITEMS = ItemTags.create(ResourceLocation.parse("hardcore_torches:soul_attunement_items"));

    // Loot Functions
    public static final LootItemFunctionType HARDCORE_TORCH_LOOT_FUNCTION = new LootItemFunctionType(TorchLootFunction.CODEC);
    public static final LootItemFunctionType SET_FUEL_LOOT_FUNCTION = new LootItemFunctionType(SetFuelLootFunction.CODEC);

    // Recipe Types
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPE_DEFERRED_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, MOD_ID);
    public static final DeferredHolder<RecipeType<?>, RecipeType<OilCanRecipe>> OIL_CAN_RECIPE = RECIPE_TYPE_DEFERRED_REGISTER.register("oil_can", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeType<?>, RecipeType<DamageLightRecipe>> DAMAGE_LIGHT_RECIPE = RECIPE_TYPE_DEFERRED_REGISTER.register("damage_light", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeType<?>, RecipeType<TorchRecipe>> TORCH_RECIPE = RECIPE_TYPE_DEFERRED_REGISTER.register("torch", () -> new RecipeType<>() {});

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_DEFERRED_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    private static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<OilCanRecipe>> OIL_CAN_RECIPE_SERIALIZER = RECIPE_SERIALIZER_DEFERRED_REGISTER.register("oil_can", OilCanRecipe.Serializer::new);
    private static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DamageLightRecipe>> DAMAGE_LIGHT_RECIPE_SERIALIZER = RECIPE_SERIALIZER_DEFERRED_REGISTER.register("damage_light", DamageLightRecipe.Serializer::new);
    private static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TorchRecipe>> TORCH_RECIPE_SERIALIZER = RECIPE_SERIALIZER_DEFERRED_REGISTER.register("torch", TorchRecipe.Serializer::new);

    // Register Loot Tables
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MOD_CODEC_REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<FatModifier>> FAT_MOD_CODEC = LOOT_MOD_CODEC_REGISTER.register("fat_modifier", () -> FatModifier.CODEC);

    // Register Loot Functions
    private static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNC_REGISTER = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MOD_ID);
    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<?>> TORCH_LOOT_FUNCTION_TYPE = LOOT_FUNC_REGISTER.register("torch", () -> HARDCORE_TORCH_LOOT_FUNCTION);
    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<?>> SET_FUEL_FUNCTION_TYPE = LOOT_FUNC_REGISTER.register("set_damage", () -> SET_FUEL_LOOT_FUNCTION);

    // Register World features (replacement of vanilla torches and lanterns)
    private static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(Registries.FEATURE, MOD_ID);
    private static final DeferredHolder<Feature<?>, ReplaceAllFeature> REPLACE_ALL_FEATURE = FEATURE_REGISTER.register("replace_all", () -> new ReplaceAllFeature(NoneFeatureConfiguration.CODEC.stable()));

    // Register World Modifiers
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<ReplaceAllBiomeModifier>> REPLACE_ALL_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("replace_all_modifier", () -> ReplaceAllBiomeModifier.CODEC);

    // Register Conditions
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MOD_ID);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigCraftUnlitCondition>> CONDITION_CRAFT_UNLIT = CONDITION_SERIALIZERS.register(ConfigCraftUnlitCondition.NAME.getPath(), () -> ConfigCraftUnlitCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigCraftLightCondition>> CONDITION_CRAFT_LIGHT = CONDITION_SERIALIZERS.register(ConfigCraftLightCondition.NAME.getPath(), () -> ConfigCraftLightCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigHardcoreCampfireCondition>> CONDITION_HARDCORE_CAMPFIRE = CONDITION_SERIALIZERS.register(ConfigHardcoreCampfireCondition.NAME.getPath(), () -> ConfigHardcoreCampfireCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigHardcoreStoveCondition>> CONDITION_HARDCORE_STOVE = CONDITION_SERIALIZERS.register(ConfigHardcoreStoveCondition.NAME.getPath(), () -> ConfigHardcoreStoveCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigLanternsUseFuelCondition>> CONDITION_LANTERNS_USE_FUEL = CONDITION_SERIALIZERS.register(ConfigLanternsUseFuelCondition.NAME.getPath(), () -> ConfigLanternsUseFuelCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigOilcanFatCondition>> CONDITION_OILCANS_USE_FAT = CONDITION_SERIALIZERS.register(ConfigOilcanFatCondition.NAME.getPath(), () -> ConfigOilcanFatCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigOilcanCoalCondition>> CONDITION_OILCANS_USE_COAL = CONDITION_SERIALIZERS.register(ConfigOilcanCoalCondition.NAME.getPath(), () -> ConfigOilcanCoalCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigCraftBandolierCondition>> CONDITION_CRAFT_BANDOLIER = CONDITION_SERIALIZERS.register(ConfigCraftBandolierCondition.NAME.getPath(), () -> ConfigCraftBandolierCondition.CODEC);

    // Register Data types
    public static final DeferredRegister<DataComponentType<?>> DATA_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TYPE_FUEL = DATA_TYPES.register("fuel", () -> DataTypes.FUEL);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TYPE_TORCH_COUNT = DATA_TYPES.register("torch_count", () -> DataTypes.TORCH_COUNT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<Item>>> TYPE_TORCH_TYPE = DATA_TYPES.register("torch_type", () -> DataTypes.TORCH_TYPE);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public HardcoreTorches(IEventBus modEventBus, ModContainer HCTcontainer)
    {
        this.HCTcontainer = HCTcontainer;

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildContents);
        modEventBus.addListener(this::registerCapabilities);

        NeoForge.EVENT_BUS.register(new PlayerEventHandler());

        // Init
        ItemInit.ITEMS.register(modEventBus);
        BlockInit.BLOCKS.register(modEventBus);
        BlockEntityInit.BLOCK_ENTITIES.register(modEventBus);

        // Compat
        if (ModList.get().isLoaded("amendments")) {
            AmendmentsCommonCompat.loadCompat();
        }
        if (ModList.get().isLoaded("farmersdelight")) {
            FarmersCommonCompat.loadCompat();
        }

        // For loot tables
        LOOT_MOD_CODEC_REGISTER.register(modEventBus);
        LOOT_FUNC_REGISTER.register(modEventBus);

        // For recipe types
        RECIPE_TYPE_DEFERRED_REGISTER.register(modEventBus);
        RECIPE_SERIALIZER_DEFERRED_REGISTER.register(modEventBus);

        // World generation
        FEATURE_REGISTER.register(modEventBus);
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);

        // Conditions
        CONDITION_SERIALIZERS.register(modEventBus);

        // Data types
        DATA_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (HardcoreTorches) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        //NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        //modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        Config.init(HCTcontainer);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Groups
        basicTorches.add(BlockInit.LIT_TORCH.get());
        basicTorches.add(BlockInit.LIT_WALL_TORCH.get());
        basicTorches.add(BlockInit.UNLIT_TORCH.get());
        basicTorches.add(BlockInit.UNLIT_WALL_TORCH.get());
        basicTorches.add(BlockInit.SMOLDERING_TORCH.get());
        basicTorches.add(BlockInit.SMOLDERING_WALL_TORCH.get());
        basicTorches.add(BlockInit.BURNT_TORCH.get());
        basicTorches.add(BlockInit.BURNT_WALL_TORCH.get());
        basicLanterns.add(BlockInit.LIT_LANTERN.get());
        basicLanterns.add(BlockInit.UNLIT_LANTERN.get());
        soulLanterns.add(BlockInit.LIT_SOUL_LANTERN.get());
        soulLanterns.add(BlockInit.UNLIT_SOUL_LANTERN.get());
        soulTorches.add(BlockInit.LIT_SOUL_TORCH.get());
        soulTorches.add(BlockInit.LIT_WALL_SOUL_TORCH.get());
        soulTorches.add(BlockInit.UNLIT_SOUL_TORCH.get());
        soulTorches.add(BlockInit.UNLIT_WALL_SOUL_TORCH.get());

        if (ModList.get().isLoaded("amendments")) {
            AmendmentsCommonCompat.loadData();
        }

        if (ModList.get().isLoaded("farmersdelight")) {
            FarmersCommonCompat.loadData();
        }
    }

    public void registerCapabilities(final RegisterCapabilitiesEvent event) {
        if (ModList.get().isLoaded("curios")) {
            CuriosCommonCompat.attachCapabilities(event);
        }
    }

    public void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ItemInit.OIL_CAN.get());
            event.accept(OilCanItem.setFuel(new ItemStack(ItemInit.OIL_CAN.get()), Config.maxCanFuel.get()));
            if (Config.showBandolier.get()) event.accept(ItemInit.BANDOLIER.get());
            event.accept(ItemInit.FIRE_STARTER.get());
            event.accept(ItemInit.ANIMAL_FAT.get());
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ItemInit.LIT_TORCH.get());
            event.accept(ItemInit.UNLIT_TORCH.get());
            event.accept(ItemInit.SMOLDERING_TORCH.get());
            event.accept(ItemInit.BURNT_TORCH.get());
            event.accept(ItemInit.LIT_SOUL_TORCH.get());
            event.accept(ItemInit.UNLIT_SOUL_TORCH.get());
            event.accept(ItemInit.LIT_LANTERN.get());
            event.accept(ItemInit.UNLIT_LANTERN.get());
            event.accept(ItemInit.LIT_SOUL_LANTERN.get());
            event.accept(ItemInit.UNLIT_SOUL_LANTERN.get());
            event.accept(ItemInit.UNLIT_CAMPFIRE.get());
        }

        if (ModList.get().isLoaded("farmersdelight")) {
            FarmersCommonCompat.creativeTab(event);
        }
    }
}
