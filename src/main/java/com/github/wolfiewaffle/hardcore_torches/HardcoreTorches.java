package com.github.wolfiewaffle.hardcore_torches;

import com.github.wolfiewaffle.hardcore_torches.compat.amendments.AmendmentsCommonCompat;
import com.github.wolfiewaffle.hardcore_torches.compat.farmersdelight.FarmersCommonCompat;
import com.github.wolfiewaffle.hardcore_torches.config.*;
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
import com.mojang.serialization.Codec;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
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
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(HardcoreTorches.MOD_ID)
public class HardcoreTorches
{
    public static final String MOD_ID = "hardcore_torches";

    // Groups
    public static TorchGroup basicTorches = new TorchGroup("basic");
    public static TorchGroup soulTorches = new TorchGroup("soul");
    public static LanternGroup basicLanterns = new LanternGroup("basic");
    public static LanternGroup soulLanterns = new LanternGroup("soul");

    // Tags
    @SuppressWarnings("unused")
    public static final TagKey<Item> MC_ANIMAL_FAT = ItemTags.create(new ResourceLocation("minecraft:animal_fat"));
    @SuppressWarnings("unused")
    public static final TagKey<Item> ALL_TORCH_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:torches"));
    public static final TagKey<Item> ACTIVE_TORCHES = ItemTags.create(new ResourceLocation("hardcore_torches:active_torches"));
    public static final TagKey<Item> INACTIVE_TORCHES = ItemTags.create(new ResourceLocation("hardcore_torches:inactive_torches"));
    public static final TagKey<Block> FREE_TORCH_LIGHT_BLOCKS = BlockTags.create(new ResourceLocation("hardcore_torches:free_torch_light_blocks"));
    public static final TagKey<Item> FREE_TORCH_LIGHT_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:free_torch_light_items"));
    public static final TagKey<Item> DAMAGE_TORCH_LIGHT_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:damage_torch_light_items"));
    public static final TagKey<Item> CONSUME_TORCH_LIGHT_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:consume_torch_light_items"));
    public static final TagKey<Item> FREE_TORCH_EXTINGUISH_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:free_torch_extinguish_items"));
    public static final TagKey<Item> DAMAGE_TORCH_EXTINGUISH_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:damage_torch_extinguish_items"));
    public static final TagKey<Item> CONSUME_TORCH_EXTINGUISH_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:consume_torch_extinguish_items"));
    public static final TagKey<Item> FREE_TORCH_SMOTHER_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:free_torch_smother_items"));
    public static final TagKey<Item> DAMAGE_TORCH_SMOTHER_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:damage_torch_smother_items"));
    public static final TagKey<Item> CONSUME_TORCH_SMOTHER_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:consume_torch_smother_items"));
    public static final TagKey<Item> FREE_LANTERN_LIGHT_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:free_lantern_light_items"));
    public static final TagKey<Item> DAMAGE_LANTERN_LIGHT_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:damage_lantern_light_items"));
    public static final TagKey<Item> CONSUME_LANTERN_LIGHT_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:consume_lantern_light_items"));
    public static final TagKey<Item> SOUL_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:soul_attunement_items"));

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
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MOD_CODEC_REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<FatModifier>> FAT_MOD_CODEC = LOOT_MOD_CODEC_REGISTER.register("fat_modifier", () -> FatModifier.codec);

