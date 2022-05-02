package com.github.wolfiewaffle.hardcore_torches.block;

import java.util.function.IntSupplier;

public class LanternBlock extends AbstractLanternBlock {

    public LanternBlock(Properties prop, boolean isLit, IntSupplier maxFuel) {
        super(prop, isLit, maxFuel);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }
}
