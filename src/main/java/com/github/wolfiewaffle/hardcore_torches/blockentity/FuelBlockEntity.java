package com.github.wolfiewaffle.hardcore_torches.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Random;

public class FuelBlockEntity extends BlockEntity {
    protected int fuel;
    protected static Random random = new Random();

    public FuelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int getFuel() {
        return fuel;
    }

    public void setFuel(int newValue) {
        fuel = newValue;
    }

    public void changeFuel(int increment) {
        Level world = this.getLevel();
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
    public void load(CompoundTag nbt) {
        if (nbt != null) {
            super.load(nbt);

            fuel = nbt.getInt("Fuel");
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.putInt("Fuel", fuel);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
    // endregion
}
