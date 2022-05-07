package com.github.wolfiewaffle.hardcore_torches.blockentity;

import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LanternBlockEntity extends FuelBlockEntity {

    public LanternBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.LANTERN_BLOCK_ENTITY.get(), pos, state);
        fuel = Config.startingLanternFuel.get();
    }

    public void tick() {
        Level world = getLevel();

        if (!world.isClientSide) {
            BlockPos pos = getBlockPos();

            // Burn out
            if (fuel >= 0 && ((AbstractLanternBlock) world.getBlockState(pos).getBlock()).isLit) {
                changeFuel(-1);
            }

            setChanged();
        }
    }
}
