package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.mojang.serialization.MapCodec;

public class UnlitSoulLanternBlock extends SoulLanternBlock {
    public static final MapCodec<UnlitSoulLanternBlock> CODEC = simpleCodec(UnlitSoulLanternBlock::new);

    public UnlitSoulLanternBlock(Properties prop) {
        super(prop, false, () -> Config.defaultSoulLanternFuel.get());
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public MapCodec<UnlitSoulLanternBlock> codec() {
        return CODEC;
    }
}
