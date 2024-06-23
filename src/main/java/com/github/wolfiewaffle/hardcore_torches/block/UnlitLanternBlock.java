package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.mojang.serialization.MapCodec;

public class UnlitLanternBlock extends LanternBlock {
    public static final MapCodec<UnlitLanternBlock> CODEC = simpleCodec(UnlitLanternBlock::new);

    public UnlitLanternBlock(Properties prop) {
        super(prop, false, () -> Config.defaultLanternFuel.get());
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public MapCodec<UnlitLanternBlock> codec() {
        return CODEC;
    }
}
