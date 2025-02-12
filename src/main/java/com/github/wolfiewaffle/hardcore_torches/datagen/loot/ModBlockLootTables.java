package com.github.wolfiewaffle.hardcore_torches.datagen.loot;

import com.github.wolfiewaffle.hardcore_torches.compat.farmersdelight.FarmersCommonCompat;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.fml.ModList;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {

    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        if (ModList.get().isLoaded("farmersdelight")) {
            this.dropSelf(FarmersCommonCompat.HARDCORE_STOVE.get());
        }
    }

//    @Override
//    protected Iterable<Block> getKnownBlocks() {
//        return BlockInit.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
//
//    }
}
