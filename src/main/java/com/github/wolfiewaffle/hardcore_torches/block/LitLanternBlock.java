package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.mojang.serialization.MapCodec;

public class LitLanternBlock extends LanternBlock {
    public static final MapCodec<LitLanternBlock> CODEC = simpleCodec(LitLanternBlock::new);

    public LitLanternBlock(Properties prop) {
        super(prop, true, () -> Config.defaultLanternFuel.get());
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public MapCodec<LitLanternBlock> codec() {
        return CODEC;
    }
}
