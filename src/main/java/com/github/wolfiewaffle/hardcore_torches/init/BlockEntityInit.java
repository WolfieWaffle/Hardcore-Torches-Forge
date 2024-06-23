package com.github.wolfiewaffle.hardcore_torches.init;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.blockentity.LanternBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.TorchBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
                BlockInit.UNLIT_LANTERN.get(),
                BlockInit.LIT_SOUL_LANTERN.get(),
                BlockInit.UNLIT_SOUL_LANTERN.get()
        };
    }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MainMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<TorchBlockEntity>> TORCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("torch_block_entity", () -> BlockEntityType.Builder.of(TorchBlockEntity::new, torchTe()).build(null));
    public static final RegistryObject<BlockEntityType<LanternBlockEntity>> LANTERN_BLOCK_ENTITY = BLOCK_ENTITIES.register("lantern_block_entity", () -> BlockEntityType.Builder.of(LanternBlockEntity::new, lanternTe()).build(null));
}
