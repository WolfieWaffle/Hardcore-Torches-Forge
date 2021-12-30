package com.github.wolfiewaffle.hardcore_torches.block;

public class LanternBlock extends AbstractLanternBlock {

    public LanternBlock(Properties prop, boolean isLit) {
        super(prop, isLit);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }
}
