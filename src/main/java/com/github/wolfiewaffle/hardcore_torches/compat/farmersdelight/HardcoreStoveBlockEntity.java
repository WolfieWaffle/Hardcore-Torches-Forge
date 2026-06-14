package com.github.wolfiewaffle.hardcore_torches.compat.farmersdelight;

import com.github.wolfiewaffle.hardcore_torches.blockentity.HardcoreCampfireBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.StoveBlock;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;

import javax.annotation.Nullable;

public class HardcoreStoveBlockEntity extends StoveBlockEntity {
    int fuel = 0;

    public HardcoreStoveBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public void addFuel(int additional) {
        fuel = fuel + additional;

        if (fuel > Config.campfireMaxFuel.get()) fuel = Config.campfireMaxFuel.get();
    }

    public boolean canAcceptFuel(int additional) {
        return ((fuel + additional) <= Config.campfireMaxFuel.get());
    }

    // SUPER NECESSARY BECAUSE WE ARE OVERRIDING AN EXISTING BLOCK ENTITY!!!
    @Override
    public BlockEntityType<?> getType() {
        return FarmersCommonCompat.STOVE_BLOCK_ENTITY.get();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HardcoreStoveBlockEntity stove) {
        stove.fuel -= 1;
        if (stove.fuel <= 0) {
            Block block = state.getBlock();
            if (block instanceof StoveBlock stoveBlock) {
                stoveBlock.extinguish(null, level, pos, state);
            }
        }
    }

    public static void animationTick(Level level, BlockPos pos, BlockState state, HardcoreStoveBlockEntity stove) {
        StoveBlockEntity.particleTick(level, pos, state, stove);
    }

    @Override
    public void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);

        if (compound.contains("Fuel")) {
            fuel = compound.getInt("Fuel");
        } else {
            fuel = 0;
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putInt("Fuel", fuel);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        this.loadAdditional(pkt.getTag(), lookupProvider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        saveAdditional(nbt, registries);
        return nbt;
    }
}
