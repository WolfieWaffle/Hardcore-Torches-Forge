package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.mojang.serialization.MapCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.BaseEntityBlock;

import java.util.function.IntSupplier;

public class LanternBlock extends AbstractLanternBlock {

    public static final MapCodec<HardcoreFloorTorchBlock> CODEC = null;

    public LanternBlock(Properties prop, boolean isLit, IntSupplier maxFuel) {
        super(prop, isLit, maxFuel);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    public TagKey getFreeLightItems() {
        return HardcoreTorches.FREE_LANTERN_LIGHT_ITEMS;
    }

    @Override
    public TagKey getDamageLightItems() {
        return HardcoreTorches.DAMAGE_LANTERN_LIGHT_ITEMS;
    }

    @Override
    public TagKey getConsumeLightItems() {
        return HardcoreTorches.CONSUME_LANTERN_LIGHT_ITEMS;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }
}
