package com.github.wolfiewaffle.hardcore_torches.blockentity;

import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LanternBlockEntity extends FuelBlockEntity {

    public LanternBlockEntity() {
        super(BlockEntityInit.LANTERN_BLOCK_ENTITY.get());
        fuel = 0;
    }

    public void tick() {
        World world = getLevel();

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
