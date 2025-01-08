package com.github.wolfiewaffle.hardcore_torches.compat.amendments;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import com.mojang.datafixers.types.Type;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;

public class AmendmentsCommonCompat {
    public static final RegistryObject<Block> LIT_WALL_LANTERN;
    public static final RegistryObject<Block> UNLIT_WALL_LANTERN;
    public static final RegistryObject<Block> LIT_WALL_SOUL_LANTERN;
    public static final RegistryObject<Block> UNLIT_WALL_SOUL_LANTERN;
    public static final RegistryObject<BlockEntityType<WallLanternBlockEntity>> WALL_LANTERN_BLOCK_ENTITY;

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
        LIT_WALL_LANTERN = BlockInit.BLOCKS.register("lit_wall_lantern", () -> {
            return new HardcoreWallLantern(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> {
                return 15;
            }).sound(SoundType.LANTERN).noOcclusion(), true, MainMod.basicLanterns, () -> {
                return (Integer)Config.defaultLanternFuel.get();
            });
        });
        UNLIT_WALL_LANTERN = BlockInit.BLOCKS.register("unlit_wall_lantern", () -> {
            return new HardcoreWallLantern(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> {
                return 0;
            }).sound(SoundType.LANTERN).noOcclusion(), false, MainMod.basicLanterns, () -> {
                return (Integer)Config.defaultLanternFuel.get();
            });
        });
        LIT_WALL_SOUL_LANTERN = BlockInit.BLOCKS.register("lit_wall_soul_lantern", () -> {
            return new HardcoreWallLantern(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> {
                return 15;
            }).sound(SoundType.LANTERN).noOcclusion(), true, MainMod.soulLanterns, () -> {
                return (Integer)Config.defaultLanternFuel.get();
            });
        });
        UNLIT_WALL_SOUL_LANTERN = BlockInit.BLOCKS.register("unlit_wall_soul_lantern", () -> {
            return new HardcoreWallLantern(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel((state) -> {
                return 0;
            }).sound(SoundType.LANTERN).noOcclusion(), false, MainMod.soulLanterns, () -> {
                return (Integer)Config.defaultLanternFuel.get();
            });
        });
        WALL_LANTERN_BLOCK_ENTITY = BlockEntityInit.BLOCK_ENTITIES.register("wall_lantern_block_entity", () -> {
            return Builder.of(WallLanternBlockEntity::new, new Block[]{(Block)LIT_WALL_LANTERN.get(), (Block)UNLIT_WALL_LANTERN.get(), (Block)LIT_WALL_SOUL_LANTERN.get(), (Block)UNLIT_WALL_SOUL_LANTERN.get()}).build((Type)null);
        });
    }
}
