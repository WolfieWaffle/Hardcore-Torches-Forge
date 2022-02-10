package com.github.wolfiewaffle.hardcore_torches.blockentity;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.block.BlockState;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class FuelBlockEntity extends TileEntity implements ITickableTileEntity {
    protected int fuel;
    protected static Random random = new Random();

    public FuelBlockEntity(TileEntityType<?> type) {
        super(type);
    }

    public int getFuel() {
        return fuel;
    }

    public void setFuel(int newValue) {
        fuel = newValue;
    }

    public void changeFuel(int increment) {
        World world = this.getLevel();
        BlockPos pos = this.getBlockPos();

        fuel += increment;

        if (fuel <= 0) {
            fuel = 0;

            if (world.getBlockState(pos).getBlock() instanceof IFuelBlock) {
                IFuelBlock block = (IFuelBlock) world.getBlockState(pos).getBlock();
                block.outOfFuel(world, pos, world.getBlockState(pos));
            }
        }
    }

    // region necessary methods
    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        if (nbt != null) {
            super.load(state, nbt);

            fuel = nbt.getInt("Fuel");
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);

        nbt.putInt("Fuel", fuel);

        return nbt;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, -1, this.save(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }
    // endregion
}
