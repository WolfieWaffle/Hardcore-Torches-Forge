package com.github.wolfiewaffle.hardcore_torches.init;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.blockentity.LanternBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.TorchBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockEntityInit {

    private static Block[] torchTe() {
        return  new Block[] {
             BlockInit.LIT_TORCH.get(),
             BlockInit.LIT_WALL_TORCH.get(),
             BlockInit.SMOLDERING_TORCH.get(),
             BlockInit.SMOLDERING_WALL_TORCH.get()
        };
    }

    private static Block[] lanternTe() {
        return  new Block[] {
                BlockInit.LIT_LANTERN.get(),
                BlockInit.UNLIT_LANTERN.get()
        };
    }

    public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MainMod.MOD_ID);

    public static final RegistryObject<TileEntityType<TorchBlockEntity>> TORCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("torch_block_entity", () -> TileEntityType.Builder.of(TorchBlockEntity::new, torchTe()).build(null));
    public static final RegistryObject<TileEntityType<LanternBlockEntity>> LANTERN_BLOCK_ENTITY = BLOCK_ENTITIES.register("lantern_block_entity", () -> TileEntityType.Builder.of(LanternBlockEntity::new, lanternTe()).build(null));
}
