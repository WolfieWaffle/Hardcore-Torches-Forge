package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.mojang.serialization.MapCodec;

public class LitSoulLanternBlock extends SoulLanternBlock {
    public static final MapCodec<LitSoulLanternBlock> CODEC = simpleCodec(LitSoulLanternBlock::new);

    public LitSoulLanternBlock(Properties prop) {
        super(prop, true, () -> Config.defaultSoulLanternFuel.get());
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public MapCodec<LitSoulLanternBlock> codec() {
        return CODEC;
    }
}
