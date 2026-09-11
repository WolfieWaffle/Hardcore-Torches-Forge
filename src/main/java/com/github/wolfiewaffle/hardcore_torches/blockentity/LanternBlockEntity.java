package com.github.wolfiewaffle.hardcore_torches.blockentity;

import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LanternBlockEntity extends FuelBlockEntity {
    public LanternBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.LANTERN_BLOCK_ENTITY.get(), pos, state);
        fuelTimer.set(Config.startingLanternFuel.get());
    }

    @Override
    public int getMaxFuel() {
        return Config.defaultLanternFuel.get();
    }

    @Override
    public boolean burnsContinuously(BlockState state) {
        return state.getBlock() instanceof AbstractLanternBlock lantern && lantern.isLit;
    }
}
