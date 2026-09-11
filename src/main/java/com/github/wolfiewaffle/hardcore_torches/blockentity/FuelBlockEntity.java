package com.github.wolfiewaffle.hardcore_torches.blockentity;

import com.github.wolfiewaffle.hardcore_torches.burnout.FuelMath;
import com.github.wolfiewaffle.hardcore_torches.burnout.ScheduledBurner;
import com.github.wolfiewaffle.hardcore_torches.burnout.ScheduledFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Random;

public class FuelBlockEntity extends BlockEntity implements IFuelBlockEntity, ScheduledBurner {
    // The helper stores this reference only; scheduling starts in onLoad, not here.
    @SuppressWarnings("this-escape")
    protected final ScheduledFuel fuelTimer = new ScheduledFuel(this);
    protected static final Random random = new Random();

    public FuelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int getFuel() {
        return fuelTimer.get();
    }

    @Override
    public int getMaxFuel() {
        return 0;
    }

    @Override
    public void setFuel(int newValue) {
        fuelTimer.set(Math.min(Math.max(0, getMaxFuel()), Math.max(0, newValue)));
    }

    @Override
    public boolean canAddFuel() {
        return getFuel() < getMaxFuel();
    }

    public void changeFuel(int increment) {
        setFuel(FuelMath.addClamped(getFuel(), increment, Math.max(0, getMaxFuel())));
        if (getFuel() == 0) onBurnoutCheck();
    }

    @Override
    public final BlockEntity burnoutBlockEntity() {
        return this;
    }

    @Override
    public boolean burnsContinuously(BlockState state) {
        return false;
    }

    @Override
    public void onBurnoutCheck() {
        Level world = getLevel();
        if (world == null || world.isClientSide || isRemoved()) return;
        if (getFuel() == 0 && getBlockState().getBlock() instanceof IFuelBlock block) {
            block.outOfFuel(world, getBlockPos(), getBlockState());
        }
        if (!isRemoved()) fuelTimer.refresh();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        fuelTimer.onLoad();
    }

    @Override
    public void onChunkUnloaded() {
        fuelTimer.onRemoved();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        fuelTimer.onRemoved();
        super.setRemoved();
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        fuelTimer.refresh();
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        fuelTimer.load(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        fuelTimer.save(nbt);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        fuelTimer.save(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