    // Register Loot Functions
    private static final DeferredRegister<LootItemFunctionType> LOOT_FUNC_REGISTER = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MOD_ID);
    public static final DeferredHolder<LootItemFunctionType, LootItemFunctionType> TORCH_LOOT_FUNCTION_TYPE = LOOT_FUNC_REGISTER.register("torch", () -> HARDCORE_TORCH_LOOT_FUNCTION);
    public static final DeferredHolder<LootItemFunctionType, LootItemFunctionType> SET_FUEL_FUNCTION_TYPE = LOOT_FUNC_REGISTER.register("set_damage", () -> SET_FUEL_LOOT_FUNCTION);

    // Register World features (replacement of vanilla torches and lanterns)
    private static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(Registries.FEATURE, MOD_ID);
    private static final DeferredHolder<Feature<?>, ReplaceAllFeature> REPLACE_ALL_FEATURE = FEATURE_REGISTER.register("replace_all", () -> new ReplaceAllFeature(NoneFeatureConfiguration.CODEC.stable()));

    // Register World Modifiers
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
    public static final DeferredHolder<Codec<? extends BiomeModifier>, Codec<ReplaceAllBiomeModifier>> REPLACE_ALL_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("replace_all_modifier", () -> ReplaceAllBiomeModifier.CODEC);

    // Register Conditions
    public static final DeferredRegister<Codec<? extends ICondition>> CONDITION_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MOD_ID);
    public static final DeferredHolder<Codec<? extends ICondition>, Codec<ConfigCraftUnlitCondition>> CONDITION_CRAFT_UNLIT = CONDITION_SERIALIZERS.register(ConfigCraftUnlitCondition.NAME.getPath(), () -> ConfigCraftUnlitCondition.CODEC);
    public static final DeferredHolder<Codec<? extends ICondition>, Codec<ConfigCraftLightCondition>> CONDITION_CRAFT_LIGHT = CONDITION_SERIALIZERS.register(ConfigCraftLightCondition.NAME.getPath(), () -> ConfigCraftLightCondition.CODEC);
    public static final DeferredHolder<Codec<? extends ICondition>, Codec<ConfigHardcoreCampfireCondition>> CONDITION_HARDCORE_CAMPFIRE = CONDITION_SERIALIZERS.register(ConfigHardcoreCampfireCondition.NAME.getPath(), () -> ConfigHardcoreCampfireCondition.CODEC);
    public static final DeferredHolder<Codec<? extends ICondition>, Codec<ConfigHardcoreStoveCondition>> CONDITION_HARDCORE_STOVE = CONDITION_SERIALIZERS.register(ConfigHardcoreStoveCondition.NAME.getPath(), () -> ConfigHardcoreStoveCondition.CODEC);
    public static final DeferredHolder<Codec<? extends ICondition>, Codec<ConfigLanternsUseFuelCondition>> CONDITION_LANTERNS_USE_FUEL = CONDITION_SERIALIZERS.register(ConfigLanternsUseFuelCondition.NAME.getPath(), () -> ConfigLanternsUseFuelCondition.CODEC);
    public static final DeferredHolder<Codec<? extends ICondition>, Codec<ConfigOilcanFatCondition>> CONDITION_OILCANS_USE_FAT = CONDITION_SERIALIZERS.register(ConfigOilcanFatCondition.NAME.getPath(), () -> ConfigOilcanFatCondition.CODEC);
    public static final DeferredHolder<Codec<? extends ICondition>, Codec<ConfigOilcanCoalCondition>> CONDITION_OILCANS_USE_COAL = CONDITION_SERIALIZERS.register(ConfigOilcanCoalCondition.NAME.getPath(), () -> ConfigOilcanCoalCondition.CODEC);




    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public HardcoreTorches(IEventBus modEventBus)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildContents);
        //modEventBus.addListener(this::registerCapabilities);

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

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (HardcoreTorches) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        //NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        //modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        Config.init();
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

//    public void registerCapabilities(final RegisterCapabilitiesEvent event) {
//        if (ModList.get().isLoaded("curios")) {
//            event.registerItem(CuriosCapability.ITEM, (stack, context) -> new LanternCurio(stack), ItemInit.LIT_LANTERN.get(), ItemInit.LIT_SOUL_LANTERN.get());
//        }
//    }

    public void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ItemInit.OIL_CAN.get());
            event.accept(OilCanItem.setFuel(new ItemStack(ItemInit.OIL_CAN.get()), Config.maxCanFuel.get()));
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
