package com.github.wolfiewaffle.hardcore_torches.compat.amendments;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AmendmentsCommonCompat {
    public static final DeferredBlock<Block> LIT_WALL_LANTERN;
    public static final DeferredBlock<Block> UNLIT_WALL_LANTERN;
    public static final DeferredBlock<Block> LIT_WALL_SOUL_LANTERN;
    public static final DeferredBlock<Block> UNLIT_WALL_SOUL_LANTERN;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WallLanternBlockEntity>> WALL_LANTERN_BLOCK_ENTITY;

    public AmendmentsCommonCompat() {
    }

    // Used to initialize the class
    public static void loadCompat() {
    }

    public static void loadData() {
        ((HardcoreWallLantern)LIT_WALL_LANTERN.get()).setVariants((HardcoreWallLantern)LIT_WALL_LANTERN.get(), (HardcoreWallLantern)UNLIT_WALL_LANTERN.get());
        ((HardcoreWallLantern)UNLIT_WALL_LANTERN.get()).setVariants((HardcoreWallLantern)LIT_WALL_LANTERN.get(), (HardcoreWallLantern)UNLIT_WALL_LANTERN.get());
        ((HardcoreWallLantern)LIT_WALL_SOUL_LANTERN.get()).setVariants((HardcoreWallLantern)LIT_WALL_SOUL_LANTERN.get(), (HardcoreWallLantern)UNLIT_WALL_SOUL_LANTERN.get());
        ((HardcoreWallLantern)UNLIT_WALL_SOUL_LANTERN.get()).setVariants((HardcoreWallLantern)LIT_WALL_SOUL_LANTERN.get(), (HardcoreWallLantern)UNLIT_WALL_SOUL_LANTERN.get());
    }

    static {
        LIT_WALL_LANTERN = BlockInit.BLOCKS.register("lit_wall_lantern", () -> new HardcoreWallLantern(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> 15).sound(SoundType.LANTERN).noOcclusion(), true, HardcoreTorches.basicLanterns, () -> Config.defaultLanternFuel.get(), BlockInit.LIT_LANTERN.get()));
        UNLIT_WALL_LANTERN = BlockInit.BLOCKS.register("unlit_wall_lantern", () -> new HardcoreWallLantern(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> 0).sound(SoundType.LANTERN).noOcclusion(), false, HardcoreTorches.basicLanterns, () -> Config.defaultLanternFuel.get(), BlockInit.UNLIT_LANTERN.get()));
        LIT_WALL_SOUL_LANTERN = BlockInit.BLOCKS.register("lit_wall_soul_lantern", () -> new HardcoreWallLantern(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> 15).sound(SoundType.LANTERN).noOcclusion(), true, HardcoreTorches.soulLanterns, () -> Config.defaultLanternFuel.get(), BlockInit.LIT_SOUL_LANTERN.get()));
        UNLIT_WALL_SOUL_LANTERN = BlockInit.BLOCKS.register("unlit_wall_soul_lantern", () -> new HardcoreWallLantern(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> 0).sound(SoundType.LANTERN).noOcclusion(), false, HardcoreTorches.soulLanterns, () -> Config.defaultLanternFuel.get(), BlockInit.UNLIT_SOUL_LANTERN.get()));

        WALL_LANTERN_BLOCK_ENTITY = BlockEntityInit.BLOCK_ENTITIES.register("wall_lantern_block_entity", () -> Builder.of(WallLanternBlockEntity::new, new Block[]{LIT_WALL_LANTERN.get(), UNLIT_WALL_LANTERN.get(), LIT_WALL_SOUL_LANTERN.get(), UNLIT_WALL_SOUL_LANTERN.get()}).build(null));
    }
}
