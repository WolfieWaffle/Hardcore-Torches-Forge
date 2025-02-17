package com.github.wolfiewaffle.hardcore_torches.compat.farmersdelight;

import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.ToIntFunction;

public class FarmersCommonCompat {
    public static final DeferredBlock<HardcoreStove> HARDCORE_STOVE;
    public static final DeferredItem<BlockItem> HARDCORE_STOVE_ITEM;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HardcoreStoveBlockEntity>> STOVE_BLOCK_ENTITY;

    public FarmersCommonCompat() {
    }

    // Used to initialize the class
    public static void loadCompat() {
    }

    public static void loadData() {

    }

    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return (state) -> (Boolean)state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    static {
        HARDCORE_STOVE = BlockInit.BLOCKS.register("stove", () -> new HardcoreStove(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).lightLevel(litBlockEmission(13)), true));
        HARDCORE_STOVE_ITEM = ItemInit.ITEMS.register("stove", () -> new BlockItem(HARDCORE_STOVE.get(), new Item.Properties()));

        STOVE_BLOCK_ENTITY = BlockEntityInit.BLOCK_ENTITIES.register("hardcore_stove_block_entity", () -> BlockEntityType.Builder.of(HardcoreStoveBlockEntity::new, new Block[]{HARDCORE_STOVE.get()}).build(null));
    }

    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        // DISABLED UNTIL FARMERS DELIGHT UPDATES
//        if (event.getTabKey() == ModCreativeTabs.TAB_FARMERS_DELIGHT.) {
//            event.accept(HARDCORE_STOVE_ITEM);
//        }
    }
}
