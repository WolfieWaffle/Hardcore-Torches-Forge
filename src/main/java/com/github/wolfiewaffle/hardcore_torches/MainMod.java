package com.github.wolfiewaffle.hardcore_torches;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.config.ConfigRecipeCondition;
import com.github.wolfiewaffle.hardcore_torches.event.PlayerEventHandler;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.github.wolfiewaffle.hardcore_torches.loot.FatModifier;
import com.github.wolfiewaffle.hardcore_torches.loot.SetFuelLootFunction;
import com.github.wolfiewaffle.hardcore_torches.loot.TorchLootFunction;
import com.github.wolfiewaffle.hardcore_torches.recipe.OilCanRecipe;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// The value here should match an entry in the META-INF/mods.toml file
@net.minecraftforge.fml.common.Mod(MainMod.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MainMod.MOD_ID)
public class MainMod
{
    /**
     * The modid of this mod, this has to match the modid in mods.toml
     */
    public static final String MOD_ID = "hardcore_torches";

    // Groups
    public static TorchGroup basicTorches = new TorchGroup("basic");
    public static LanternGroup basicLanterns = new LanternGroup("basic");
    public static LanternGroup soulLanterns = new LanternGroup("soul");

    // Tags
    @SuppressWarnings("unused")
    public static final TagKey<Item> MC_ANIMAL_FAT = ItemTags.create(new ResourceLocation("minecraft:animal_fat"));

    @SuppressWarnings("unused")
    public static final TagKey<Item> ALL_TORCH_ITEMS = ItemTags.create(new ResourceLocation("hardcore_torches:torches"));

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
    public static final RegistryObject<RecipeType<OilCanRecipe>> OIL_CAN_RECIPE = RECIPE_TYPE_DEFERRED_REGISTER.register("oil_can", () -> new RecipeType<>() {});

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_DEFERRED_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    private static final RegistryObject<OilCanRecipe.Serializer> OIL_CAN_RECIPE_SERIALIZER = RECIPE_SERIALIZER_DEFERRED_REGISTER.register("oil_can", OilCanRecipe.Serializer::new);

    // Register Loot Tables
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MOD_CODEC_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final RegistryObject<Codec<FatModifier>> FAT_MOD_CODEC = LOOT_MOD_CODEC_REGISTER.register("fat_modifier", () -> FatModifier.codec);

    // Register Conditions?
    public static final DeferredRegister<Codec<? extends ICondition>> CONDITION_REGISTRY = DeferredRegister.create(ForgeRegistries.CONDITION_SERIALIZERS, MOD_ID);
    public static final RegistryObject<Codec<ConfigRecipeCondition>> CONFIG_CONDITION = CONDITION_REGISTRY.register("config_craft_unlit", () -> ConfigRecipeCondition.CODEC);

    // Register Loot Functions
    private static final DeferredRegister<LootItemFunctionType> LOOT_FUNC_REGISTER = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MOD_ID);
    public static final RegistryObject<LootItemFunctionType> TORCH_LOOT_FUNCTION_TYPE = LOOT_FUNC_REGISTER.register("torch", () -> HARDCORE_TORCH_LOOT_FUNCTION);
    public static final RegistryObject<LootItemFunctionType> SET_FUEL_FUNCTION_TYPE = LOOT_FUNC_REGISTER.register("set_damage", () -> SET_FUEL_LOOT_FUNCTION);

    // Register World features (replacement of vanilla torches and lanterns)
    private static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(ForgeRegistries.FEATURES, MOD_ID);
    private static final RegistryObject<ReplaceAllFeature> REPLACE_ALL_FEATURE = FEATURE_REGISTER.register("replace_all", () -> new ReplaceAllFeature(NoneFeatureConfiguration.CODEC.stable()));

    // Register World Modifiers (FORGE BROKEN RN, NEED CUSTOM MODIFIER)
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
    public static final RegistryObject<Codec<? extends BiomeModifier>> REPLACE_ALL_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("replace_all_modifier", () -> ReplaceAllBiomeModifier.CODEC);

    /**
     * Order of initialization:
     * 1. Registration
     * 2. Config reading (for client + common)
     * 3. FMLCommonSetupEvent
     * After world load:
     * 4. Config read on server
     */
    public MainMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        Config.init();

        // Init
        ItemInit.ITEMS.register(modEventBus);
        BlockInit.BLOCKS.register(modEventBus);
        BlockEntityInit.BLOCK_ENTITIES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());

        // Crafting
        CONDITION_REGISTRY.register(modEventBus);

        // For loot tables
        LOOT_MOD_CODEC_REGISTER.register(modEventBus);
        LOOT_FUNC_REGISTER.register(modEventBus);

        // For recipe types
        RECIPE_TYPE_DEFERRED_REGISTER.register(modEventBus);
        RECIPE_SERIALIZER_DEFERRED_REGISTER.register(modEventBus);

        // World generation
        FEATURE_REGISTER.register(modEventBus);
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> afterCommonSetup());

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

        // Register Loot Functions
        //Registry.register(Registries.LOOT_FUNCTION_TYPE, new ResourceLocation("hardcore_torches", "torch"), HARDCORE_TORCH_LOOT_FUNCTION);
        //Registry.register(Registries.LOOT_FUNCTION_TYPE, new ResourceLocation("hardcore_torches", "set_damage"), SET_FUEL_LOOT_FUNCTION);
    }

    private void afterCommonSetup() {



        // Recipe Conditions
//        CraftingHelper.register(new ConfigRecipeCondition.Serializer(() -> Config.craftUnlit.get(), new ResourceLocation("hardcore_torches", "config_craft_unlit")));
//        CraftingHelper.register(new ConfigRecipeCondition.Serializer(() -> (Config.oilRecipeType.get() == 0 || Config.oilRecipeType.get() == 2), new ResourceLocation("hardcore_torches", "config_can_fat")));
//        CraftingHelper.register(new ConfigRecipeCondition.Serializer(() -> (Config.oilRecipeType.get() == 1 || Config.oilRecipeType.get() == 2), new ResourceLocation("hardcore_torches", "config_can_coal")));
//        CraftingHelper.register(new ConfigRecipeCondition.Serializer(() -> Config.lanternsUseFuel.get(), new ResourceLocation("hardcore_torches", "lanterns_use_fuel")));
    }

//    @SubscribeEvent
//    public static void modEventCommunication(InterModEnqueueEvent event) {
//        if (ModList.get().isLoaded("curios")) {
//            //InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("belt").build());
//        }
//    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.getEntries().putBefore(Items.LEATHER.getDefaultInstance(), ItemInit.ANIMAL_FAT.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            ItemStack stack = Items.FIRE_CHARGE.getDefaultInstance();
            event.getEntries().putAfter(stack, ItemInit.ANIMAL_FAT.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(stack, ItemInit.OIL_CAN.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(stack, OilCanItem.setFuel(new ItemStack(ItemInit.OIL_CAN.get()), Config.maxCanFuel.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(stack, ItemInit.FIRE_STARTER.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            ItemStack stack = Items.SOUL_LANTERN.getDefaultInstance();
            event.getEntries().putAfter(stack, ItemInit.LIT_TORCH.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(stack, ItemInit.UNLIT_TORCH.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(stack, ItemInit.SMOLDERING_TORCH.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(stack, ItemInit.BURNT_TORCH.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(stack, ItemInit.LIT_LANTERN.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(stack, ItemInit.UNLIT_LANTERN.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(stack, ItemInit.LIT_SOUL_LANTERN.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(stack, ItemInit.UNLIT_SOUL_LANTERN.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
