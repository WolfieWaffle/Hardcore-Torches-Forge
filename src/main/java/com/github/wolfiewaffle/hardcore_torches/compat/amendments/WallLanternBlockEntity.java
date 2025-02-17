package com.github.wolfiewaffle.hardcore_torches.compat.amendments;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.LanternGroup;
import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Random;

public class WallLanternBlockEntity extends WallLanternBlockTile implements IFuelBlockEntity {
    public LanternGroup group;
    public boolean isLit;
    protected int fuel;
    protected static Random random = new Random();

    public WallLanternBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        this.group = HardcoreTorches.basicLanterns;
        this.isLit = true;
        this.fuel = Config.startingLanternFuel.get();
    }

    public BlockEntityType<?> getType() {
        return AmendmentsCommonCompat.WALL_LANTERN_BLOCK_ENTITY.get();
    }

    public int getFuel() {
        return this.fuel;
    }

    public int getMaxFuel() {
        return ((HardcoreWallLantern)this.getBlockState().getBlock()).getMaxFuel();
    }

    @Override
    public boolean canAddFuel() {
        return getFuel() < getMaxFuel();
    }

    public static void tick(Level world, BlockPos pos, BlockState state, WallLanternBlockEntity tile) {
        if (!world.isClientSide) {
            if (tile.getFuel() >= 0 && ((HardcoreWallLantern)world.getBlockState(pos).getBlock()).isLit) {
                tile.setFuel(tile.getFuel() - 1);
                if (tile.getFuel() <= 0) {
                    Block block = tile.getBlockState().getBlock();
                    if (block instanceof HardcoreWallLantern) {
                        HardcoreWallLantern lantern = (HardcoreWallLantern)block;
                        lantern.extinguish(tile.getLevel(), tile.getBlockPos(), tile.getBlockState(), true);
                    }
                }
            }

            tile.setChanged();
        }
    }

    public void setFuel(int newValue) {
        this.fuel = Math.min(this.getMaxFuel(), Math.max(0, newValue));
    }

    @Override
    public void load(CompoundTag nbt) {
        if (nbt != null) {
            super.load(nbt);
            this.fuel = nbt.getInt("Fuel");
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.putInt("Fuel", this.fuel);
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
}
